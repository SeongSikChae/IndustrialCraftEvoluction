package com.industrialcraft.machine.fluid;

import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;

/**
 * Face-aware fluid insert/extract contract, analogous to {@link com.industrialcraft.machine.power.PowerSource}.
 * All amounts are millibuckets (mB); 1000 mB = 1 FU. Pressure is in eighths (1 PU = 8).
 * <p>
 * Receivers act as a <b>pressure gate</b>: fluid enters when carried PU ≥
 * {@link #getReceiveGatePressureEighths()} (strictly lower PU is refused).
 * Reservoirs use gate 0; pipes use stored buffer PU.
 * Pipe↔pipe amounts are steered toward {@code amount ∝ PU} so more fluid sits nearer the source.
 */
public interface FluidHandler {
	boolean canInsert(Direction face);

	boolean canExtract(Direction face);

	Fluid getFluid();

	/** Stored amount in mB. */
	int getAmount();

	/** Capacity in mB. */
	int getCapacity();

	/** Buffer pressure in eighths (0 when empty). */
	int getPressureEighths();

	default double getPressurePu() {
		return FluidUnits.eighthsToPu(this.getPressureEighths());
	}

	/**
	 * Minimum incoming (carried) pressure required to insert into this handler.
	 * Default: current buffer pressure (empty → 0). Reservoirs override to always 0.
	 */
	default int getReceiveGatePressureEighths() {
		return this.getAmount() <= 0 ? 0 : this.getPressureEighths();
	}

	/**
	 * When true, pipe↔pipe transfer is capped so amounts move toward {@code amount ∝ PU}.
	 * Reservoirs override to false (storage only).
	 */
	default boolean sharesPressureVolume() {
		return true;
	}

	default double getReceiveGatePressurePu() {
		return FluidUnits.eighthsToPu(this.getReceiveGatePressureEighths());
	}

	/**
	 * Inserts with 0 PU (e.g. bucket fill).
	 *
	 * @return millibuckets actually inserted
	 */
	int insert(Fluid fluid, int amountMb, boolean simulate);

	/**
	 * Inserts carrying pressure (eighths), mixed by amount-weighted average on the receiver.
	 *
	 * @return millibuckets actually inserted
	 */
	int insert(Fluid fluid, int amountMb, int pressureEighths, boolean simulate);

	/**
	 * Extracts up to {@code maxAmountMb} of the currently stored fluid.
	 *
	 * @return millibuckets actually extracted
	 */
	int extract(int maxAmountMb, boolean simulate);
}
