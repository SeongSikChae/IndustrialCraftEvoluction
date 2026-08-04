package com.industrialcraft.machine.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

/**
 * Helpers for reading rotary power across adjacent blocks on a shared shaft axis.
 */
public final class ShaftPower {
	private ShaftPower() {
	}

	/**
	 * Finds a {@link PowerSource} feeding into {@code pos} through {@code inputFace}.
	 * The neighbor must expose power on the face that touches this block.
	 */
	public static @Nullable PowerSource findIncoming(BlockGetter level, BlockPos pos, Direction inputFace) {
		BlockEntity neighbor = level.getBlockEntity(pos.relative(inputFace));
		if (neighbor instanceof PowerSource source && source.outputsToward(inputFace.getOpposite())) {
			return source;
		}
		return null;
	}
}
