package com.industrialcraft.machine.block.entity;

import com.industrialcraft.machine.block.FluidPipeBlock;
import com.industrialcraft.machine.fluid.FluidBuffer;
import com.industrialcraft.machine.fluid.FluidHandler;
import com.industrialcraft.machine.fluid.FluidNeighbor;
import com.industrialcraft.machine.fluid.FluidTransfer;
import com.industrialcraft.machine.fluid.FluidUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class FluidPipeBlockEntity extends BlockEntity implements FluidHandler {
	private final FluidBuffer buffer = new FluidBuffer(FluidUnits.PIPE_CAPACITY_MB);

	public FluidPipeBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.FLUID_PIPE, pos, state);
	}

	public FluidBuffer getBuffer() {
		return this.buffer;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, FluidPipeBlockEntity entity) {
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
				this.setChanged();
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
	public int insert(Fluid fluid, int amountMb, boolean simulate) {
		return this.insert(fluid, amountMb, 0, simulate);
	}

	@Override
	public int insert(Fluid fluid, int amountMb, int pressureEighths, boolean simulate) {
		int moved = this.buffer.insert(fluid, amountMb, pressureEighths, simulate);
		if (!simulate && moved > 0) {
			this.setChanged();
			this.syncToClients();
		}
		return moved;
	}

	@Override
	public int extract(int maxAmountMb, boolean simulate) {
		int moved = this.buffer.extract(maxAmountMb, simulate);
		if (!simulate && moved > 0) {
			this.setChanged();
			this.syncToClients();
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
