package com.industrialcraft.machine.recipe;

import com.industrialcraft.machine.MachineMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public final class ModRecipes {
	public static final RecipeType<MachineShapedRecipe> MACHINE_CRAFTING = Registry.register(
		BuiltInRegistries.RECIPE_TYPE,
		MachineMod.id("machine_crafting"),
		new RecipeType<>() {
			@Override
			public String toString() {
				return MachineMod.id("machine_crafting").toString();
			}
		}
	);

	public static final RecipeSerializer<MachineShapedRecipe> MACHINE_CRAFTING_SERIALIZER = Registry.register(
		BuiltInRegistries.RECIPE_SERIALIZER,
		MachineMod.id("machine_crafting"),
		new RecipeSerializer<>(MachineShapedRecipe.MAP_CODEC, MachineShapedRecipe.STREAM_CODEC)
	);

	private ModRecipes() {
	}

	public static void initialize() {
		// Static field registration.
	}
}
