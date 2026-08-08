package com.industrialcraft.machine.block.entity;

import com.industrialcraft.machine.fluid.FluidBuffer;
import com.industrialcraft.machine.fluid.FluidBuckets;
import com.industrialcraft.machine.fluid.FluidFillSteps;
import com.industrialcraft.machine.fluid.FluidHandler;
import com.industrialcraft.machine.fluid.FluidNeighbor;
import com.industrialcraft.machine.fluid.FluidTransfer;
import com.industrialcraft.machine.fluid.FluidUnits;
import com.industrialcraft.machine.menu.ReservoirMenu;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class ReservoirBlockEntity extends BaseContainerBlockEntity implements FluidHandler {
	public static final int BUCKET_SLOT = 0;
	public static final int CONTAINER_SIZE = 1;
	public static final int DATA_AMOUNT = 0;
	public static final int DATA_FLUID_ID = 1;
	public static final int DATA_PRESSURE = 2;
	public static final int DATA_COUNT = 3;

	/** Claim order for the single pipe input (UP + horizontals). */
	private static final Direction[] INPUT_CLAIM_ORDER = {
		Direction.UP,
		Direction.NORTH,
		Direction.EAST,
		Direction.WEST,
		Direction.SOUTH
	};

	private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
	private final FluidBuffer buffer = new FluidBuffer(FluidUnits.RESERVOIR_CAPACITY_MB);
	/** Sticky input; null until a pipe is present on an input face. */
	private @Nullable Direction lockedInputFace;
	/** Last fill step / fluid pushed to clients for world visuals. */
	private int syncedFillStep = -1;
	private Fluid syncedFluid = Fluids.EMPTY;

	private final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int index) {
			return switch (index) {
				// Packet is short-sized; keep low 16 bits so receivers can treat as unsigned.
				case DATA_AMOUNT -> ReservoirBlockEntity.this.buffer.getAmount() & 0xFFFF;
				case DATA_FLUID_ID -> BuiltInRegistries.FLUID.getId(ReservoirBlockEntity.this.buffer.getFluid());
				case DATA_PRESSURE -> ReservoirBlockEntity.this.buffer.getPressureMilli();
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
			// Client mirror via addDataSlots; buffer is authoritative on server.
		}

		@Override
		public int getCount() {
			return DATA_COUNT;
		}
	};

	public ReservoirBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.RESERVOIR, pos, state);
	}

	public ContainerData getDataAccess() {
		return this.dataAccess;
	}

	public FluidBuffer getBuffer() {
		return this.buffer;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, ReservoirBlockEntity entity) {
		entity.refreshInputLock(level, pos);
		entity.tryDrainBucket();
		entity.pushDown(level, pos);
	}

	public void refreshInputLock(Level level, BlockPos pos) {
		Direction previous = this.lockedInputFace;
		if (this.lockedInputFace != null && !isPipe(level, pos.relative(this.lockedInputFace))) {
			this.lockedInputFace = null;
		}
		if (this.lockedInputFace == null) {
			for (Direction face : INPUT_CLAIM_ORDER) {
				if (isPipe(level, pos.relative(face))) {
					this.lockedInputFace = face;
					break;
				}
			}
		}
		if (this.lockedInputFace != previous) {
			this.setChanged();
			this.syncNeighborPipes(level, pos);
		}
	}

	public static boolean allowsPipeConnection(net.minecraft.world.level.BlockGetter level, BlockPos reservoirPos, Direction reservoirFace) {
		if (reservoirFace == Direction.DOWN) {
			return true;
		}
		if (reservoirFace == Direction.UP || reservoirFace.getAxis().isHorizontal()) {
			BlockEntity be = level.getBlockEntity(reservoirPos);
			if (be instanceof ReservoirBlockEntity reservoir) {
				if (level instanceof Level serverLevel && !serverLevel.isClientSide()) {
					reservoir.refreshInputLock(serverLevel, reservoirPos);
				}
				return reservoirFace == reservoir.lockedInputFace;
			}
			return false;
		}
		return false;
	}

	private static boolean isPipe(Level level, BlockPos pos) {
		return level.getBlockState(pos).getBlock() instanceof com.industrialcraft.machine.block.FluidPipeBlock;
	}

	private void syncNeighborPipes(Level level, BlockPos pos) {
		for (Direction face : Direction.values()) {
			BlockPos neighborPos = pos.relative(face);
			BlockState neighbor = level.getBlockState(neighborPos);
			if (neighbor.getBlock() instanceof com.industrialcraft.machine.block.FluidPipeBlock) {
				BlockState updated = com.industrialcraft.machine.block.FluidPipeBlock.withConnections(neighbor, level, neighborPos);
				if (updated != neighbor) {
					level.setBlock(neighborPos, updated, net.minecraft.world.level.block.Block.UPDATE_ALL);
				}
			}
		}
	}

	private void tryDrainBucket() {
		ItemStack stack = this.items.get(BUCKET_SLOT);
		if (!FluidBuckets.isFilledBucket(stack)) {
			return;
		}
		Fluid fluid = FluidBuckets.getFluid(stack);
		if (this.buffer.insert(fluid, FluidUnits.MB_PER_BUCKET, true) < FluidUnits.MB_PER_BUCKET) {
			return;
		}
		this.buffer.insert(fluid, FluidUnits.MB_PER_BUCKET, false);
		ItemStack emptied = FluidBuckets.emptiedBucket(stack);
		this.items.set(BUCKET_SLOT, emptied != null ? emptied : ItemStack.EMPTY);
		this.onFluidChanged();
	}

	private void pushDown(Level level, BlockPos pos) {
		if (this.buffer.isEmpty()) {
			return;
		}
		FluidHandler below = FluidNeighbor.findInsertable(level, pos, Direction.DOWN);
		if (below == null) {
			return;
		}
		FluidTransfer.move(this, below, Direction.DOWN, false);
	}

	@Override
	public boolean canInsert(Direction face) {
		return face == this.lockedInputFace;
	}

	@Override
	public boolean canExtract(Direction face) {
		return face == Direction.DOWN;
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

	/**
	 * Line pressure is not stored. Outbound head is hydrostatic: {@code 100 kPa × (q / C)}.
	 */
	@Override
	public int getPressureMilli() {
		if (this.buffer.isEmpty()) {
			return 0;
		}
		double headKpa = FluidUnits.RESERVOIR_FULL_HEAD_KPA
			* (this.buffer.getAmount() / (double) FluidUnits.RESERVOIR_CAPACITY_MB);
		return FluidUnits.kpaToMilli(headKpa);
	}

	/**
	 * Intake gate is always 0 kPa — line pressure is not retained in the tank.
	 */
	@Override
	public int getReceiveGatePressureMilli() {
		return 0;
	}

	@Override
	public boolean sharesPressureVolume() {
		return false;
	}

	/**
	 * Reservoirs do not store line pressure. The receive gate is 0 kPa; deposited fluid is stored at 0 kPa.
	 */
	@Override
	public int insert(Fluid fluid, int amountMb, boolean simulate) {
		return this.insert(fluid, amountMb, 0, simulate);
	}

	@Override
	public int insert(Fluid fluid, int amountMb, int pressureMilli, boolean simulate) {
		int moved = this.buffer.insert(fluid, amountMb, 0, simulate);
		if (!simulate && moved > 0) {
			this.onFluidChanged();
		}
		return moved;
	}

	@Override
	public int extract(int maxAmountMb, boolean simulate) {
		int moved = this.buffer.extract(maxAmountMb, simulate);
		if (!simulate && moved > 0) {
			this.onFluidChanged();
		}
		return moved;
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.machine.reservoir");
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
		return new ReservoirMenu(containerId, inventory, this, this.dataAccess);
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return slot == BUCKET_SLOT && FluidBuckets.isFilledBucket(stack);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, this.items);
		this.buffer.save(output);
		if (this.lockedInputFace != null) {
			output.putString("LockedInputFace", this.lockedInputFace.getSerializedName());
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(input, this.items);
		this.buffer.load(input);
		this.lockedInputFace = Direction.byName(input.getStringOr("LockedInputFace", ""));
		if (this.lockedInputFace == Direction.DOWN) {
			this.lockedInputFace = null;
		}
		this.syncedFillStep = FluidFillSteps.step(this.buffer.getAmount(), this.buffer.getCapacity());
		this.syncedFluid = this.buffer.getFluid();
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
		return tag;
	}

	/**
	 * Persist always; sync to clients only when the discrete fill step or fluid type changes.
	 */
	private void onFluidChanged() {
		this.setChanged();
		int step = FluidFillSteps.step(this.buffer.getAmount(), this.buffer.getCapacity());
		Fluid fluid = this.buffer.getFluid();
		if (step == this.syncedFillStep && fluid.isSame(this.syncedFluid)) {
			return;
		}
		this.syncedFillStep = step;
		this.syncedFluid = fluid;
		BlockEntityClientSync.sync(this);
	}
}
