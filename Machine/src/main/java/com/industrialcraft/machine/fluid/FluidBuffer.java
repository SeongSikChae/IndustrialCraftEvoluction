package com.industrialcraft.machine.fluid;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Single-fluid tank. Amount/capacity in mB; pressure in milli-kPa (1 kPa = 1000).
 */
public final class FluidBuffer {
	private final int capacityMb;
	private Fluid fluid = Fluids.EMPTY;
	private int amountMb;
	/** Fixed-point pressure; meaningful only when non-empty. */
	private int pressureMilli;

	public FluidBuffer(int capacityMb) {
		this.capacityMb = Math.max(1, capacityMb);
	}

	public Fluid getFluid() {
		return this.fluid;
	}

	/** Stored amount in mB. */
	public int getAmount() {
		return this.amountMb;
	}

	/** Capacity in mB. */
	public int getCapacity() {
		return this.capacityMb;
	}

	/** Pressure in milli-kPa (0 when empty; never negative). */
	public int getPressureMilli() {
		return this.isEmpty() ? 0 : Math.max(0, this.pressureMilli);
	}

	/** Pressure in kPa (for display / continuous formulas). */
	public double getPressureKpa() {
		return FluidUnits.milliToKpa(this.getPressureMilli());
	}

	public boolean isEmpty() {
		return this.amountMb <= 0 || this.fluid == Fluids.EMPTY;
	}

	public int getSpace() {
		return this.capacityMb - this.amountMb;
	}

	public static Fluid normalize(Fluid fluid) {
		if (fluid == null || fluid == Fluids.EMPTY) {
			return Fluids.EMPTY;
		}
		if (fluid instanceof FlowingFluid flowing) {
			return flowing.getSource();
		}
		return fluid;
	}

	/**
	 * Adds pressure to non-empty buffer (check-valve boost). Does not change amount.
	 */
	public void addPressureMilli(int deltaMilli) {
		if (this.isEmpty() || deltaMilli == 0) {
			return;
		}
		long next = (long) this.pressureMilli + deltaMilli;
		this.pressureMilli = (int) Math.max(0L, Math.min(Integer.MAX_VALUE, next));
	}

	/**
	 * Sets absolute pressure for a non-empty buffer (line pressure share). Cleared when empty.
	 */
	public void setPressureMilli(int pressureMilli) {
		if (this.isEmpty()) {
			this.pressureMilli = 0;
			return;
		}
		this.pressureMilli = Math.max(0, pressureMilli);
	}

	/**
	 * Inserts with 0 kPa (bucket fill / default).
	 */
	public int insert(Fluid incoming, int amountMb, boolean simulate) {
		return this.insert(incoming, amountMb, 0, simulate);
	}

	/**
	 * Inserts fluid carrying {@code incomingPressureMilli}, mixing by amount-weighted average.
	 *
	 * @return millibuckets actually inserted
	 */
	public int insert(Fluid incoming, int amountMb, int incomingPressureMilli, boolean simulate) {
		Fluid normalized = normalize(incoming);
		if (normalized == Fluids.EMPTY || amountMb <= 0) {
			return 0;
		}
		if (!this.isEmpty() && !this.fluid.isSame(normalized)) {
			return 0;
		}
		int toInsert = Math.min(amountMb, this.getSpace());
		if (toInsert <= 0) {
			return 0;
		}
		if (!simulate) {
			int clampedIncoming = Math.max(0, incomingPressureMilli);
			if (this.isEmpty()) {
				this.fluid = normalized;
				this.amountMb = toInsert;
				this.pressureMilli = clampedIncoming;
			} else {
				long totalMb = (long) this.amountMb + toInsert;
				long mixed = (long) this.pressureMilli * this.amountMb + (long) clampedIncoming * toInsert;
				this.amountMb += toInsert;
				this.pressureMilli = (int) (mixed / totalMb);
			}
		}
		return toInsert;
	}

	/**
	 * Extracts fluid; remaining fluid keeps the same pressure. Clears pressure when emptied.
	 *
	 * @return millibuckets actually extracted
	 */
	public int extract(int maxAmountMb, boolean simulate) {
		if (this.isEmpty() || maxAmountMb <= 0) {
			return 0;
		}
		int toExtract = Math.min(maxAmountMb, this.amountMb);
		if (!simulate) {
			this.amountMb -= toExtract;
			if (this.amountMb <= 0) {
				this.amountMb = 0;
				this.fluid = Fluids.EMPTY;
				this.pressureMilli = 0;
			}
		}
		return toExtract;
	}

	public void save(ValueOutput output) {
		output.putString("Fluid", BuiltInRegistries.FLUID.getKey(this.fluid).toString());
		output.putInt("AmountMb", this.amountMb);
		output.putInt("PressureMilli", this.isEmpty() ? 0 : this.pressureMilli);
	}

	public void load(ValueInput input) {
		String raw = input.getStringOr("Fluid", "minecraft:empty");
		Identifier id = Identifier.tryParse(raw);
		Fluid loaded = id != null ? BuiltInRegistries.FLUID.getValue(id) : Fluids.EMPTY;
		this.fluid = normalize(loaded);
		this.amountMb = Mth.clamp(input.getIntOr("AmountMb", 0), 0, this.capacityMb);
		int milli = input.getIntOr("PressureMilli", Integer.MIN_VALUE);
		if (milli == Integer.MIN_VALUE) {
			milli = FluidUnits.legacyEighthsToMilli(input.getIntOr("PressureEighths", 0));
		}
		this.pressureMilli = Math.max(0, milli);
		if (this.amountMb <= 0 || this.fluid == Fluids.EMPTY) {
			this.fluid = Fluids.EMPTY;
			this.amountMb = 0;
			this.pressureMilli = 0;
		}
	}
}
