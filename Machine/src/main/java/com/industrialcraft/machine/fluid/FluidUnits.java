package com.industrialcraft.machine.fluid;

/**
 * Fluid unit constants. Internal storage uses millibuckets (mB); 1000 mB = 1 FU = 1 vanilla bucket.
 * Pressure uses fixed-point eighths: 1 PU = 8 units (0.125 PU = 1 unit).
 */
public final class FluidUnits {
	public static final int MB_PER_BUCKET = 1000;
	public static final int RESERVOIR_CAPACITY_MB = 64 * MB_PER_BUCKET;
	public static final int PIPE_CAPACITY_MB = MB_PER_BUCKET;

	/** Fixed-point scale: 1 PU = 8 eighths. */
	public static final int PRESSURE_EIGHTHS_PER_PU = 8;
	/** Maximum safe line pressure (PU). Values above this rupture pressure-rated pipes. */
	public static final int MAX_SAFE_PRESSURE_PU = 256;
	/** {@link #MAX_SAFE_PRESSURE_PU} in eighths. */
	public static final int MAX_SAFE_PRESSURE_EIGHTHS = MAX_SAFE_PRESSURE_PU * PRESSURE_EIGHTHS_PER_PU;
	/** Pressure gained when fluid drops one block (Y decreases). */
	public static final int FALL_PRESSURE_EIGHTHS = PRESSURE_EIGHTHS_PER_PU;
	/** Pressure lost on each horizontal pipe hop (0.125 PU). */
	public static final int HORIZONTAL_PRESSURE_LOSS_EIGHTHS = 1;

	/** Baseline flow ≈ 1 FU/s when carried PU is near 0; uses at least 1 mB when flowing. */
	public static final int FLOW_BASE_MB_PER_TICK = MB_PER_BUCKET / 20;
	/** Extra relative rate per 1 PU of sender carried pressure. */
	public static final double FLOW_RATE_K = 1.0;
	/** Hard cap so high carried PU cannot dump a full pipe in one tick. */
	public static final int FLOW_MAX_MB_PER_TICK = MB_PER_BUCKET;

	/**
	 * Rain collector fill rate while raining under open sky (~0.5 FU/s; full 64 FU ≈ 128 s).
	 */
	public static final int RAIN_COLLECT_MB_PER_TICK = 25;

	private FluidUnits() {
	}

	public static double toFu(int mb) {
		return mb / (double) MB_PER_BUCKET;
	}

	public static int fromFu(double fu) {
		return (int) Math.round(fu * MB_PER_BUCKET);
	}

	public static String formatFu(int mb) {
		return String.format("%.2f", toFu(mb));
	}

	public static double eighthsToPu(int eighths) {
		return eighths / (double) PRESSURE_EIGHTHS_PER_PU;
	}

	public static int puToEighths(double pu) {
		return (int) Math.round(pu * PRESSURE_EIGHTHS_PER_PU);
	}

	public static String formatPu(int eighths) {
		return String.format("%.3f", eighthsToPu(eighths));
	}

	/** {@code true} when pressure exceeds the system safe maximum ({@link #MAX_SAFE_PRESSURE_PU} PU). */
	public static boolean exceedsMaxSafePressure(int eighths) {
		return eighths > MAX_SAFE_PRESSURE_EIGHTHS;
	}

	/**
	 * Tick transfer budget from sender carried pressure (PU units, not eighths).
	 */
	public static int flowRateMb(double carriedPu) {
		if (carriedPu <= 0.0) {
			return 0;
		}
		double rate = FLOW_BASE_MB_PER_TICK * (1.0 + FLOW_RATE_K * carriedPu);
		int mb = (int) Math.round(rate);
		return Math.max(1, Math.min(FLOW_MAX_MB_PER_TICK, mb));
	}
}
