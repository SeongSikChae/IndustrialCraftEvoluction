package com.industrialcraft.material.gametest;

import com.industrialcraft.material.block.ModBlocks;
import com.industrialcraft.material.item.ModItems;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Local client check: place compressed blocks + resolve GUI item models.
 * Visual screenshots are for human review (project-common §7: local only).
 * Do not dump tiny preview PNGs — they do not catch cube-face texture mistakes.
 */
@SuppressWarnings("UnstableApiUsage")
public class MaterialModelClientGameTest implements FabricClientGameTest {
	private static final BlockPos ORIGIN = new BlockPos(8, -60, 8);

	private static final List<Block> COMPRESSED = List.of(
		ModBlocks.PEAT_BLOCK,
		ModBlocks.LIGNITE_BLOCK,
		ModBlocks.SUB_BITUMINOUS_BLOCK,
		ModBlocks.ANTHRACITE_BLOCK
	);

	private static final List<Item> HOTBAR = List.of(
		ModBlocks.PEAT_BLOCK.asItem(),
		ModBlocks.LIGNITE_BLOCK.asItem(),
		ModBlocks.SUB_BITUMINOUS_BLOCK.asItem(),
		ModBlocks.ANTHRACITE_BLOCK.asItem(),
		ModItems.PEAT,
		ModItems.ANTHRACITE
	);

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
			world.getServer().runOnServer(server -> {
				ServerPlayer player = world.getConnection().getServerPlayer();
				player.setGameMode(GameType.CREATIVE);
				player.getAbilities().flying = true;
				player.onUpdateAbilities();

				ServerLevel level = world.getConnection().getServerLevel();
				for (int x = -3; x <= 6; x++) {
					for (int z = -3; z <= 3; z++) {
						level.setBlock(ORIGIN.offset(x, -1, z), Blocks.SMOOTH_STONE.defaultBlockState(), 3);
						for (int y = 0; y < 4; y++) {
							level.setBlock(ORIGIN.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
						}
					}
				}

				int x = 0;
				for (Block block : COMPRESSED) {
					level.setBlock(ORIGIN.offset(x, 0, 0), block.defaultBlockState(), 3);
					level.setBlock(ORIGIN.offset(x, 1, 0), block.defaultBlockState(), 3);
					x += 2;
				}

				player.getInventory().clearContent();
				int slot = 0;
				for (Item item : HOTBAR) {
					player.getInventory().setItem(slot++, new ItemStack(item));
				}

				player.teleportTo(
					level,
					ORIGIN.getX() + 3.0,
					ORIGIN.getY() + 1.5,
					ORIGIN.getZ() + 4.5,
					Set.of(),
					180.0F,
					20.0F,
					false
				);
			});
			world.getConnection().waitForClientboundPackets();
			world.getConnection().waitForChunksDownload();
			world.getConnection().waitForChunksRender();
			context.waitTicks(40);

			context.runOnClient(client -> assertItemModelsResolve(client));
			// Human visual check: compressed cubes should look solid (coal_block style), not item lumps.
			context.takeScreenshot("material_compressed_blocks");
		}
	}

	private static void assertItemModelsResolve(Minecraft client) {
		ItemModelResolver resolver = client.getItemModelResolver();
		ItemStackRenderState state = new ItemStackRenderState();
		for (Item item : HOTBAR) {
			ItemStack stack = new ItemStack(item);
			state.clear();
			resolver.updateForTopItem(state, stack, ItemDisplayContext.GUI, client.level, null, 0);
			if (state.isEmpty()) {
				throw new AssertionError("item model failed to resolve for " + item);
			}
		}
	}
}
