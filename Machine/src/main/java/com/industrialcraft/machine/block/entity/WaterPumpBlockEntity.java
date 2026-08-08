package com.industrialcraft.machine.block.entity;

import com.industrialcraft.machine.block.FluidPipeBlock;
import com.industrialcraft.machine.block.WaterPumpBlock;
import com.industrialcraft.machine.fluid.FluidBuffer;
import com.industrialcraft.machine.fluid.FluidHandler;
import com.industrialcraft.machine.fluid.FluidNeighbor;
import com.industrialcraft.machine.fluid.FluidUnits;
import com.industrialcraft.machine.item.ModItems;
import com.industrialcraft.machine.menu.WaterPumpMenu;
import com.industrialcraft.machine.power.PowerSource;
import com.industrialcraft.machine.power.ShaftPower;
import com.industrialcraft.machine.power.ShaftVisuals;
import com.industrialcraft.machine.power.SyncedSiFloat;
import com.industrialcraft.machine.util.BlockEntityClientSync;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Powered pump per {@code machine.mdc}: ω-driven intake/outflow, {@code p_pump = 10τ} (1 Nm ≡ 10 kPa),
 * optional check-valve accessory for reverse-pressure push and 10-tick boost when outlet is full.
 */
public class WaterPumpBlockEntity extends BaseContainerBlockEntity implements FluidHandler {
	public static final int MAX_OMEGA = 256;
	public static final int CAPACITY_MB = FluidUnits.MB_PER_BUCKET;
	public static final int CHECK_VALVE_SLOT = 0;
	public static final int CONTAINER_SIZE = 1;
	public static final int CHECK_VALVE_PRESSURIZE_INTERVAL = 10;

	public static final int DATA_AMOUNT = 0;
	public static final int DATA_FLUID_ID = 1;
	public static final int DATA_BUFFER_KPA_LO = 2;
	public static final int DATA_BUFFER_KPA_HI = 3;
	public static final int DATA_TORQUE_LO = 4;
	public static final int DATA_TORQUE_HI = 5;
	public static final int DATA_OMEGA_LO = 6;
	public static final int DATA_OMEGA_HI = 7;
	public static final int DATA_INLET_MB = 8;
	public static final int DATA_OUTLET_MB = 9;
	public static final int DATA_INLET_KPA_LO = 10;
	public static final int DATA_INLET_KPA_HI = 11;
	public static final int DATA_OUTLET_KPA_LO = 12;
	public static final int DATA_OUTLET_KPA_HI = 13;
	public static final int DATA_COUNT = 14;

	private static final Direction[] OUTPUT_CLAIM_ORDER = {
		Direction.UP,
		Direction.NORTH,
		Direction.EAST,
		Direction.WEST,
		Direction.SOUTH
	};

	private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
	private final FluidBuffer buffer = new FluidBuffer(CAPACITY_MB);
	private @Nullable Direction lockedOutputFace;
	private double appliedTorque;
	private double appliedOmega;
	private float shaftAngle;
	private int checkValveTickCounter;

	private int lastInletMb;
	private int lastOutletMb;
	private double lastInletKpa;
	private double lastOutletKpa;
	private boolean worldIntakeThisTick;

