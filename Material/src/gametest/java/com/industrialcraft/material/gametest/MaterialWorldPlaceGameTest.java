package com.industrialcraft.material.gametest;

import com.industrialcraft.material.block.ModBlocks;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;

public class MaterialWorldPlaceGameTest {
	@GameTest
	public void placeAllMaterialOresAndFuelBlocks(GameTestHelper helper) {
		Block[] blocks = {
			ModBlocks.PEAT_ORE,
			ModBlocks.DEEPSLATE_PEAT_ORE,
			ModBlocks.LIGNITE_ORE,
			ModBlocks.DEEPSLATE_LIGNITE_ORE,
			ModBlocks.SUB_BITUMINOUS_ORE,
			ModBlocks.DEEPSLATE_SUB_BITUMINOUS_ORE,
			ModBlocks.ANTHRACITE_ORE,
			ModBlocks.DEEPSLATE_ANTHRACITE_ORE,
			ModBlocks.PEAT_BLOCK,
			ModBlocks.LIGNITE_BLOCK,
			ModBlocks.SUB_BITUMINOUS_BLOCK,
			ModBlocks.ANTHRACITE_BLOCK
		};

		int i = 0;
		for (Block block : blocks) {
			BlockPos pos = new BlockPos(i % 4, 1, i / 4);
			helper.setBlock(pos, block);
			helper.assertBlockPresent(block, pos);
			helper.assertTrue(helper.getBlockState(pos).is(block), block + " state after place");
			i++;
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void placedPeatBlockSurvivesTicks(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, ModBlocks.PEAT_BLOCK);
		helper.runAfterDelay(20, () -> {
			helper.assertBlockPresent(ModBlocks.PEAT_BLOCK, pos);
			helper.succeed();
		});
	}
}
