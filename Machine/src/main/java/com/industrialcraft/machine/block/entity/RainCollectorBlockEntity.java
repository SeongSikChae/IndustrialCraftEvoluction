package com.industrialcraft.machine.block.entity;

import com.industrialcraft.machine.MachineMod;
import com.industrialcraft.machine.fluid.FluidHandler;
import com.industrialcraft.machine.fluid.FluidNeighbor;
import com.industrialcraft.machine.fluid.FluidUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;

/**
 * Server-ticks rain collection into any insertable {@link FluidHandler} below.
 */
public class RainCollectorBlockEntity extends BlockEntity {
	public RainCollectorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.RAIN_COLLECTOR, pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, RainCollectorBlockEntity entity) {
		// Sample rain in the air cell above this solid block. {@link Level#isRainingAt} rejects
		// positions whose MOTION_BLOCKING heightmap is above pos.getY(), which is always true
		// for the collector's own cell when it has a collision shape.
		if (!level.isRainingAt(pos.above())) {
			return;
		}
		FluidHandler below = FluidNeighbor.findInsertable(level, pos, Direction.DOWN);
		if (below == null) {
			return;
		}
		below.insert(Fluids.WATER, FluidUnits.RAIN_COLLECT_MB_PER_TICK, false);
	}

	/**
	 * Empty-hand diagnose: writes rain/sky/heightmap/below-handler state to the game log.
	 */
	public void logDiagnostics() {
		Level level = this.level;
		if (level == null || level.isClientSide()) {
			return;
		}
		BlockPos pos = this.worldPosition;
		BlockPos above = pos.above();
		BlockPos belowPos = pos.below();
		int heightmapY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());

		boolean levelRaining = level.isRaining();
		boolean rainingAtSelf = level.isRainingAt(pos);
		boolean rainingAtAbove = level.isRainingAt(above);
		boolean canSeeSkySelf = level.canSeeSky(pos);
		boolean canSeeSkyAbove = level.canSeeSky(above);

		BlockState belowState = level.getBlockState(belowPos);
		BlockEntity belowBe = level.getBlockEntity(belowPos);
		FluidHandler insertable = FluidNeighbor.findInsertable(level, pos, Direction.DOWN);
		int simulateInsert = insertable == null
			? -1
			: insertable.insert(Fluids.WATER, FluidUnits.RAIN_COLLECT_MB_PER_TICK, true);

		String belowBeType = belowBe == null ? "none" : belowBe.getClass().getSimpleName();
		String belowFluid = "n/a";
		int belowAmount = -1;
		int belowCap = -1;
		if (insertable != null) {
			belowFluid = BuiltInRegistries.FLUID.getKey(insertable.getFluid()).toString();
			belowAmount = insertable.getAmount();
			belowCap = insertable.getCapacity();
		}

		String verdict;
		if (!levelRaining) {
			verdict = "reject: level not raining";
		} else if (!rainingAtAbove) {
			verdict = "reject: isRainingAt(above) false (sky/biome/heightmap)";
		} else if (insertable == null) {
			verdict = "reject: no insertable FluidHandler below (UP face)";
		} else if (simulateInsert <= 0) {
			verdict = "reject: insert simulated 0 (full or wrong fluid)";
		} else {
			verdict = "ok: would insert " + simulateInsert + " mB/tick";
		}

		MachineMod.LOGGER.info(
			"RainCollector @ [{}, {}, {}] verdict={} levelRaining={} rainingAtSelf={} rainingAtAbove={} "
				+ "canSeeSkySelf={} canSeeSkyAbove={} heightmapMotionBlockingY={} posY={} "
				+ "belowBlock={} belowBE={} insertable={} belowFluid={} belowAmount={} mB cap={} simulateInsertMb={}",
			pos.getX(),
			pos.getY(),
			pos.getZ(),
			verdict,
			levelRaining,
			rainingAtSelf,
			rainingAtAbove,
			canSeeSkySelf,
			canSeeSkyAbove,
			heightmapY,
			pos.getY(),
			BuiltInRegistries.BLOCK.getKey(belowState.getBlock()),
			belowBeType,
			insertable != null,
			belowFluid,
			belowAmount,
			belowCap,
			simulateInsert
		);
	}
}
