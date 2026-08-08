package com.industrialcraft.material.gametest;

import com.industrialcraft.material.block.ModBlocks;
import com.industrialcraft.material.item.ModCreativeTabs;
import com.industrialcraft.material.item.ModItems;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class MaterialCreativeTabGameTest {
	@GameTest
	public void buildingBlocksTabContainsFuelBlocks(GameTestHelper helper) {
		Set<Item> items = displayItems(helper, ModCreativeTabs.BUILDING_BLOCKS_KEY);
		helper.assertTrue(items.contains(ModBlocks.PEAT_BLOCK.asItem()), "peat_block in building tab");
		helper.assertTrue(items.contains(ModBlocks.LIGNITE_BLOCK.asItem()), "lignite_block in building tab");
		helper.assertTrue(items.contains(ModBlocks.SUB_BITUMINOUS_BLOCK.asItem()), "sub_bituminous_block in building tab");
		helper.assertTrue(items.contains(Items.COAL_BLOCK), "coal_block in building tab");
		helper.assertTrue(items.contains(ModBlocks.ANTHRACITE_BLOCK.asItem()), "anthracite_block in building tab");
		helper.succeed();
	}

	@GameTest
	public void ingredientsTabContainsFuels(GameTestHelper helper) {
		Set<Item> items = displayItems(helper, ModCreativeTabs.INGREDIENTS_KEY);
		helper.assertTrue(items.contains(ModItems.PEAT), "peat in ingredients tab");
		helper.assertTrue(items.contains(ModItems.LIGNITE), "lignite in ingredients tab");
		helper.assertTrue(items.contains(ModItems.SUB_BITUMINOUS), "sub_bituminous in ingredients tab");
		helper.assertTrue(items.contains(Items.COAL), "coal in ingredients tab");
		helper.assertTrue(items.contains(Items.CHARCOAL), "charcoal in ingredients tab");
		helper.assertTrue(items.contains(ModItems.ANTHRACITE), "anthracite in ingredients tab");
		helper.succeed();
	}

	@GameTest
	public void naturalBlocksTabContainsOres(GameTestHelper helper) {
		Set<Item> items = displayItems(helper, ModCreativeTabs.NATURAL_BLOCKS_KEY);
		helper.assertTrue(items.contains(ModBlocks.PEAT_ORE.asItem()), "peat_ore in natural tab");
		helper.assertTrue(items.contains(ModBlocks.DEEPSLATE_PEAT_ORE.asItem()), "deepslate_peat_ore in natural tab");
		helper.assertTrue(items.contains(ModBlocks.LIGNITE_ORE.asItem()), "lignite_ore in natural tab");
		helper.assertTrue(items.contains(ModBlocks.DEEPSLATE_LIGNITE_ORE.asItem()), "deepslate_lignite_ore in natural tab");
		helper.assertTrue(items.contains(ModBlocks.SUB_BITUMINOUS_ORE.asItem()), "sub_bituminous_ore in natural tab");
		helper.assertTrue(items.contains(ModBlocks.DEEPSLATE_SUB_BITUMINOUS_ORE.asItem()), "deepslate_sub_bituminous_ore in natural tab");
		helper.assertTrue(items.contains(Items.COAL_ORE), "coal_ore in natural tab");
		helper.assertTrue(items.contains(Items.DEEPSLATE_COAL_ORE), "deepslate_coal_ore in natural tab");
		helper.assertTrue(items.contains(ModBlocks.ANTHRACITE_ORE.asItem()), "anthracite_ore in natural tab");
		helper.assertTrue(items.contains(ModBlocks.DEEPSLATE_ANTHRACITE_ORE.asItem()), "deepslate_anthracite_ore in natural tab");
		helper.succeed();
	}

	private static Set<Item> displayItems(GameTestHelper helper, net.minecraft.resources.ResourceKey<CreativeModeTab> key) {
		CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.getValue(key);
		helper.assertTrue(tab != null, key.identifier() + " tab registered");
		CreativeModeTab.ItemDisplayParameters params = new CreativeModeTab.ItemDisplayParameters(
			helper.getLevel().enabledFeatures(),
			true,
			helper.getLevel().registryAccess()
		);
		tab.buildContents(params);
		Collection<ItemStack> stacks = tab.getDisplayItems();
		helper.assertTrue(!stacks.isEmpty(), key.identifier() + " tab not empty");
		return stacks.stream().map(ItemStack::getItem).collect(Collectors.toSet());
	}
}
