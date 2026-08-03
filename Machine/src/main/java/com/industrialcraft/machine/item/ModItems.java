package com.industrialcraft.machine.item;

import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class ModItems {
	/** Rigid shaft+sprocket mesh used only by the furnace engine BER. */
	public static final Item FURNACE_ENGINE_GEAR = register(ModItemIds.FURNACE_ENGINE_GEAR, Item::new, new Item.Properties());

	private ModItems() {
	}

	public static Item register(ResourceKey<Item> itemKey, Function<Item.Properties, Item> itemFactory, Item.Properties settings) {
		Item item = itemFactory.apply(settings.setId(itemKey));
		return Registry.register(BuiltInRegistries.ITEM, itemKey, item);
	}

	public static void initialize() {
	}
}
