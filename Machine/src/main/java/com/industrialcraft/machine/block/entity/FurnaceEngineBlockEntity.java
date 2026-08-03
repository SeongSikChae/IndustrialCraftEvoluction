package com.industrialcraft.machine.block.entity;

import com.industrialcraft.machine.block.FurnaceEngineBlock;
import com.industrialcraft.machine.menu.FurnaceEngineMenu;
import com.industrialcraft.machine.power.FuelDurations;
import com.industrialcraft.machine.power.PowerSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.ItemTags;
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
	public static final int CONTAINER_SIZE = 1;
	public static final int DATA_BURN_TIME = 0;
	public static final int DATA_BURN_DURATION = 1;
	public static final int DATA_COUNT = 2;

	private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
	private int burnTime;
	private int burnDuration;

	private final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int index) {
			return switch (index) {
				case DATA_BURN_TIME -> FurnaceEngineBlockEntity.this.burnTime;
				case DATA_BURN_DURATION -> FurnaceEngineBlockEntity.this.burnDuration;
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
			switch (index) {
				case DATA_BURN_TIME -> FurnaceEngineBlockEntity.this.burnTime = value;
				case DATA_BURN_DURATION -> FurnaceEngineBlockEntity.this.burnDuration = value;
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

		if (entity.burnTime > 0) {
			entity.burnTime--;
		}

		if (entity.burnTime <= 0) {
			entity.tryConsumeFuel(level);
		}

		boolean lit = entity.isLit();
		if (wasLit != lit) {
			level.setBlock(pos, state.setValue(FurnaceEngineBlock.LIT, lit), 3);
			entity.setChanged();
			entity.syncToClients();
		} else if (lit) {
			entity.setChanged();
		}
	}

	private void tryConsumeFuel(Level level) {
		ItemStack fuel = this.items.get(0);
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
			this.items.set(0, ItemStack.EMPTY);
		}
		this.setChanged();
	}

	public static boolean isFuel(ItemStack stack) {
		return stack.is(ItemTags.FURNACE_MINECART_FUEL);
	}

	public boolean isLit() {
		return this.burnTime > 0;
	}

	@Override
	public int getTorque() {
		return this.isLit() ? TORQUE : 0;
	}

	@Override
	public int getOmega() {
		return this.isLit() ? OMEGA : 0;
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
		return isFuel(stack);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, this.items);
		output.putInt("BurnTime", this.burnTime);
		output.putInt("BurnDuration", this.burnDuration);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(input, this.items);
		this.burnTime = input.getIntOr("BurnTime", 0);
		this.burnDuration = input.getIntOr("BurnDuration", 0);
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
		if (this.level != null && !this.level.isClientSide()) {
			BlockState state = this.getBlockState();
			this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
		}
	}
}
