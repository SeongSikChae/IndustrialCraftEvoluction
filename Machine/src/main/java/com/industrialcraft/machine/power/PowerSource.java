package com.industrialcraft.machine.power;

import net.minecraft.core.Direction;

/**
 * Minimal rotary power output contract. Shaft networks and machines can query
 * this without depending on engine-specific inventory/fuel logic.
 */
public interface PowerSource {
	int getTorque();

	int getOmega();

	default boolean isGenerating() {
		return getTorque() > 0 && getOmega() > 0;
	}

	/**
	 * Whether rotary power is exposed on the given face of this block.
	 * Omni sources return {@code true} for every face; shafted machines limit to their output side.
	 */
	default boolean outputsToward(Direction face) {
		return true;
	}

	/** Mechanical power in watts ({@code torque × omega}). */
	default int getPower() {
		return getTorque() * getOmega();
	}
}
