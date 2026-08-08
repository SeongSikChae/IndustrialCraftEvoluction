package com.industrialcraft.machine.fluid;

import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;

/**
 * Face-aware fluid insert/extract contract, analogous to {@link com.industrialcraft.machine.power.PowerSource}.
 * All amounts are millibuckets (mB); 1000 mB = 1 B. Pressure is milli-kPa (1 kPa = 1000), same grid as milli-Nm.
 * <p>
 * Flow is <b>high → low</b>: fluid enters when carried kPa &gt; {@link #getReceiveGatePressureMilli()}
 * (equal or lower carrier is refused). Reservoirs use gate 0; pipes use stored buffer kPa.
 * Pumps impose outlet pressure ({@code 10τ} under {@code 1 Nm ≡ 10 kPa}).
 */
public interface FluidHandler {
	boolean canInsert(Direction face);

	boolean canExtract(Direction face);

	Fluid getFluid();

	/** Stored amount in mB. */
	int getAmount();

	/** Capacity in mB. */
	int getCapacity();

	/** Buffer pressure in milli-kPa (0 when empty). */
	int getPressureMilli();

	default double getPressureKpa() {
		return FluidUnits.milliToKpa(this.getPressureMilli());
	}

	/**
	 * Minimum incoming (carried) pressure required to insert into this handler.
	 * Default: current buffer pressure (empty → 0). Reservoirs override to always 0.
	 */
	default int getReceiveGatePressureMilli() {
		return this.getAmount() <= 0 ? 0 : this.getPressureMilli();
	}

	/**
	 * When true, this handler participates in line-pressure gating like a pipe.
	 * Reservoirs override to false (storage only; gate stays 0).
	 */
	default boolean sharesPressureVolume() {
		return true;
	}

	default double getReceiveGatePressureKpa() {
		return FluidUnits.milliToKpa(this.getReceiveGatePressureMilli());
	}

	/**
	 * When true, carried pressure above {@link FluidUnits#MAX_SAFE_PRESSURE_KPA} ruptures this handler
	 * instead of inserting.
	 */
	default boolean rupturesAboveMaxPressure() {
		return false;
	}

	/** Called when overpressure is supplied; no-op unless {@link #rupturesAboveMaxPressure()}. */
	default void ruptureFromOverpressure() {
	}

	/**
	 * Inserts with 0 kPa (e.g. bucket fill).
	 *
	 * @return millibuckets actually inserted
	 */
	int insert(Fluid fluid, int amountMb, boolean simulate);

	/**
	 * Inserts carrying pressure (milli-PU), mixed by amount-weighted average on the receiver.
	 *
	 * @return millibuckets actually inserted
	 */
	int insert(Fluid fluid, int amountMb, int pressureMilli, boolean simulate);

	/**
	 * Extracts up to {@code maxAmountMb} of the currently stored fluid.
	 *
	 * @return millibuckets actually extracted
	 */
	int extract(int maxAmountMb, boolean simulate);
}
