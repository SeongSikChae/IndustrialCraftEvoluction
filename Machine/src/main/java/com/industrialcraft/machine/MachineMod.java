package com.industrialcraft.machine;

import com.industrialcraft.machine.block.ModBlocks;
import com.industrialcraft.machine.block.entity.ModBlockEntities;
import com.industrialcraft.machine.menu.ModMenus;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MachineMod implements ModInitializer {
	public static final String MOD_ID = "machine";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModBlocks.initialize();
		ModBlockEntities.initialize();
		ModMenus.initialize();
		LOGGER.info("Machine initialized");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
