package com.industrialcraft.material.item;

import com.industrialcraft.material.MaterialMod;
import com.industrialcraft.material.block.ModBlocks;
import java.util.function.Function;
import net.fabricmc.fabric.api.registry.FuelValueEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class ModItems {
	public static final int PEAT_BURN_TIME = 400;
	public static final int LIGNITE_BURN_TIME = 800;
	public static final int SUB_BITUMINOUS_BURN_TIME = 1200;
	public static final int ANTHRACITE_BURN_TIME = 2000;

	public static final int PEAT_BLOCK_BURN_TIME = 4000;
	public static final int LIGNITE_BLOCK_BURN_TIME = 8000;
	public static final int SUB_BITUMINOUS_BLOCK_BURN_TIME = 12000;
	public static final int ANTHRACITE_BLOCK_BURN_TIME = 20000;

	public static final Item PEAT = register(ModItemIds.PEAT, Item::new, new Item.Properties());
	public static final Item LIGNITE = register(ModItemIds.LIGNITE, Item::new, new Item.Properties());
	public static final Item SUB_BITUMINOUS = register(ModItemIds.SUB_BITUMINOUS, Item::new, new Item.Properties());
	public static final Item ANTHRACITE = register(ModItemIds.ANTHRACITE, Item::new, new Item.Properties());

	private ModItems() {
	}

	public static Item register(ResourceKey<Item> itemKey, Function<Item.Properties, Item> itemFactory, Item.Properties settings) {
		Item item = itemFactory.apply(settings.setId(itemKey));
		return Registry.register(BuiltInRegistries.ITEM, itemKey, item);
	}

	public static void initialize() {
		FuelValueEvents.BUILD.register((builder, context) -> {
			builder.add(Items.CHARCOAL, MaterialMod.CHARCOAL_BURN_TIME);
			builder.add(PEAT, PEAT_BURN_TIME);
			builder.add(LIGNITE, LIGNITE_BURN_TIME);
			builder.add(SUB_BITUMINOUS, SUB_BITUMINOUS_BURN_TIME);
			builder.add(ANTHRACITE, ANTHRACITE_BURN_TIME);
			builder.add(ModBlocks.PEAT_BLOCK.asItem(), PEAT_BLOCK_BURN_TIME);
			builder.add(ModBlocks.LIGNITE_BLOCK.asItem(), LIGNITE_BLOCK_BURN_TIME);
			builder.add(ModBlocks.SUB_BITUMINOUS_BLOCK.asItem(), SUB_BITUMINOUS_BLOCK_BURN_TIME);
			builder.add(ModBlocks.ANTHRACITE_BLOCK.asItem(), ANTHRACITE_BLOCK_BURN_TIME);
		});
	}
}
