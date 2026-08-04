package com.industrialcraft.machine.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public final class FurnaceEngineOrientationVerifier {
	private FurnaceEngineOrientationVerifier() {
	}

	public static void verifyOrThrow() {
		FurnaceEngineBlock block = (FurnaceEngineBlock) ModBlocks.FURNACE_ENGINE;
		int failures = 0;

		for (Direction look : Direction.Plane.HORIZONTAL) {
			Direction expected = FurnaceEngineBlock.computeOutputShaft(look);
			BlockState state = block.defaultBlockState().setValue(FurnaceEngineBlock.FACING, expected);
			Direction shape = FurnaceEngineBlock.detectSprocketFromShape(state);
			if (FurnaceEngineBlock.getOutputFace(state) != expected || shape != expected) {
				failures++;
			}
		}

		if (failures > 0) {
			throw new IllegalStateException("FurnaceEngine orientation self-check failed (" + failures + ")");
		}
	}
}
