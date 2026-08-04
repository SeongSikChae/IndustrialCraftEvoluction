package com.industrialcraft.machine;

import com.industrialcraft.machine.block.DynamoOrientationVerifier;
import com.industrialcraft.machine.block.FurnaceEngineOrientationVerifier;
import com.industrialcraft.machine.block.ModBlocks;
import com.industrialcraft.machine.block.entity.ModBlockEntities;
import com.industrialcraft.machine.item.ModItems;
import com.industrialcraft.machine.menu.ModMenus;
import com.industrialcraft.machine.recipe.ModRecipeBookCategories;
import com.industrialcraft.machine.recipe.ModRecipes;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MachineMod implements ModInitializer {
	public static final String MOD_ID = "machine";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModRecipes.initialize();
		ModRecipeBookCategories.initialize();
		ModItems.initialize();
		ModBlocks.initialize();
		ModBlockEntities.initialize();
		ModMenus.initialize();
		FurnaceEngineOrientationVerifier.verifyOrThrow();
		DynamoOrientationVerifier.verifyOrThrow();
		LOGGER.info("Machine initialized");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
