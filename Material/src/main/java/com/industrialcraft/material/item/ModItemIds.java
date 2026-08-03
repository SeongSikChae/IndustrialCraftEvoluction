package com.industrialcraft.material.item;

import com.industrialcraft.material.MaterialMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class ModItemIds {
	public static final ResourceKey<Item> PEAT = create("peat");
	public static final ResourceKey<Item> LIGNITE = create("lignite");
	public static final ResourceKey<Item> SUB_BITUMINOUS = create("sub_bituminous");
	public static final ResourceKey<Item> ANTHRACITE = create("anthracite");

	private ModItemIds() {
	}

	public static ResourceKey<Item> create(String name) {
		return ResourceKey.create(Registries.ITEM, MaterialMod.id(name));
	}
}
