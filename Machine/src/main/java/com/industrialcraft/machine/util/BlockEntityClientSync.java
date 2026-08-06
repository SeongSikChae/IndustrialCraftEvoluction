package com.industrialcraft.machine.util;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Pushes block-entity NBT to tracking clients.
 * {@link Level#sendBlockUpdated} alone is not always enough when the {@link BlockState} is unchanged
 * (fluid amount / shaft values), so the update packet is sent explicitly.
 */
public final class BlockEntityClientSync {
	private BlockEntityClientSync() {
	}

	public static void sync(BlockEntity blockEntity) {
		Level level = blockEntity.getLevel();
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}
		BlockPos pos = blockEntity.getBlockPos();
		BlockState state = blockEntity.getBlockState();
		serverLevel.sendBlockUpdated(pos, state, state, 3);
		serverLevel.getChunkSource().blockChanged(pos);
		Packet<ClientGamePacketListener> packet = ClientboundBlockEntityDataPacket.create(blockEntity);
		for (ServerPlayer player : PlayerLookup.tracking(serverLevel, pos)) {
			player.connection.send(packet);
		}
	}
}
