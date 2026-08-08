package com.industrialcraft.material.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record MixedCoalOreConfiguration(int size, float discardChanceOnAirExposure) implements FeatureConfiguration {
	public static final Codec<MixedCoalOreConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.intRange(0, 64).fieldOf("size").forGetter(MixedCoalOreConfiguration::size),
		Codec.floatRange(0.0F, 1.0F).fieldOf("discard_chance_on_air_exposure").forGetter(MixedCoalOreConfiguration::discardChanceOnAirExposure)
	).apply(instance, MixedCoalOreConfiguration::new));
}
