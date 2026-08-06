package com.industrialcraft.showcase.gametest;

import com.industrialcraft.material.block.ModBlocks;
import com.industrialcraft.material.item.ModItems;
import java.util.List;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * Fuel-grade showcase specs (peat + coal-rank series).
 */
public final class FuelGradeSpecs {
	public static final FuelGradeSpec PEAT = new FuelGradeSpec(
		"peat",
		ModItems.PEAT,
		ModBlocks.PEAT_ORE,
		ModBlocks.DEEPSLATE_PEAT_ORE,
		ModItems.PEAT_BURN_TIME,
		"이탄",
		"이탄 광석",
		"심층암 이탄 광석"
	);

	public static final FuelGradeSpec LIGNITE = new FuelGradeSpec(
		"lignite",
		ModItems.LIGNITE,
		ModBlocks.LIGNITE_ORE,
		ModBlocks.DEEPSLATE_LIGNITE_ORE,
		ModItems.LIGNITE_BURN_TIME,
		"갈탄",
		"갈탄 광석",
		"심층암 갈탄 광석"
	);

	public static final FuelGradeSpec SUB_BITUMINOUS = new FuelGradeSpec(
		"sub_bituminous",
		ModItems.SUB_BITUMINOUS,
		ModBlocks.SUB_BITUMINOUS_ORE,
		ModBlocks.DEEPSLATE_SUB_BITUMINOUS_ORE,
		ModItems.SUB_BITUMINOUS_BURN_TIME,
		"아역청탄",
		"아역청탄 광석",
		"심층암 아역청탄 광석"
	);

	/** Vanilla coal / coal ore, renamed 역청탄 in Material lang. */
	public static final FuelGradeSpec BITUMINOUS = new FuelGradeSpec(
		"bituminous",
		Items.COAL,
		Blocks.COAL_ORE,
		Blocks.DEEPSLATE_COAL_ORE,
		1600,
		"역청탄",
		"역청탄 광석",
		"심층 역청탄 광석"
	);

	public static final FuelGradeSpec ANTHRACITE = new FuelGradeSpec(
		"anthracite",
		ModItems.ANTHRACITE,
		ModBlocks.ANTHRACITE_ORE,
		ModBlocks.DEEPSLATE_ANTHRACITE_ORE,
		ModItems.ANTHRACITE_BURN_TIME,
		"무연탄",
		"무연탄 광석",
		"심층암 무연탄 광석"
	);

	/** Grades requested after peat: lignite → anthracite. */
	public static final List<FuelGradeSpec> COAL_SERIES = List.of(
		LIGNITE,
		SUB_BITUMINOUS,
		BITUMINOUS,
		ANTHRACITE
	);

	private FuelGradeSpecs() {
	}
}
