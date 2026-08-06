package com.industrialcraft.machine.fluid;

import net.minecraft.util.Mth;

/**
 * Discrete fill levels for world visuals. Reservoir and pipe share the same step count;
 * only capacity differs when converting amount → step.
 * Client sync is expected on step (or fluid type) change, not every millibucket.
 */
public final class FluidFillSteps {
	/** 0 = empty, {@link #STEPS} = full. */
	public static final int STEPS = 8;

	private FluidFillSteps() {
	}

	/**
	 * Maps stored millibuckets to a visual step.
	 * Any positive amount is at least 1; full capacity is {@link #STEPS}.
	 */
	public static int step(int amountMb, int capacityMb) {
		if (amountMb <= 0 || capacityMb <= 0) {
			return 0;
		}
		if (amountMb >= capacityMb) {
			return STEPS;
		}
		return Mth.clamp((amountMb * STEPS + capacityMb - 1) / capacityMb, 1, STEPS);
	}

	/** Vertical fill fraction for BER meshes. */
	public static float fillRatio(int step) {
		return Mth.clamp(step, 0, STEPS) / (float) STEPS;
	}
}
