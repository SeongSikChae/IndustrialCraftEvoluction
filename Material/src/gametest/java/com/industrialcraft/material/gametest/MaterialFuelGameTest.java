package com.industrialcraft.material.gametest;

import com.industrialcraft.material.MaterialMod;
import com.industrialcraft.material.block.ModBlocks;
import com.industrialcraft.material.item.ModItems;
import com.industrialcraft.material.power.FuelDurations;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class MaterialFuelGameTest {
	@GameTest
	public void furnaceBurnTimesMatchSpec(GameTestHelper helper) {
		var fuels = helper.getLevel().fuelValues();
		helper.assertTrue(fuels.burnDuration(new ItemStack(ModItems.PEAT)) == ModItems.PEAT_BURN_TIME, "peat burn");
		helper.assertTrue(fuels.burnDuration(new ItemStack(ModItems.LIGNITE)) == ModItems.LIGNITE_BURN_TIME, "lignite burn");
		helper.assertTrue(fuels.burnDuration(new ItemStack(ModItems.SUB_BITUMINOUS)) == ModItems.SUB_BITUMINOUS_BURN_TIME, "sub_bituminous burn");
		helper.assertTrue(fuels.burnDuration(new ItemStack(Items.CHARCOAL)) == MaterialMod.CHARCOAL_BURN_TIME, "charcoal burn");
		helper.assertTrue(fuels.burnDuration(new ItemStack(Items.COAL)) == 1600, "coal burn");
		helper.assertTrue(fuels.burnDuration(new ItemStack(ModItems.ANTHRACITE)) == ModItems.ANTHRACITE_BURN_TIME, "anthracite burn");
		helper.assertTrue(fuels.burnDuration(new ItemStack(ModBlocks.PEAT_BLOCK)) == ModItems.PEAT_BLOCK_BURN_TIME, "peat block burn");
		helper.assertTrue(fuels.burnDuration(new ItemStack(ModBlocks.LIGNITE_BLOCK)) == ModItems.LIGNITE_BLOCK_BURN_TIME, "lignite block burn");
		helper.assertTrue(fuels.burnDuration(new ItemStack(ModBlocks.SUB_BITUMINOUS_BLOCK)) == ModItems.SUB_BITUMINOUS_BLOCK_BURN_TIME, "sub_bituminous block burn");
		helper.assertTrue(fuels.burnDuration(new ItemStack(Blocks.COAL_BLOCK)) == 16000, "coal block burn");
		helper.assertTrue(fuels.burnDuration(new ItemStack(ModBlocks.ANTHRACITE_BLOCK)) == ModItems.ANTHRACITE_BLOCK_BURN_TIME, "anthracite block burn");
		helper.assertTrue(fuels.burnDuration(new ItemStack(ModBlocks.PEAT_ORE)) == 0, "peat ore not furnace fuel");
		helper.succeed();
	}

	@GameTest
	public void minecartFuelScalesFromFurnaceBurn(GameTestHelper helper) {
		var level = helper.getLevel();
		helper.assertTrue(FuelDurations.minecartStyleFuelTicks(level, new ItemStack(ModItems.PEAT)) == 900, "peat minecart ticks");
		helper.assertTrue(FuelDurations.minecartStyleFuelTicks(level, new ItemStack(ModItems.LIGNITE)) == 1800, "lignite minecart ticks");
		helper.assertTrue(FuelDurations.minecartStyleFuelTicks(level, new ItemStack(ModItems.SUB_BITUMINOUS)) == 2700, "sub_bituminous minecart ticks");
		helper.assertTrue(FuelDurations.minecartStyleFuelTicks(level, new ItemStack(Items.CHARCOAL)) == 3240, "charcoal minecart ticks");
		helper.assertTrue(FuelDurations.minecartStyleFuelTicks(level, new ItemStack(Items.COAL)) == 3600, "coal minecart ticks");
		helper.assertTrue(FuelDurations.minecartStyleFuelTicks(level, new ItemStack(ModItems.ANTHRACITE)) == 4500, "anthracite minecart ticks");
		helper.assertTrue(FuelDurations.minecartStyleFuelTicks(level, new ItemStack(ModBlocks.PEAT_BLOCK)) == 9000, "peat block scaled ticks");
		helper.assertTrue(FuelDurations.minecartStyleFuelTicks(level, new ItemStack(Items.IRON_INGOT)) == 0, "non-fuel returns 0");
		helper.succeed();
	}

	@GameTest
	public void furnaceMinecartFuelTagMatchesSpec(GameTestHelper helper) {
		helper.assertTrue(new ItemStack(ModItems.PEAT).is(ItemTags.FURNACE_MINECART_FUEL), "peat tagged");
		helper.assertTrue(new ItemStack(ModItems.LIGNITE).is(ItemTags.FURNACE_MINECART_FUEL), "lignite tagged");
		helper.assertTrue(new ItemStack(ModItems.SUB_BITUMINOUS).is(ItemTags.FURNACE_MINECART_FUEL), "sub_bituminous tagged");
		helper.assertTrue(new ItemStack(ModItems.ANTHRACITE).is(ItemTags.FURNACE_MINECART_FUEL), "anthracite tagged");
		helper.assertTrue(new ItemStack(Items.COAL).is(ItemTags.FURNACE_MINECART_FUEL), "coal tagged");
		helper.assertTrue(new ItemStack(Items.CHARCOAL).is(ItemTags.FURNACE_MINECART_FUEL), "charcoal tagged");
		helper.assertTrue(!new ItemStack(ModBlocks.PEAT_BLOCK).is(ItemTags.FURNACE_MINECART_FUEL), "peat_block not minecart fuel");
		helper.assertTrue(!new ItemStack(ModBlocks.LIGNITE_BLOCK).is(ItemTags.FURNACE_MINECART_FUEL), "lignite_block not minecart fuel");
		helper.assertTrue(!new ItemStack(ModBlocks.SUB_BITUMINOUS_BLOCK).is(ItemTags.FURNACE_MINECART_FUEL), "sub_bituminous_block not minecart fuel");
		helper.assertTrue(!new ItemStack(ModBlocks.ANTHRACITE_BLOCK).is(ItemTags.FURNACE_MINECART_FUEL), "anthracite_block not minecart fuel");
		helper.assertTrue(!new ItemStack(Blocks.COAL_BLOCK).is(ItemTags.FURNACE_MINECART_FUEL), "coal_block not minecart fuel");
		helper.succeed();
	}

	@GameTest
	public void coalsTagIncludesMaterialFuels(GameTestHelper helper) {
		helper.assertTrue(new ItemStack(ModItems.PEAT).is(ItemTags.COALS), "peat in #coals");
		helper.assertTrue(new ItemStack(ModItems.LIGNITE).is(ItemTags.COALS), "lignite in #coals");
		helper.assertTrue(new ItemStack(ModItems.SUB_BITUMINOUS).is(ItemTags.COALS), "sub_bituminous in #coals");
		helper.assertTrue(new ItemStack(ModItems.ANTHRACITE).is(ItemTags.COALS), "anthracite in #coals");
		helper.succeed();
	}

	@GameTest
	public void coalOresTagIncludesMaterialOres(GameTestHelper helper) {
		var blockCoalOres = net.minecraft.tags.TagKey.create(
			Registries.BLOCK,
			Identifier.withDefaultNamespace("coal_ores")
		);
		var itemCoalOres = net.minecraft.tags.TagKey.create(
			Registries.ITEM,
			Identifier.withDefaultNamespace("coal_ores")
		);

		helper.assertTrue(ModBlocks.PEAT_ORE.defaultBlockState().is(blockCoalOres), "peat_ore block tag");
		helper.assertTrue(ModBlocks.DEEPSLATE_PEAT_ORE.defaultBlockState().is(blockCoalOres), "deepslate_peat_ore block tag");
		helper.assertTrue(ModBlocks.LIGNITE_ORE.defaultBlockState().is(blockCoalOres), "lignite_ore block tag");
		helper.assertTrue(ModBlocks.DEEPSLATE_LIGNITE_ORE.defaultBlockState().is(blockCoalOres), "deepslate_lignite_ore block tag");
		helper.assertTrue(ModBlocks.SUB_BITUMINOUS_ORE.defaultBlockState().is(blockCoalOres), "sub_bituminous_ore block tag");
		helper.assertTrue(ModBlocks.DEEPSLATE_SUB_BITUMINOUS_ORE.defaultBlockState().is(blockCoalOres), "deepslate_sub_bituminous_ore block tag");
		helper.assertTrue(ModBlocks.ANTHRACITE_ORE.defaultBlockState().is(blockCoalOres), "anthracite_ore block tag");
		helper.assertTrue(ModBlocks.DEEPSLATE_ANTHRACITE_ORE.defaultBlockState().is(blockCoalOres), "deepslate_anthracite_ore block tag");

		helper.assertTrue(new ItemStack(ModBlocks.PEAT_ORE).is(itemCoalOres), "peat_ore item tag");
		helper.assertTrue(new ItemStack(ModBlocks.ANTHRACITE_ORE).is(itemCoalOres), "anthracite_ore item tag");
		helper.succeed();
	}
}
