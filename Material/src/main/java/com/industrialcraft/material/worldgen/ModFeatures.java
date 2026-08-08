package com.industrialcraft.material.worldgen;

import com.industrialcraft.material.MaterialMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class ModFeatures {
	public static final Feature<MixedCoalOreConfiguration> MIXED_COAL_ORE = Registry.register(
		BuiltInRegistries.FEATURE,
		MaterialMod.id("mixed_coal_ore"),
		new MixedCoalOreFeature(MixedCoalOreConfiguration.CODEC)
	);

	private ModFeatures() {
	}

	public static void initialize() {
		// Registration side effect via field init.
	}
}
