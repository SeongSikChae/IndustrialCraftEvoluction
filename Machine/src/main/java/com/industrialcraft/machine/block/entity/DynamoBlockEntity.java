package com.industrialcraft.machine.block.entity;

import com.industrialcraft.machine.block.DynamoBlock;
import com.industrialcraft.machine.power.PowerSource;
import com.industrialcraft.machine.power.ShaftPower;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Relays adjacent rotary power 1:1 from the input face to the output face.
 * Values are cached each tick to keep multi-dynamo chains acyclic.
 */
public class DynamoBlockEntity extends BlockEntity implements PowerSource {
	private int torque;
	private int omega;
	private float shaftAngle;

	public DynamoBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.DYNAMO, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, DynamoBlockEntity entity) {
		if (level.isClientSide()) {
			entity.tickShaftVisual();
			return;
		}

		Direction inputFace = DynamoBlock.getInputFace(state);
		PowerSource source = ShaftPower.findIncoming(level, pos, inputFace);
		int nextTorque = source != null ? source.getTorque() : 0;
		int nextOmega = source != null ? source.getOmega() : 0;

		if (nextTorque != entity.torque || nextOmega != entity.omega) {
			entity.torque = nextTorque;
			entity.omega = nextOmega;
			entity.setChanged();
			entity.syncToClients();
		}
	}

	private void tickShaftVisual() {
		float speed = FurnaceEngineBlockEntity.MAX_SHAFT_SPEED
			* (this.omega / (float) FurnaceEngineBlockEntity.OMEGA);
		if (speed > 0.0F) {
			this.shaftAngle += speed;
			if (this.shaftAngle >= 360.0F) {
				this.shaftAngle %= 360.0F;
			}
		}
	}

	public float getShaftAngle(float partialTick) {
		float speed = FurnaceEngineBlockEntity.MAX_SHAFT_SPEED
			* (this.omega / (float) FurnaceEngineBlockEntity.OMEGA);
		return this.shaftAngle + speed * partialTick;
	}

	@Override
	public int getTorque() {
		return this.torque;
	}

	@Override
	public int getOmega() {
		return this.omega;
	}

	@Override
	public boolean outputsToward(Direction face) {
		return face == DynamoBlock.getOutputFace(this.getBlockState());
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("Torque", this.torque);
		output.putInt("Omega", this.omega);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.torque = input.getIntOr("Torque", 0);
		this.omega = input.getIntOr("Omega", 0);
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
