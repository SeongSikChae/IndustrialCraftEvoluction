package com.industrialcraft.machine.block;

import com.industrialcraft.machine.MachineMod;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;

public final class ModBlockItemIds {
	public static final BlockItemId FURNACE_ENGINE = create("furnace_engine");

	private ModBlockItemIds() {
	}

	private static BlockItemId create(String name) {
		Identifier id = MachineMod.id(name);
		return BlockItemId.create(id, id);
	}
}
