package com.industrialcraft.showcase.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Boots a consistent singleplayer world and prepares a flat display platform.
 */
@SuppressWarnings("UnstableApiUsage")
public final class ShowcaseWorlds {
	private ShowcaseWorlds() {
	}

	public static TestSingleplayerContext open(ClientGameTestContext context) {
		return context.worldBuilder().setUseConsistentSettings(true).create();
	}

	public static void prepareCreativeFlying(TestSingleplayerContext world) {
		world.getServer().runOnServer(server -> {
			ServerPlayer player = world.getConnection().getServerPlayer();
			player.setGameMode(GameType.CREATIVE);
			player.getAbilities().flying = true;
			player.onUpdateAbilities();
		});
		world.getConnection().waitForClientboundPackets();
	}

	/**
	 * Clears a box and lays a platform of {@code floor} at {@code origin.y - 1}.
	 */
	public static void clearPlatform(
		TestSingleplayerContext world,
		BlockPos origin,
		int radiusXZ,
		int height,
		Block floor
	) {
		world.getServer().runOnServer(server -> {
			ServerLevel level = world.getConnection().getServerLevel();
			for (int x = -radiusXZ; x <= radiusXZ; x++) {
				for (int z = -radiusXZ; z <= radiusXZ; z++) {
					level.setBlock(origin.offset(x, -1, z), floor.defaultBlockState(), 3);
					for (int y = 0; y < height; y++) {
						level.setBlock(origin.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
					}
				}
			}
		});
		world.getConnection().waitForClientboundPackets();
	}
}