	private final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int index) {
			float bufferKpa = SyncedSiFloat.fromSi(FluidUnits.milliToKpa(WaterPumpBlockEntity.this.buffer.getPressureMilli()));
			float torque = SyncedSiFloat.fromSi(WaterPumpBlockEntity.this.appliedTorque);
			float omega = SyncedSiFloat.fromSi(WaterPumpBlockEntity.this.appliedOmega);
			float inletKpa = SyncedSiFloat.fromSi(WaterPumpBlockEntity.this.lastInletKpa);
			float outletKpa = SyncedSiFloat.fromSi(WaterPumpBlockEntity.this.lastOutletKpa);
			return switch (index) {
				case DATA_AMOUNT -> WaterPumpBlockEntity.this.buffer.getAmount();
				case DATA_FLUID_ID -> BuiltInRegistries.FLUID.getId(WaterPumpBlockEntity.this.buffer.getFluid());
				case DATA_BUFFER_KPA_LO -> SyncedSiFloat.loBits(bufferKpa);
				case DATA_BUFFER_KPA_HI -> SyncedSiFloat.hiBits(bufferKpa);
				case DATA_TORQUE_LO -> SyncedSiFloat.loBits(torque);
				case DATA_TORQUE_HI -> SyncedSiFloat.hiBits(torque);
				case DATA_OMEGA_LO -> SyncedSiFloat.loBits(omega);
				case DATA_OMEGA_HI -> SyncedSiFloat.hiBits(omega);
				case DATA_INLET_MB -> WaterPumpBlockEntity.this.lastInletMb;
				case DATA_OUTLET_MB -> WaterPumpBlockEntity.this.lastOutletMb;
				case DATA_INLET_KPA_LO -> SyncedSiFloat.loBits(inletKpa);
				case DATA_INLET_KPA_HI -> SyncedSiFloat.hiBits(inletKpa);
				case DATA_OUTLET_KPA_LO -> SyncedSiFloat.loBits(outletKpa);
				case DATA_OUTLET_KPA_HI -> SyncedSiFloat.hiBits(outletKpa);
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
		}

		@Override
		public int getCount() {
			return DATA_COUNT;
		}
	};

	public WaterPumpBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.WATER_PUMP, pos, state);
	}

	public ContainerData getDataAccess() {
		return this.dataAccess;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, WaterPumpBlockEntity entity) {
		if (level.isClientSide()) {
			entity.tickShaftVisual();
			return;
		}

		entity.lastInletMb = 0;
		entity.lastOutletMb = 0;
		entity.worldIntakeThisTick = false;

		entity.refreshOutputLock(level, pos, state);
		Direction inputFace = WaterPumpBlock.getInputFace(state);
		PowerSource source = ShaftPower.findIncoming(level, pos, inputFace);
		double torque = source != null ? PowerSource.sanitize(source.getTorque()) : 0.0;
		double omega = source != null ? Math.min(PowerSource.sanitize(source.getOmega()), MAX_OMEGA) : 0.0;

		boolean powerChanged = torque != entity.appliedTorque || omega != entity.appliedOmega;
		entity.appliedTorque = torque;
		entity.appliedOmega = omega;

		int rateMb = rateMb(omega);
		boolean pipeBelow = isPipe(level, pos.below());
		if (!pipeBelow && rateMb > 0) {
			entity.tryIntakeFromWorld(level, pos, rateMb);
		}
		if (rateMb > 0) {
			entity.pushOut(level, pos, rateMb, torque);
		}

		if (entity.worldIntakeThisTick) {
			entity.lastInletKpa = 0.0;
		} else if (entity.lastInletMb <= 0) {
			entity.lastInletKpa = 0.0;
		}
		if (entity.lastOutletMb <= 0) {
			entity.lastOutletKpa = FluidUnits.torqueNmToPumpKpa(entity.appliedTorque);
		}

		if (powerChanged) {
			entity.setChanged();
			entity.syncToClients();
		}
	}

	private void tickShaftVisual() {
		this.shaftAngle = ShaftVisuals.advanceAngle(
			this.shaftAngle,
			ShaftVisuals.degreesPerTick((float) this.appliedOmega)
		);
	}

	public float getShaftAngle(float partialTick) {
		return ShaftVisuals.interpolateAngle(
			this.shaftAngle,
			ShaftVisuals.degreesPerTick((float) this.appliedOmega),
			partialTick
		);
	}

	public double getAppliedTorque() {
		return this.appliedTorque;
	}

	public double getAppliedOmega() {
		return this.appliedOmega;
	}

	public @Nullable Direction getLockedOutputFace() {
		return this.lockedOutputFace;
	}

	public FluidBuffer getBuffer() {
		return this.buffer;
	}

	public boolean hasCheckValve() {
		return this.items.get(CHECK_VALVE_SLOT).is(ModItems.CHECK_VALVE_ACCESSORY);
	}

	public static boolean isCheckValve(ItemStack stack) {
		return stack.is(ModItems.CHECK_VALVE_ACCESSORY);
	}

	/**
	 * Outlet push gate. Without a check valve, fluid moves only when
	 * {@code p_pump >=} adjacent pipe pressure (equal allowed). Pressures are SI kPa.
	 */
	public static boolean canPushAgainstPressure(double pumpKpa, double destKpa, boolean hasCheckValve) {
		return hasCheckValve || pumpKpa >= destKpa;
	}

	/**
	 * Pressure carried into the outlet neighbor when inserting fluid.
	 * With a check valve, never dilute an already higher line pressure (use {@code max}).
	 */
	public static int outletInsertPressureMilli(int pumpMilli, int destMilli, boolean hasCheckValve) {
		int pump = Math.max(0, pumpMilli);
		int dest = Math.max(0, destMilli);
		if (hasCheckValve && dest > pump) {
			return dest;
		}
		return pump;
	}

	/**
	 * Check-valve boost schedule: every {@link #CHECK_VALVE_PRESSURIZE_INTERVAL} ticks while the
	 * adjacent pipe can be pressurized — full, or already at/above {@code p_pump} (line keeps
	 * draining so it never stays full; otherwise boost never armed).
	 *
	 * @return next counter value; {@link Integer#MIN_VALUE} sentinel means "fire boost and reset to 0"
	 */
	public static int advanceCheckValveBoostCounter(
		int counter,
		boolean hasCheckValve,
		boolean destHasFluid,
		boolean destFull,
		int pumpMilli,
		int destMilli
	) {
		if (!hasCheckValve || !destHasFluid || pumpMilli <= 0) {
			return 0;
		}
		boolean arm = destFull || destMilli >= pumpMilli;
		if (!arm) {
			return 0;
		}
		int next = counter + 1;
		if (next >= CHECK_VALVE_PRESSURIZE_INTERVAL) {
			return Integer.MIN_VALUE;
		}
		return next;
	}

	/** ω 256 → 1 B/tick. {@code omegaSi} is rad/s. */
	public static int rateMb(double omegaSi) {
		if (!(omegaSi > 0.0) || !Double.isFinite(omegaSi)) {
			return 0;
		}
		double omega = Math.min(omegaSi, MAX_OMEGA);
		return Math.max(0, (int) Math.round(CAPACITY_MB * (omega / (double) MAX_OMEGA)));
	}

	/** mB/tick → B/s. */
	public static double mbPerTickToFuPerSec(int mbPerTick) {
		return mbPerTick * 20.0 / FluidUnits.MB_PER_BUCKET;
	}

	public void refreshOutputLock(Level level, BlockPos pos, BlockState state) {
		Direction previous = this.lockedOutputFace;
		if (this.lockedOutputFace != null
			&& (!WaterPumpBlock.isFluidOutputFace(state, this.lockedOutputFace)
				|| !isPipe(level, pos.relative(this.lockedOutputFace)))) {
			this.lockedOutputFace = null;
		}
		if (this.lockedOutputFace == null) {
			for (Direction face : OUTPUT_CLAIM_ORDER) {
				if (WaterPumpBlock.isFluidOutputFace(state, face) && isPipe(level, pos.relative(face))) {
					this.lockedOutputFace = face;
					break;
				}
			}
		}
		if (this.lockedOutputFace != previous) {
			this.setChanged();
			this.syncToClients();
			updateNeighborPipeConnections(level, pos);
		}
	}

	public static boolean allowsPipeConnection(BlockGetter level, BlockPos pumpPos, Direction pumpFace) {
		if (pumpFace == Direction.DOWN) {
			return true;
		}
		BlockState state = level.getBlockState(pumpPos);
		if (!(state.getBlock() instanceof WaterPumpBlock)) {
			return false;
		}
		if (!WaterPumpBlock.isFluidOutputFace(state, pumpFace)) {
			return false;
		}
		BlockEntity be = level.getBlockEntity(pumpPos);
		if (!(be instanceof WaterPumpBlockEntity pump)) {
			return false;
		}
		return pumpFace == pump.lockedOutputFace;
	}

	private static boolean isPipe(Level level, BlockPos pos) {
		return level.getBlockState(pos).getBlock() instanceof FluidPipeBlock;
	}

	private static void updateNeighborPipeConnections(Level level, BlockPos pos) {
		for (Direction face : Direction.values()) {
			BlockPos neighborPos = pos.relative(face);
			BlockState neighbor = level.getBlockState(neighborPos);
			if (neighbor.getBlock() instanceof FluidPipeBlock) {
				BlockState updated = FluidPipeBlock.withConnections(neighbor, level, neighborPos);
				if (updated != neighbor) {
					level.setBlock(neighborPos, updated, Block.UPDATE_ALL);
				}
			}
		}
	}

	private void tryIntakeFromWorld(Level level, BlockPos pos, int rateMb) {
		int space = this.buffer.getSpace();
		if (space <= 0 || rateMb <= 0) {
			return;
		}
		BlockPos below = pos.below();
		FluidState fluidState = level.getFluidState(below);
		if (!fluidState.isSource()) {
			return;
		}
		Fluid fluid = FluidBuffer.normalize(fluidState.getType());
		if (fluid == Fluids.EMPTY) {
			return;
		}
		int want = Math.min(rateMb, space);
		int moved = this.buffer.insert(fluid, want, 0, false);
		if (moved > 0) {
			this.lastInletMb += moved;
			this.worldIntakeThisTick = true;
			this.lastInletKpa = 0.0;
			this.setChanged();
		}
	}

	private void pushOut(Level level, BlockPos pos, int rateMb, double torqueNm) {
		Direction out = this.lockedOutputFace;
		if (out == null || this.buffer.isEmpty() || rateMb <= 0) {
			return;
		}
		FluidHandler dest = FluidNeighbor.findInsertable(level, pos, out);
		if (dest == null) {
			return;
		}

		double pumpKpa = FluidUnits.torqueNmToPumpKpa(torqueNm);
		int pumpMilli = FluidUnits.torqueNmToPumpPressureMilli(torqueNm);
		this.lastOutletKpa = pumpKpa;
		double destKpa = FluidUnits.milliToKpa(dest.getPressureMilli());
		boolean checkValve = this.hasCheckValve();

		// Without check valve: transfer only when p_pump >= adjacent pipe pressure.
		if (!canPushAgainstPressure(pumpKpa, destKpa, checkValve)) {
			this.checkValveTickCounter = 0;
			return;
		}

		boolean destFull = dest.getCapacity() - dest.getAmount() <= 0;
		int boostAdvance = advanceCheckValveBoostCounter(
			this.checkValveTickCounter,
			checkValve,
			dest.getAmount() > 0,
			destFull,
			pumpMilli,
			dest.getPressureMilli()
		);
		if (boostAdvance == Integer.MIN_VALUE) {
			this.checkValveTickCounter = 0;
			if (dest instanceof FluidPipeBlockEntity pipe) {
				pipe.boostPressureMilli(pumpMilli);
			}
		} else {
			this.checkValveTickCounter = boostAdvance;
		}

		if (destFull) {
			return;
		}

		if (FluidUnits.exceedsMaxSafePressure(pumpMilli) && dest.rupturesAboveMaxPressure()) {
			int waste = Math.min(rateMb, this.buffer.getAmount());
			this.buffer.extract(waste, false);
			this.lastOutletMb += waste;
			dest.ruptureFromOverpressure();
			this.setChanged();
			return;
		}

		Fluid fluid = this.buffer.getFluid();
		int want = Math.min(rateMb, this.buffer.getAmount());
		int insertPressure = outletInsertPressureMilli(pumpMilli, dest.getPressureMilli(), checkValve);
		int accepted = dest.insert(fluid, want, insertPressure, true);
		if (accepted <= 0) {
			return;
		}
		int extracted = this.buffer.extract(accepted, false);
		int inserted = dest.insert(fluid, extracted, insertPressure, false);
		if (inserted < extracted) {
			this.buffer.insert(fluid, extracted - inserted, 0, false);
		}
		this.lastOutletMb += inserted;
		this.setChanged();
	}

	@Override
	public boolean canInsert(Direction face) {
		return face == Direction.DOWN;
	}

	@Override
	public boolean canExtract(Direction face) {
		return face == this.lockedOutputFace;
	}

	@Override
	public Fluid getFluid() {
		return this.buffer.getFluid();
	}

	@Override
	public int getAmount() {
		return this.buffer.getAmount();
	}

	@Override
	public int getCapacity() {
		return this.buffer.getCapacity();
	}

	@Override
	public int getPressureMilli() {
		return this.buffer.getPressureMilli();
	}

	@Override
	public int getReceiveGatePressureMilli() {
		return 0;
	}

	@Override
	public boolean sharesPressureVolume() {
		return false;
	}

	@Override
	public int insert(Fluid fluid, int amountMb, boolean simulate) {
		return this.insert(fluid, amountMb, 0, simulate);
	}

	@Override
	public int insert(Fluid fluid, int amountMb, int pressureMilli, boolean simulate) {
		// Passive pressure discarded — p_pump uses 10τ only.
		int moved = this.buffer.insert(fluid, amountMb, 0, simulate);
		if (!simulate && moved > 0) {
			this.lastInletMb += moved;
			if (!this.worldIntakeThisTick) {
				this.lastInletKpa = FluidUnits.milliToKpa(Math.max(0, pressureMilli));
			}
			this.setChanged();
		}
		return moved;
	}

	@Override
	public int extract(int maxAmountMb, boolean simulate) {
		int moved = this.buffer.extract(maxAmountMb, simulate);
		if (!simulate && moved > 0) {
			this.setChanged();
		}
		return moved;
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.machine.water_pump");
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
		return new WaterPumpMenu(containerId, inventory, this, this.dataAccess);
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return slot == CHECK_VALVE_SLOT && isCheckValve(stack);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, this.items);
		this.buffer.save(output);
		output.putDouble("AppliedTorqueNm", this.appliedTorque);
		output.putDouble("AppliedOmegaRadPerSec", this.appliedOmega);
		if (this.lockedOutputFace != null) {
			output.putString("LockedOutputFace", this.lockedOutputFace.getSerializedName());
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(input, this.items);
		this.buffer.load(input);
		this.appliedTorque = readAppliedSi(input, "AppliedTorqueNm", "AppliedTorqueMilli");
		this.appliedOmega = Math.min(readAppliedSi(input, "AppliedOmegaRadPerSec", "AppliedOmegaMilli"), MAX_OMEGA);
		this.lockedOutputFace = Direction.byName(input.getStringOr("LockedOutputFace", ""));
		if (this.lockedOutputFace != null
			&& !WaterPumpBlock.isFluidOutputFace(this.getBlockState(), this.lockedOutputFace)) {
			this.lockedOutputFace = null;
		}
	}

	/** Prefer SI doubles; migrate legacy milli ints (/1000). */
	private static double readAppliedSi(ValueInput input, String siKey, String milliKey) {
		double si = input.getDoubleOr(siKey, Double.NaN);
		if (Double.isFinite(si)) {
			return PowerSource.sanitize(si);
		}
		int milli = input.getIntOr(milliKey, Integer.MIN_VALUE);
		if (milli != Integer.MIN_VALUE) {
			return PowerSource.sanitize(milli / 1000.0);
		}
		return 0.0;
	}

	@Override
	public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = this.saveWithoutMetadata(registries);
		tag.putString("Fluid", BuiltInRegistries.FLUID.getKey(this.buffer.getFluid()).toString());
		tag.putInt("AmountMb", this.buffer.getAmount());
		tag.putInt("PressureMilli", this.buffer.getPressureMilli());
		tag.putDouble("AppliedTorqueNm", this.appliedTorque);
		tag.putDouble("AppliedOmegaRadPerSec", this.appliedOmega);
		if (this.lockedOutputFace != null) {
			tag.putString("LockedOutputFace", this.lockedOutputFace.getSerializedName());
		}
		return tag;
	}

	private void syncToClients() {
		BlockEntityClientSync.sync(this);
	}
}
