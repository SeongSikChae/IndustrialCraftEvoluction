package com.industrialcraft.machine.item;

import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class ModItems {
	/** Shared shaft+sprocket mesh for engine/dynamo BER (not a craftable/tab item). */
	public static final Item SHAFT_GEAR = register(ModItemIds.SHAFT_GEAR, Item::new, new Item.Properties());
	public static final Item GOVERNOR_ACCESSORY = register(ModItemIds.GOVERNOR_ACCESSORY, Item::new, new Item.Properties());
	public static final Item CHECK_VALVE_ACCESSORY = register(ModItemIds.CHECK_VALVE_ACCESSORY, Item::new, new Item.Properties());

	private ModItems() {
	}

	public static Item register(ResourceKey<Item> itemKey, Function<Item.Properties, Item> itemFactory, Item.Properties settings) {
		Item item = itemFactory.apply(settings.setId(itemKey));
		return Registry.register(BuiltInRegistries.ITEM, itemKey, item);
	}

	public static void initialize() {
		// Creative tabs filled by ModCreativeTabs.
	}
}
