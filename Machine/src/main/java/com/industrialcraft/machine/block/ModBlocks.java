package com.industrialcraft.machine.block;

import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {
	public static final Block MACHINE_CRAFTING_TABLE = register(
		ModBlockItemIds.MACHINE_CRAFTING_TABLE,
		MachineCraftingTableBlock::new,
		BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)
	);
	public static final Block FURNACE_ENGINE = register(
		ModBlockItemIds.FURNACE_ENGINE,
		FurnaceEngineBlock::new,
		BlockBehaviour.Properties.ofFullCopy(Blocks.FURNACE).noOcclusion()
	);
	public static final Block DYNAMO = register(
		ModBlockItemIds.DYNAMO,
		DynamoBlock::new,
		BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion()
	);
	public static final Block RESERVOIR = register(
		ModBlockItemIds.RESERVOIR,
		ReservoirBlock::new,
		BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion()
	);
	public static final Block FLUID_PIPE = register(
		ModBlockItemIds.FLUID_PIPE,
		FluidPipeBlock::new,
		BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).noOcclusion().strength(0.5F)
	);

	private ModBlocks() {
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
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
			.register(entries -> {
				entries.accept(MACHINE_CRAFTING_TABLE);
				entries.accept(FURNACE_ENGINE);
				entries.accept(DYNAMO);
				entries.accept(RESERVOIR);
				entries.accept(FLUID_PIPE);
			});
	}
}
