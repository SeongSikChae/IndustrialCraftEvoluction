package com.industrialcraft.material;

import com.industrialcraft.material.block.ModBlocks;
import com.industrialcraft.material.item.ModCreativeTabs;
import com.industrialcraft.material.item.ModItems;
import com.industrialcraft.material.worldgen.ModFeatures;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaterialMod implements ModInitializer {
	public static final String MOD_ID = "material";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/** Vanilla charcoal is 1600; Material sets it to 90% of bituminous (1440). */
	public static final int CHARCOAL_BURN_TIME = 1440;

	@Override
	public void onInitialize() {
		ModFeatures.initialize();
		ModBlocks.initialize();
		ModItems.initialize();
		ModCreativeTabs.initialize();
		LOGGER.info("Material initialized");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
