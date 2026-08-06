package com.industrialcraft.machine.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

/**
 * Helpers for reading {@link FluidHandler} across adjacent blocks.
 */
public final class FluidNeighbor {
	private FluidNeighbor() {
	}

	/**
	 * Finds a handler at the neighbor toward {@code face} that accepts insert on the touching face.
	 */
	public static @Nullable FluidHandler findInsertable(BlockGetter level, BlockPos pos, Direction face) {
		BlockEntity neighbor = level.getBlockEntity(pos.relative(face));
		if (neighbor instanceof FluidHandler handler && handler.canInsert(face.getOpposite())) {
			return handler;
		}
		return null;
	}

	/**
	 * Finds a handler at the neighbor toward {@code face} that allows extract on the touching face.
	 */
	public static @Nullable FluidHandler findExtractable(BlockGetter level, BlockPos pos, Direction face) {
		BlockEntity neighbor = level.getBlockEntity(pos.relative(face));
		if (neighbor instanceof FluidHandler handler && handler.canExtract(face.getOpposite())) {
			return handler;
		}
		return null;
	}
}
