package com.industrialcraft.machine.block.entity;

import com.industrialcraft.machine.block.FurnaceEngineBlock;
import com.industrialcraft.machine.item.ModItems;
import com.industrialcraft.machine.menu.FurnaceEngineMenu;
import com.industrialcraft.machine.power.FuelDurations;
import com.industrialcraft.machine.power.PowerSource;
import com.industrialcraft.machine.power.ShaftVisuals;
import com.industrialcraft.machine.util.BlockEntityClientSync;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class FurnaceEngineBlockEntity extends BaseContainerBlockEntity implements PowerSource {
	public static final int TORQUE = 4;
	public static final int OMEGA = 256;
	public static final int FUEL_SLOT = 0;
	public static final int GOVERNOR_SLOT = 1;
	public static final int CONTAINER_SIZE = 2;
	public static final int DATA_BURN_TIME = 0;
	public static final int DATA_BURN_DURATION = 1;
	public static final int DATA_SPIN_MILLI = 2;
	public static final int DATA_THROTTLE_MILLI = 3;
	public static final int DATA_APPLIED_THROTTLE_MILLI = 4;
	public static final int DATA_COUNT = 5;
	public static final int SPIN_MILLI_MAX = 10_000;
	public static final int THROTTLE_MILLI_MAX = 10_000;
	public static final int THROTTLE_PERCENT_MIN = 1;
	public static final int THROTTLE_PERCENT_MAX = 100;
	public static final float THROTTLE_MIN = THROTTLE_PERCENT_MIN / 100.0F;
	private static final float SPIN_ACCEL = 0.05F;
	private static final float SPIN_DECEL = 0.0125F;
	/** Linear ramp rate so 0↔100% takes ~1.25s (25 ticks). */
	private static final float THROTTLE_RAMP = 0.04F;

	private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
	private int burnTime;
	private int burnDuration;
	/** Fractional burn ticks accumulated against {@link #getEffectiveThrottle()}. */
	private float burnProgress;
	/** Target throttle 0..1 from the slider; used when a governor is installed. */
	private float throttle = 1.0F;
	/** Smoothed throttle 0..1 used for burn, power, and shaft visuals. */
	private float appliedThrottle = 1.0F;
	/** 0..1 flywheel factor shared by power output and shaft visuals. */
	private float spinFactor;
	private float shaftAngle;

	private final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int index) {
			return switch (index) {
				case DATA_BURN_TIME -> FurnaceEngineBlockEntity.this.burnTime;
				case DATA_BURN_DURATION -> FurnaceEngineBlockEntity.this.burnDuration;
				case DATA_SPIN_MILLI -> FurnaceEngineBlockEntity.this.getSpinMilli();
				case DATA_THROTTLE_MILLI -> FurnaceEngineBlockEntity.this.getThrottleMilli();
				case DATA_APPLIED_THROTTLE_MILLI -> FurnaceEngineBlockEntity.this.getAppliedThrottleMilli();
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
			switch (index) {
				case DATA_BURN_TIME -> FurnaceEngineBlockEntity.this.burnTime = value;
				case DATA_BURN_DURATION -> FurnaceEngineBlockEntity.this.burnDuration = value;
				case DATA_SPIN_MILLI -> FurnaceEngineBlockEntity.this.spinFactor = value / (float) SPIN_MILLI_MAX;
				case DATA_THROTTLE_MILLI ->
					FurnaceEngineBlockEntity.this.throttle =
						Mth.clamp(value / (float) THROTTLE_MILLI_MAX, THROTTLE_MIN, 1.0F);
				case DATA_APPLIED_THROTTLE_MILLI ->
					FurnaceEngineBlockEntity.this.appliedThrottle =
						Mth.clamp(value / (float) THROTTLE_MILLI_MAX, THROTTLE_MIN, 1.0F);
			}
		}

		@Override
		public int getCount() {
			return DATA_COUNT;
		}
	};

	public FurnaceEngineBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.FURNACE_ENGINE, pos, state);
	}

	public ContainerData getDataAccess() {
		return this.dataAccess;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, FurnaceEngineBlockEntity entity) {
		boolean wasLit = entity.isLit();
		boolean throttleChanged = entity.tickThrottleRamp();
		float effectiveThrottle = entity.getEffectiveThrottle();

		if (entity.burnTime > 0 && effectiveThrottle > 0.0F) {
			entity.burnProgress += effectiveThrottle;
			int consumed = (int) entity.burnProgress;
			if (consumed > 0) {
				entity.burnProgress -= consumed;
				entity.burnTime = Math.max(0, entity.burnTime - consumed);
			}
		}

		if (entity.burnTime <= 0 && effectiveThrottle > 0.0F) {
			entity.tryConsumeFuel(level);
		}

		boolean lit = entity.isLit();
		boolean powered = lit && effectiveThrottle > 0.0F;
		boolean spinChanged = entity.tickSpin(powered);

		if (wasLit != lit) {
			level.setBlock(pos, state.setValue(FurnaceEngineBlock.LIT, lit), 3);
			entity.setChanged();
			entity.syncToClients();
		} else if (lit || spinChanged || throttleChanged) {
			entity.setChanged();
		}
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, FurnaceEngineBlockEntity entity) {
		entity.tickThrottleRamp();
		boolean lit = state.getValue(FurnaceEngineBlock.LIT);
		boolean powered = lit && entity.getEffectiveThrottle() > 0.0F;
		entity.tickSpin(powered);

		if (lit) {
			RandomSource random = level.getRandom();
			if (random.nextFloat() < 0.18F) {
				FurnaceEngineBlock.spawnExhaustSmoke(level, pos, state, random, true);
			}
		}

		float shaftSpeed = entity.shaftDegreesPerTick();
		entity.shaftAngle = ShaftVisuals.advanceAngle(entity.shaftAngle, shaftSpeed);
	}

	private boolean tickThrottleRamp() {
		float target = this.getThrottleTarget();
		float previous = this.appliedThrottle;
		if (this.appliedThrottle < target) {
			this.appliedThrottle = Math.min(target, this.appliedThrottle + THROTTLE_RAMP);
		} else if (this.appliedThrottle > target) {
			this.appliedThrottle = Math.max(target, this.appliedThrottle - THROTTLE_RAMP);
		}
		return this.appliedThrottle != previous;
	}

	private boolean tickSpin(boolean powered) {
		float previous = this.spinFactor;
		if (powered) {
			this.spinFactor = Math.min(1.0F, this.spinFactor + SPIN_ACCEL);
		} else if (this.spinFactor > 0.0F) {
			this.spinFactor = Math.max(0.0F, this.spinFactor - SPIN_DECEL);
		}
		return this.spinFactor != previous;
	}

	/** Visual-only; uses log2(ω) via {@link ShaftVisuals}. */
	private float shaftDegreesPerTick() {
		return ShaftVisuals.degreesPerTick(OMEGA * this.getOutputScale());
	}

	public float getShaftAngle(float partialTick) {
		return ShaftVisuals.interpolateAngle(this.shaftAngle, this.shaftDegreesPerTick(), partialTick);
	}

	public float getSpinFactor() {
		return this.spinFactor;
	}

	public int getSpinMilli() {
		return Mth.clamp(Math.round(this.spinFactor * SPIN_MILLI_MAX), 0, SPIN_MILLI_MAX);
	}

	public boolean hasGovernor() {
		return this.items.get(GOVERNOR_SLOT).is(ModItems.GOVERNOR_ACCESSORY);
	}

	public float getThrottle() {
		return this.throttle;
	}

	/** Immediate target: slider value with governor, otherwise full open. */
	public float getThrottleTarget() {
		return this.hasGovernor() ? this.throttle : 1.0F;
	}

	/** Smoothed throttle used for burn, power, and shaft visuals. */
	public float getEffectiveThrottle() {
		return this.appliedThrottle;
	}

	public int getThrottleMilli() {
		return Mth.clamp(Math.round(this.throttle * THROTTLE_MILLI_MAX), 0, THROTTLE_MILLI_MAX);
	}

	public int getAppliedThrottleMilli() {
		return Mth.clamp(Math.round(this.appliedThrottle * THROTTLE_MILLI_MAX), 0, THROTTLE_MILLI_MAX);
	}

	public void setThrottlePercent(int percent) {
		float previous = this.throttle;
		this.throttle = Mth.clamp(percent, THROTTLE_PERCENT_MIN, THROTTLE_PERCENT_MAX) / 100.0F;
		if (this.throttle != previous) {
			this.setChanged();
			this.syncToClients();
		}
	}

	/** spinFactor × √effectiveThrottle — shared by τ, ω, shaft visuals, and GUI. */
	public static float outputScale(float spinFactor, float effectiveThrottle) {
		if (effectiveThrottle <= 0.0F) {
			return 0.0F;
		}
		return spinFactor * (float) Math.sqrt(effectiveThrottle);
	}

	/** spinFactor × √effectiveThrottle — shared by τ, ω, and shaft visuals. */
	public float getOutputScale() {
		return outputScale(this.spinFactor, this.getEffectiveThrottle());
	}

	private void tryConsumeFuel(Level level) {
		ItemStack fuel = this.items.get(FUEL_SLOT);
		if (fuel.isEmpty() || !isFuel(fuel)) {
			this.burnDuration = 0;
			return;
		}

		int add = FuelDurations.minecartStyleFuelTicks(level, fuel);
		if (!FuelDurations.canAcceptFuel(this.burnTime, add)) {
			return;
		}

		this.burnTime += add;
		this.burnDuration = add;
		fuel.shrink(1);
		if (fuel.isEmpty()) {
			this.items.set(FUEL_SLOT, ItemStack.EMPTY);
		}
		this.setChanged();
	}

	public static boolean isFuel(ItemStack stack) {
		return stack.is(ItemTags.FURNACE_MINECART_FUEL);
	}

	public static boolean isGovernor(ItemStack stack) {
		return stack.is(ModItems.GOVERNOR_ACCESSORY);
	}

	public boolean isLit() {
		return this.burnTime > 0;
	}

	@Override
	public int getTorque() {
		return Math.round(TORQUE * this.getOutputScale());
	}

	@Override
	public int getOmega() {
		return Math.round(OMEGA * this.getOutputScale());
	}

	@Override
	public boolean outputsToward(Direction face) {
		return face == FurnaceEngineBlock.getOutputFace(this.getBlockState());
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.machine.furnace_engine");
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.items = items;
	}

	@Override
	public int getContainerSize() {
		return CONTAINER_SIZE;
	}

	@Override
	protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
		return new FurnaceEngineMenu(containerId, inventory, this, this.dataAccess);
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return switch (slot) {
			case FUEL_SLOT -> isFuel(stack);
			case GOVERNOR_SLOT -> isGovernor(stack);
			default -> false;
		};
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		ItemStack previous = this.getItem(slot);
		super.setItem(slot, stack);
		if (slot == GOVERNOR_SLOT && !ItemStack.isSameItemSameComponents(previous, stack)) {
			this.syncToClients();
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, this.items);
		output.putInt("BurnTime", this.burnTime);
		output.putInt("BurnDuration", this.burnDuration);
		output.putFloat("BurnProgress", this.burnProgress);
		output.putFloat("Throttle", this.throttle);
		output.putFloat("AppliedThrottle", this.appliedThrottle);
		output.putFloat("SpinFactor", this.spinFactor);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(input, this.items);
		this.burnTime = input.getIntOr("BurnTime", 0);
		this.burnDuration = input.getIntOr("BurnDuration", 0);
		this.burnProgress = Mth.clamp(input.getFloatOr("BurnProgress", 0.0F), 0.0F, 1.0F);
		this.throttle = Mth.clamp(input.getFloatOr("Throttle", 1.0F), THROTTLE_MIN, 1.0F);
		this.appliedThrottle = Mth.clamp(input.getFloatOr("AppliedThrottle", this.throttle), THROTTLE_MIN, 1.0F);
		this.spinFactor = Mth.clamp(input.getFloatOr("SpinFactor", 0.0F), 0.0F, 1.0F);
	}

	@Override
	public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return this.saveWithoutMetadata(registries);
	}

	private void syncToClients() {
		BlockEntityClientSync.sync(this);
	}
}
