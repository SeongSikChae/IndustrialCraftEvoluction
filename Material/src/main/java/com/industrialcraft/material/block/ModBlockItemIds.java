package com.industrialcraft.material.block;

import com.industrialcraft.material.MaterialMod;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;

public final class ModBlockItemIds {
	public static final BlockItemId PEAT_ORE = create("peat_ore");
	public static final BlockItemId DEEPSLATE_PEAT_ORE = create("deepslate_peat_ore");
	public static final BlockItemId LIGNITE_ORE = create("lignite_ore");
	public static final BlockItemId DEEPSLATE_LIGNITE_ORE = create("deepslate_lignite_ore");
	public static final BlockItemId SUB_BITUMINOUS_ORE = create("sub_bituminous_ore");
	public static final BlockItemId DEEPSLATE_SUB_BITUMINOUS_ORE = create("deepslate_sub_bituminous_ore");
	public static final BlockItemId ANTHRACITE_ORE = create("anthracite_ore");
	public static final BlockItemId DEEPSLATE_ANTHRACITE_ORE = create("deepslate_anthracite_ore");

	private ModBlockItemIds() {
	}

	private static BlockItemId create(String name) {
		Identifier id = MaterialMod.id(name);
		return BlockItemId.create(id, id);
	}
}
