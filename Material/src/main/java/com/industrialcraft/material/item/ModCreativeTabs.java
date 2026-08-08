package com.industrialcraft.material.item;

import com.industrialcraft.material.MaterialMod;
import com.industrialcraft.material.block.ModBlocks;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public final class ModCreativeTabs {
	public static final ResourceKey<CreativeModeTab> BUILDING_BLOCKS_KEY = key("building_blocks");
	public static final ResourceKey<CreativeModeTab> INGREDIENTS_KEY = key("ingredients");
	public static final ResourceKey<CreativeModeTab> NATURAL_BLOCKS_KEY = key("natural_blocks");

	private ModCreativeTabs() {
	}

	public static void initialize() {
		Registry.register(
			BuiltInRegistries.CREATIVE_MODE_TAB,
			BUILDING_BLOCKS_KEY,
			FabricCreativeModeTab.builder()
				.title(Component.translatable("itemGroup.material.building_blocks"))
				.icon(() -> new ItemStack(ModBlocks.PEAT_BLOCK))
				.displayItems((params, output) -> {
					output.accept(ModBlocks.PEAT_BLOCK);
					output.accept(ModBlocks.LIGNITE_BLOCK);
					output.accept(ModBlocks.SUB_BITUMINOUS_BLOCK);
					output.accept(Blocks.COAL_BLOCK);
					output.accept(ModBlocks.ANTHRACITE_BLOCK);
				})
				.build()
		);

		Registry.register(
			BuiltInRegistries.CREATIVE_MODE_TAB,
			INGREDIENTS_KEY,
			FabricCreativeModeTab.builder()
				.title(Component.translatable("itemGroup.material.ingredients"))
				.icon(() -> new ItemStack(ModItems.PEAT))
				.displayItems((params, output) -> {
					output.accept(ModItems.PEAT);
					output.accept(ModItems.LIGNITE);
					output.accept(ModItems.SUB_BITUMINOUS);
					output.accept(Items.COAL);
					output.accept(Items.CHARCOAL);
					output.accept(ModItems.ANTHRACITE);
				})
				.build()
		);

		Registry.register(
			BuiltInRegistries.CREATIVE_MODE_TAB,
			NATURAL_BLOCKS_KEY,
			FabricCreativeModeTab.builder()
				.title(Component.translatable("itemGroup.material.natural_blocks"))
				.icon(() -> new ItemStack(ModBlocks.PEAT_ORE))
				.displayItems((params, output) -> {
					output.accept(ModBlocks.PEAT_ORE);
					output.accept(ModBlocks.DEEPSLATE_PEAT_ORE);
					output.accept(ModBlocks.LIGNITE_ORE);
					output.accept(ModBlocks.DEEPSLATE_LIGNITE_ORE);
					output.accept(ModBlocks.SUB_BITUMINOUS_ORE);
					output.accept(ModBlocks.DEEPSLATE_SUB_BITUMINOUS_ORE);
					output.accept(Blocks.COAL_ORE);
					output.accept(Blocks.DEEPSLATE_COAL_ORE);
					output.accept(ModBlocks.ANTHRACITE_ORE);
					output.accept(ModBlocks.DEEPSLATE_ANTHRACITE_ORE);
				})
				.build()
		);
	}

	private static ResourceKey<CreativeModeTab> key(String path) {
		return ResourceKey.create(Registries.CREATIVE_MODE_TAB, MaterialMod.id(path));
	}
}
