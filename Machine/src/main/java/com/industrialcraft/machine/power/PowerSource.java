package com.industrialcraft.machine.power;

import net.minecraft.core.Direction;

/**
 * Rotary power output contract in SI units.
 * <p>
 * Torque is N·m, angular velocity is rad/s, mechanical power is W ({@code τ × ω}).
 * Arithmetic uses {@code double} with no fixed-point milli scaling.
 */
public interface PowerSource {
	/** Torque in newton-metres (Nm). */
	double getTorque();

	/** Angular velocity in rad/s. */
	double getOmega();

	default boolean isGenerating() {
		return this.getTorque() > 0.0 && this.getOmega() > 0.0;
	}

	/**
	 * Whether rotary power is exposed on the given face of this block.
	 * Omni sources return {@code true} for every face; shafted machines limit to their output side.
	 */
	default boolean outputsToward(Direction face) {
		return true;
	}

	/** Mechanical power in watts ({@code τ × ω}). */
	default double getPower() {
		return this.getTorque() * this.getOmega();
	}

	/** Non-finite or negative → 0. */
	static double sanitize(double si) {
		if (!Double.isFinite(si) || si <= 0.0) {
			return 0.0;
		}
		return si;
	}
}
