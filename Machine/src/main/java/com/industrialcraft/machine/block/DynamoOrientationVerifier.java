package com.industrialcraft.machine.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/** Placement / I/O faces must match .cursor/rules/machine-shaft-power.mdc (dynamo). */
public final class DynamoOrientationVerifier {
	private DynamoOrientationVerifier() {
	}

	public static void verifyOrThrow() {
		DynamoBlock block = (DynamoBlock) ModBlocks.DYNAMO;
		int failures = 0;

		for (Direction look : Direction.Plane.HORIZONTAL) {
			Direction expectedOutput = look.getOpposite();
			Direction expectedInput = look;
			BlockState state = block.defaultBlockState().setValue(DynamoBlock.FACING, expectedOutput);
			if (DynamoBlock.getOutputFace(state) != expectedOutput || DynamoBlock.getInputFace(state) != expectedInput) {
				failures++;
			}
		}

		if (failures > 0) {
			throw new IllegalStateException("Dynamo orientation self-check failed (" + failures + ")");
		}
	}
}
