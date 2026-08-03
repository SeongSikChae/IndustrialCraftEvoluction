package com.industrialcraft.machine.recipe;

import com.industrialcraft.machine.MachineMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeBookCategory;

public final class ModRecipeBookCategories {
	public static final RecipeBookCategory MACHINE = Registry.register(
		BuiltInRegistries.RECIPE_BOOK_CATEGORY,
		MachineMod.id("machine"),
		new RecipeBookCategory()
	);

	private ModRecipeBookCategories() {
	}

	public static void initialize() {
		// Static field registration.
	}
}
