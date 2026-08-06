package com.industrialcraft.machine.block;

import com.industrialcraft.machine.MachineMod;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;

public final class ModBlockItemIds {
	public static final BlockItemId MACHINE_CRAFTING_TABLE = create("machine_crafting_table");
	public static final BlockItemId FURNACE_ENGINE = create("furnace_engine");
	public static final BlockItemId DYNAMO = create("dynamo");
	public static final BlockItemId RESERVOIR = create("reservoir");
	public static final BlockItemId FLUID_PIPE = create("fluid_pipe");
	public static final BlockItemId RAIN_COLLECTOR = create("rain_collector");

	private ModBlockItemIds() {
	}

	private static BlockItemId create(String name) {
		Identifier id = MachineMod.id(name);
		return BlockItemId.create(id, id);
	}
}
