package com.industrialcraft.material.gametest;

import com.industrialcraft.material.block.ModBlocks;
import com.industrialcraft.material.worldgen.MixedCoalOreFeature;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class MixedCoalOreGameTest {
	@GameTest
	public void pickOreWeightsApproximateSpec(GameTestHelper helper) {
		RandomSource random = RandomSource.create(42L);
		int peat = 0;
		int lignite = 0;
		int sub = 0;
		int bituminous = 0;
		int anthracite = 0;
		int samples = 10000;
		for (int i = 0; i < samples; i++) {
			BlockState state = MixedCoalOreFeature.pickOre(random, false);
			if (state.is(ModBlocks.PEAT_ORE)) {
				peat++;
			} else if (state.is(ModBlocks.LIGNITE_ORE)) {
				lignite++;
			} else if (state.is(ModBlocks.SUB_BITUMINOUS_ORE)) {
				sub++;
			} else if (state.is(Blocks.COAL_ORE)) {
				bituminous++;
			} else if (state.is(ModBlocks.ANTHRACITE_ORE)) {
				anthracite++;
			} else {
				helper.fail("unexpected ore " + state);
				return;
			}
		}
		// Expect ~50/10/17.5/20/2.5 — allow ±3% absolute for this fixed seed.
		helper.assertTrue(Math.abs(peat / (double) samples - 0.50) < 0.03, "peat ~50%");
		helper.assertTrue(Math.abs(lignite / (double) samples - 0.10) < 0.03, "lignite ~10%");
		helper.assertTrue(Math.abs(sub / (double) samples - 0.175) < 0.03, "sub ~17.5%");
		helper.assertTrue(Math.abs(bituminous / (double) samples - 0.20) < 0.03, "bituminous ~20%");
		helper.assertTrue(Math.abs(anthracite / (double) samples - 0.025) < 0.03, "anthracite ~2.5%");
		helper.succeed();
	}
}
