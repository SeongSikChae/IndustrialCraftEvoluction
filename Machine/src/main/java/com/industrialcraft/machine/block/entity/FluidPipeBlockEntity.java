package com.industrialcraft.machine.block.entity;

import com.industrialcraft.machine.block.FluidPipeBlock;
import com.industrialcraft.machine.fluid.FluidBuffer;
import com.industrialcraft.machine.fluid.FluidFillSteps;
import com.industrialcraft.machine.fluid.FluidHandler;
import com.industrialcraft.machine.fluid.FluidNeighbor;
import com.industrialcraft.machine.fluid.FluidTransfer;
import com.industrialcraft.machine.fluid.FluidUnits;
import com.industrialcraft.machine.util.BlockEntityClientSync;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class FluidPipeBlockEntity extends BlockEntity implements FluidHandler {
	private final FluidBuffer buffer = new FluidBuffer(FluidUnits.PIPE_CAPACITY_MB);
	private int syncedFillStep = -1;
	private Fluid syncedFluid = Fluids.EMPTY;

	public FluidPipeBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.FLUID_PIPE, pos, state);
	}

	public FluidBuffer getBuffer() {
		return this.buffer;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, FluidPipeBlockEntity entity) {
		if (FluidUnits.exceedsMaxSafePressure(entity.getPressureEighths())) {
			entity.ruptureFromOverpressure();
			return;
		}
		entity.propagate(level, pos, state);
	}

	private void propagate(Level level, BlockPos pos, BlockState state) {
		if (this.buffer.isEmpty()) {
			return;
		}
		Fluid fluid = this.buffer.getFluid();
		int budget = FluidUnits.FLOW_MAX_MB_PER_TICK;
		for (Direction direction : Direction.values()) {
			if (budget <= 0 || this.buffer.isEmpty()) {
				break;
			}
			if (!FluidPipeBlock.isConnected(state, direction)) {
				continue;
			}
			FluidHandler neighbor = FluidNeighbor.findInsertable(level, pos, direction);
			if (neighbor == null || neighbor == this) {
				continue;
			}
			if (!neighbor.getFluid().isSame(fluid) && neighbor.getAmount() > 0) {
				continue;
			}
			int moved = FluidTransfer.move(this, neighbor, direction, budget, false);
			if (moved > 0) {
				budget -= moved;
			}
			if (this.isRemoved()) {
				return;
			}
		}
	}

	@Override
	public boolean canInsert(Direction face) {
		return FluidPipeBlock.isConnected(this.getBlockState(), face);
	}

	@Override
	public boolean canExtract(Direction face) {
		return FluidPipeBlock.isConnected(this.getBlockState(), face);
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
	public int getPressureEighths() {
		return this.buffer.getPressureEighths();
	}

	@Override
	public boolean rupturesAboveMaxPressure() {
		return true;
	}

	@Override
	public void ruptureFromOverpressure() {
		Level level = this.level;
		if (level == null || level.isClientSide() || this.isRemoved()) {
			return;
		}
		BlockPos pos = this.worldPosition;
		BlockState state = this.getBlockState();
		if (!this.buffer.isEmpty()) {
			this.buffer.extract(this.buffer.getAmount(), false);
		}
		level.levelEvent(2001, pos, Block.getId(state));
		level.destroyBlock(pos, true);
	}

	@Override
	public int insert(Fluid fluid, int amountMb, boolean simulate) {
		return this.insert(fluid, amountMb, 0, simulate);
	}

	@Override
	public int insert(Fluid fluid, int amountMb, int pressureEighths, boolean simulate) {
		if (FluidUnits.exceedsMaxSafePressure(pressureEighths)) {
			if (!simulate) {
				this.ruptureFromOverpressure();
			}
			return 0;
		}
		int moved = this.buffer.insert(fluid, amountMb, pressureEighths, simulate);
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
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		this.buffer.save(output);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.buffer.load(input);
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
		tag.putInt("PressureEighths", this.buffer.getPressureEighths());
		return tag;
	}

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
