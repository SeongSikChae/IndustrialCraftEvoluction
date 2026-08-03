package com.industrialcraft.material.item;

import com.industrialcraft.material.MaterialMod;
import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.registry.FuelValueEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class ModItems {
	public static final int PEAT_BURN_TIME = 400;
	public static final int LIGNITE_BURN_TIME = 800;
	public static final int SUB_BITUMINOUS_BURN_TIME = 1200;
	public static final int ANTHRACITE_BURN_TIME = 2000;

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
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
			.register(entries -> {
				entries.accept(PEAT);
				entries.accept(LIGNITE);
				entries.accept(SUB_BITUMINOUS);
				entries.accept(ANTHRACITE);
			});

		FuelValueEvents.BUILD.register((builder, context) -> {
			builder.add(Items.CHARCOAL, MaterialMod.CHARCOAL_BURN_TIME);
			builder.add(PEAT, PEAT_BURN_TIME);
			builder.add(LIGNITE, LIGNITE_BURN_TIME);
			builder.add(SUB_BITUMINOUS, SUB_BITUMINOUS_BURN_TIME);
			builder.add(ANTHRACITE, ANTHRACITE_BURN_TIME);
		});
	}
}
