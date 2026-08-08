package com.industrialcraft.machine.item;

import com.industrialcraft.machine.MachineMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class ModItemIds {
	/** Shared shaft+sprocket mesh for engine/dynamo BER (not a craftable/tab item). */
	public static final ResourceKey<Item> SHAFT_GEAR = create("shaft_gear");
	public static final ResourceKey<Item> GOVERNOR_ACCESSORY = create("governor_accessory");
	public static final ResourceKey<Item> CHECK_VALVE_ACCESSORY = create("check_valve_accessory");

	private ModItemIds() {
	}

	public static ResourceKey<Item> create(String name) {
		return ResourceKey.create(Registries.ITEM, MachineMod.id(name));
	}
}
