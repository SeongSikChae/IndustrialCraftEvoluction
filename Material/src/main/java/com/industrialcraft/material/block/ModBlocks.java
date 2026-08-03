package com.industrialcraft.material.block;

import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocks {
	public static final Block PEAT_ORE = registerOre(ModBlockItemIds.PEAT_ORE);
	public static final Block DEEPSLATE_PEAT_ORE = registerDeepslateOre(ModBlockItemIds.DEEPSLATE_PEAT_ORE, PEAT_ORE);
	public static final Block LIGNITE_ORE = registerOre(ModBlockItemIds.LIGNITE_ORE);
	public static final Block DEEPSLATE_LIGNITE_ORE = registerDeepslateOre(ModBlockItemIds.DEEPSLATE_LIGNITE_ORE, LIGNITE_ORE);
	public static final Block SUB_BITUMINOUS_ORE = registerOre(ModBlockItemIds.SUB_BITUMINOUS_ORE);
	public static final Block DEEPSLATE_SUB_BITUMINOUS_ORE = registerDeepslateOre(ModBlockItemIds.DEEPSLATE_SUB_BITUMINOUS_ORE, SUB_BITUMINOUS_ORE);
	public static final Block ANTHRACITE_ORE = registerOre(ModBlockItemIds.ANTHRACITE_ORE);
	public static final Block DEEPSLATE_ANTHRACITE_ORE = registerDeepslateOre(ModBlockItemIds.DEEPSLATE_ANTHRACITE_ORE, ANTHRACITE_ORE);

	private ModBlocks() {
	}

	private static Block registerOre(BlockItemId id) {
		return register(
			id,
			properties -> new DropExperienceBlock(UniformInt.of(0, 2), properties),
			BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_ORE)
		);
	}

	private static Block registerDeepslateOre(BlockItemId id, Block base) {
		return register(
			id,
			properties -> new DropExperienceBlock(UniformInt.of(0, 2), properties),
			BlockBehaviour.Properties.ofFullCopy(base)
				.mapColor(MapColor.DEEPSLATE)
				.strength(4.5F, 3.0F)
				.sound(SoundType.DEEPSLATE)
		);
	}

	private static Block register(BlockItemId id, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
		Block block = register(id.block(), factory, properties);
		Item.Properties itemProperties = new Item.Properties().useBlockDescriptionPrefix().setId(id.item());
		Registry.register(BuiltInRegistries.ITEM, id.item(), new BlockItem(block, itemProperties));
		return block;
	}

	private static Block register(ResourceKey<Block> id, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
		Block block = factory.apply(properties.setId(id));
		return Registry.register(BuiltInRegistries.BLOCK, id, block);
	}

	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS)
			.register(entries -> {
				entries.accept(PEAT_ORE);
				entries.accept(DEEPSLATE_PEAT_ORE);
				entries.accept(LIGNITE_ORE);
				entries.accept(DEEPSLATE_LIGNITE_ORE);
				entries.accept(SUB_BITUMINOUS_ORE);
				entries.accept(DEEPSLATE_SUB_BITUMINOUS_ORE);
				entries.accept(ANTHRACITE_ORE);
				entries.accept(DEEPSLATE_ANTHRACITE_ORE);
			});
	}
}
