package com.industrialcraft.machine.gametest;

import com.industrialcraft.machine.block.FurnaceEngineBlock;
import com.industrialcraft.machine.block.ModBlocks;
import java.nio.file.Path;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("UnstableApiUsage")
public class FurnaceEngineFaceCaptureTest implements FabricClientGameTest {
	private static final BlockPos ENGINE_POS = new BlockPos(8, -60, 8);
	private static final Path OUT_DIR = Path.of("docs", "orientation-verify");

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().setUseConsistentSettings(true).create()) {
			Direction look = Direction.SOUTH;
			Direction expected = FurnaceEngineBlock.computeOutputShaft(look);

			BlockState placed = singleplayer.getServer().computeOnServer(server -> {
				ServerLevel level = singleplayer.getConnection().getServerLevel();
				for (int x = -3; x <= 3; x++) {
					for (int z = -3; z <= 3; z++) {
						level.setBlock(ENGINE_POS.offset(x, -1, z), Blocks.SMOOTH_STONE.defaultBlockState(), 3);
						level.setBlock(ENGINE_POS.offset(x, 0, z), Blocks.AIR.defaultBlockState(), 3);
						level.setBlock(ENGINE_POS.offset(x, 1, z), Blocks.AIR.defaultBlockState(), 3);
						level.setBlock(ENGINE_POS.offset(x, 2, z), Blocks.AIR.defaultBlockState(), 3);
					}
				}

				ServerPlayer player = singleplayer.getConnection().getServerPlayer();
				player.setGameMode(GameType.CREATIVE);
				player.getAbilities().flying = true;
				player.onUpdateAbilities();
				// Look SOUTH (yaw 0) — shaft toward player = NORTH
				player.teleportTo(level, ENGINE_POS.getX() + 0.5, ENGINE_POS.getY(), ENGINE_POS.getZ() - 2.5,
					java.util.Set.of(), 0.0F, 0.0F, false);

				BlockHitResult hit = new BlockHitResult(
					Vec3.atCenterOf(ENGINE_POS.below()).add(0, 0.5, 0),
					Direction.UP, ENGINE_POS.below(), false);
				BlockPlaceContext placeContext = new BlockPlaceContext(
					player, InteractionHand.MAIN_HAND, new ItemStack(ModBlocks.FURNACE_ENGINE), hit);
				BlockState state = ModBlocks.FURNACE_ENGINE.getStateForPlacement(placeContext);
				if (state == null) {
					throw new IllegalStateException("null placement");
				}
				state = state.setValue(FurnaceEngineBlock.LIT, true);
				level.setBlock(ENGINE_POS, state, 3);
				return level.getBlockState(ENGINE_POS);
			});

			Direction shape = FurnaceEngineBlock.detectSprocketFromShape(placed);
			if (placed.getValue(FurnaceEngineBlock.FACING) != expected || shape != expected) {
				throw new IllegalStateException("orientation proof failed: facing="
					+ placed.getValue(FurnaceEngineBlock.FACING) + " shape=" + shape + " expected=" + expected);
			}

			singleplayer.getConnection().waitForChunksDownload();
			singleplayer.getConnection().waitForChunksRender();
			context.waitTicks(40);

			for (Direction viewFrom : new Direction[]{Direction.NORTH, Direction.EAST, Direction.WEST, Direction.SOUTH}) {
				captureFace(context, singleplayer, viewFrom, expected);
			}
		}
	}

	private static void captureFace(
		ClientGameTestContext context,
		TestSingleplayerContext singleplayer,
		Direction viewFrom,
		Direction sprocket
	) {
		Vec3 center = Vec3.atCenterOf(ENGINE_POS);
		Vec3 eye = center.add(-viewFrom.getStepX() * 3.6, 0.45, -viewFrom.getStepZ() * 3.6);
		singleplayer.getServer().runOnServer(server -> {
			ServerPlayer player = singleplayer.getConnection().getServerLevel().players().getFirst();
			player.teleportTo(singleplayer.getConnection().getServerLevel(),
				eye.x, eye.y - player.getEyeHeight(), eye.z, java.util.Set.of(), 0, 0, false);
		});
		singleplayer.getConnection().waitForClientboundPackets();
		context.waitTicks(8);
		context.getInput().lookAt(ENGINE_POS);
		singleplayer.getConnection().waitForChunksRender();
		context.waitTicks(20);
		String name = "proof_look_south_sprocket_" + sprocket.getSerializedName() + "_view_" + viewFrom.getSerializedName();
		Path saved = context.takeScreenshot(
			TestScreenshotOptions.of(name).disableCounterPrefix().withDestinationDir(OUT_DIR).withSize(960, 720));
		System.out.println("Saved: " + saved.toAbsolutePath());
	}
}
