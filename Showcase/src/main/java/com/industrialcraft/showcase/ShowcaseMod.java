package com.industrialcraft.showcase;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tooling-only mod that pulls Material (and later Machine) into client gametests
 * for documentation showcase clips.
 */
public class ShowcaseMod implements ModInitializer {
	public static final String MOD_ID = "showcase";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Showcase tooling initialized");
	}
}
