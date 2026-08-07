package com.industrialcraft.showcase.gametest;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Spec for a fuel-grade showcase clip: floor-dropped item + ore + deepslate ore.
 */
public record FuelGradeSpec(
	String clipId,
	Item item,
	Block ore,
	Block deepslateOre,
	int burnTicks,
	String itemTitle,
	String oreTitle,
	String deepslateOreTitle
) {
	public String fuelSubtitle() {
		int seconds = burnTicks / 20;
		return "화로 기준 연료 · " + burnTicks + "틱 (" + seconds + "초)";
	}
}
