package com.industrialcraft.machine.item;

import com.industrialcraft.machine.MachineMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class ModItemIds {
	public static final ResourceKey<Item> FURNACE_ENGINE_GEAR = create("furnace_engine_gear");
	public static final ResourceKey<Item> GOVERNOR_ACCESSORY = create("governor_accessory");

	private ModItemIds() {
	}

	public static ResourceKey<Item> create(String name) {
		return ResourceKey.create(Registries.ITEM, MachineMod.id(name));
	}
}
