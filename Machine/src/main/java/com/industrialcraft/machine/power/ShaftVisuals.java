package com.industrialcraft.machine.power;

import net.minecraft.util.Mth;

/**
 * Maps mechanical omega (1..32768) to BER shaft degrees-per-tick on a log2 curve.
 * Panel / power numbers stay linear; only the spinning mesh uses this.
 */
public final class ShaftVisuals {
	public static final int OMEGA_VISUAL_MAX = 32_768;
	/** Degrees per tick at ω = 1. */
	public static final float MIN_DEG_PER_TICK = 1.5F;
	/** Degrees per tick at ω = {@link #OMEGA_VISUAL_MAX}. */
	public static final float MAX_DEG_PER_TICK = 15.0F;

	private static final float LOG2_OF_MAX = 15.0F;

	private ShaftVisuals() {
	}

	/**
	 * Visual shaft angular speed in degrees per game tick.
	 * {@code omega <= 0} → stopped; {@code (0, 1)} ramps into the log curve;
	 * {@code 1..32768} → {@link #MIN_DEG_PER_TICK}..{@link #MAX_DEG_PER_TICK}.
	 */
	public static float degreesPerTick(float omega) {
		if (omega <= 0.0F) {
			return 0.0F;
		}
		if (omega < 1.0F) {
			return MIN_DEG_PER_TICK * omega;
		}

		float clamped = Math.min(omega, (float) OMEGA_VISUAL_MAX);
		float t = (float) (Math.log(clamped) / Math.log(2.0D)) / LOG2_OF_MAX;
		return MIN_DEG_PER_TICK + (MAX_DEG_PER_TICK - MIN_DEG_PER_TICK) * Mth.clamp(t, 0.0F, 1.0F);
	}
}
