package com.industrialcraft.machine.power;

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
}
