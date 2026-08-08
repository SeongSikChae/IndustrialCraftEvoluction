package com.industrialcraft.material.gametest;

import com.industrialcraft.material.MaterialMod;
import com.industrialcraft.material.block.ModBlocks;
import com.industrialcraft.material.item.ModItems;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class MaterialContentGameTest {
	@GameTest
	public void fuelItemsAreRegistered(GameTestHelper helper) {
		assertItem(helper, "peat", ModItems.PEAT);
		assertItem(helper, "lignite", ModItems.LIGNITE);
		assertItem(helper, "sub_bituminous", ModItems.SUB_BITUMINOUS);
		assertItem(helper, "anthracite", ModItems.ANTHRACITE);
		helper.succeed();
	}

	@GameTest
	public void oreAndFuelBlocksAreRegistered(GameTestHelper helper) {
		assertBlock(helper, "peat_ore", ModBlocks.PEAT_ORE);
		assertBlock(helper, "deepslate_peat_ore", ModBlocks.DEEPSLATE_PEAT_ORE);
		assertBlock(helper, "lignite_ore", ModBlocks.LIGNITE_ORE);
		assertBlock(helper, "deepslate_lignite_ore", ModBlocks.DEEPSLATE_LIGNITE_ORE);
		assertBlock(helper, "sub_bituminous_ore", ModBlocks.SUB_BITUMINOUS_ORE);
		assertBlock(helper, "deepslate_sub_bituminous_ore", ModBlocks.DEEPSLATE_SUB_BITUMINOUS_ORE);
		assertBlock(helper, "anthracite_ore", ModBlocks.ANTHRACITE_ORE);
		assertBlock(helper, "deepslate_anthracite_ore", ModBlocks.DEEPSLATE_ANTHRACITE_ORE);
		assertBlock(helper, "peat_block", ModBlocks.PEAT_BLOCK);
		assertBlock(helper, "lignite_block", ModBlocks.LIGNITE_BLOCK);
		assertBlock(helper, "sub_bituminous_block", ModBlocks.SUB_BITUMINOUS_BLOCK);
		assertBlock(helper, "anthracite_block", ModBlocks.ANTHRACITE_BLOCK);
		helper.succeed();
	}

	@GameTest
	public void oreAndFuelBlockItemsAreRegistered(GameTestHelper helper) {
		assertItem(helper, "peat_ore", ModBlocks.PEAT_ORE.asItem());
		assertItem(helper, "deepslate_peat_ore", ModBlocks.DEEPSLATE_PEAT_ORE.asItem());
		assertItem(helper, "lignite_ore", ModBlocks.LIGNITE_ORE.asItem());
		assertItem(helper, "deepslate_lignite_ore", ModBlocks.DEEPSLATE_LIGNITE_ORE.asItem());
		assertItem(helper, "sub_bituminous_ore", ModBlocks.SUB_BITUMINOUS_ORE.asItem());
		assertItem(helper, "deepslate_sub_bituminous_ore", ModBlocks.DEEPSLATE_SUB_BITUMINOUS_ORE.asItem());
		assertItem(helper, "anthracite_ore", ModBlocks.ANTHRACITE_ORE.asItem());
		assertItem(helper, "deepslate_anthracite_ore", ModBlocks.DEEPSLATE_ANTHRACITE_ORE.asItem());
		assertItem(helper, "peat_block", ModBlocks.PEAT_BLOCK.asItem());
		assertItem(helper, "lignite_block", ModBlocks.LIGNITE_BLOCK.asItem());
		assertItem(helper, "sub_bituminous_block", ModBlocks.SUB_BITUMINOUS_BLOCK.asItem());
		assertItem(helper, "anthracite_block", ModBlocks.ANTHRACITE_BLOCK.asItem());
		helper.succeed();
	}

	private static void assertItem(GameTestHelper helper, String path, Item expected) {
		Item registered = BuiltInRegistries.ITEM.getValue(MaterialMod.id(path));
		helper.assertTrue(registered == expected, path + " item registry");
	}

	private static void assertBlock(GameTestHelper helper, String path, Block expected) {
		Block registered = BuiltInRegistries.BLOCK.getValue(MaterialMod.id(path));
		helper.assertTrue(registered == expected, path + " block registry");
	}
}
