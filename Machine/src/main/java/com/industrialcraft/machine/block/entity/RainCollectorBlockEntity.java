package com.industrialcraft.machine.block.entity;

import com.industrialcraft.machine.fluid.FluidHandler;
import com.industrialcraft.machine.fluid.FluidNeighbor;
import com.industrialcraft.machine.fluid.FluidUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
}
