package com.industrialcraft.machine.fluid;

import net.minecraft.core.Direction;

/**
 * Fluid unit constants. Internal storage uses millibuckets (mB); 1000 mB = 1 B = 1 vanilla bucket.
 * Pressure uses fixed-point <strong>milli-kPa</strong> ({@link #PRESSURE_MILLI_PER_KPA}) for buffer/NBT.
 * Shaft torque stays SI {@code double} ({@code 1 Nm ≡ 10 kPa}); convert at the fluid boundary only.
 */
public final class FluidUnits {
	public static final int MB_PER_BUCKET = 1000;
	public static final int RESERVOIR_CAPACITY_MB = 64 * MB_PER_BUCKET;
	public static final int PIPE_CAPACITY_MB = MB_PER_BUCKET;

	/** Fixed-point scale: 1 kPa = 1000 milli-kPa. */
	public static final int PRESSURE_MILLI_PER_KPA = 1000;
	/** Hydraulic conversion: {@code 1 Nm ≡ 10 kPa} → \(p_\mathrm{pump} = 10\tau\). */
	public static final int KPA_PER_NM = 10;
	/** Maximum safe line pressure (kPa). Values above this rupture pressure-rated pipes. */
	public static final int MAX_SAFE_PRESSURE_KPA = 2560;
	/** {@link #MAX_SAFE_PRESSURE_KPA} in milli-kPa. */
	public static final int MAX_SAFE_PRESSURE_MILLI = MAX_SAFE_PRESSURE_KPA * PRESSURE_MILLI_PER_KPA;
	/** Pressure change per vertical block: ±10 kPa. */
	public static final int VERTICAL_PRESSURE_MILLI = 10 * PRESSURE_MILLI_PER_KPA;

	/** Full-buffer transfer cap (1 B/tick). */
	public static final int FLOW_MAX_MB_PER_TICK = MB_PER_BUCKET;
	/**
	 * Reference for κ: at {@link #FLOW_REF_PRESSURE_KPA} kPa and full pipe (1 B), rate = 1 B/tick.
	 * {@code κ = 1000/10 = 100}. Higher p is still capped at {@link #FLOW_MAX_MB_PER_TICK}.
	 */
	public static final int FLOW_REF_PRESSURE_KPA = 10;
	/**
	 * {@code rate = min(cap, round(κ × p_kPa × (q/C)))}; {@code p ≤ 0 → 0} (no negative pressure flow).
	 */
	public static final double FLOW_RATE_KAPPA = (double) MB_PER_BUCKET / (double) FLOW_REF_PRESSURE_KPA;

	/** Full reservoir head at capacity (kPa). */
	public static final int RESERVOIR_FULL_HEAD_KPA = 100;

	/**
	 * Rain collector fill rate while raining under open sky (~0.5 B/s; full 64 B ≈ 128 s).
	 */
	public static final int RAIN_COLLECT_MB_PER_TICK = 25;

	/** Legacy eighths (1 kPa = 8) → milli-kPa. */
	private static final int MILLI_PER_LEGACY_EIGHTH = PRESSURE_MILLI_PER_KPA / 8;

	private FluidUnits() {
	}

	public static double toBuckets(int mb) {
		return mb / (double) MB_PER_BUCKET;
	}

	public static int fromBuckets(double buckets) {
		return (int) (buckets * MB_PER_BUCKET);
	}

	public static String formatBuckets(int mb) {
		return String.format("%.2f", toBuckets(mb));
	}

	/** milli-kPa → SI kPa (never reports negative). */
	public static double milliToKpa(int pressureMilli) {
		if (pressureMilli <= 0) {
			return 0.0;
		}
		return pressureMilli / (double) PRESSURE_MILLI_PER_KPA;
	}

	/** SI kPa → milli-kPa (truncate toward zero). Buffer storage only. */
	public static int kpaToMilli(double kpa) {
		if (!Double.isFinite(kpa) || kpa <= 0.0) {
			return 0;
		}
		return (int) (kpa * PRESSURE_MILLI_PER_KPA);
	}

	/** Shaft SI torque → pump outlet pressure (kPa). Exact: {@code 10τ}. */
	public static double torqueNmToPumpKpa(double torqueNm) {
		if (!Double.isFinite(torqueNm) || torqueNm <= 0.0) {
			return 0.0;
		}
		return torqueNm * KPA_PER_NM;
	}

	/** Shaft SI torque → buffer milli-kPa at the fluid boundary. */
	public static int torqueNmToPumpPressureMilli(double torqueNm) {
		return kpaToMilli(torqueNmToPumpKpa(torqueNm));
	}

	/** @deprecated Use {@link #torqueNmToPumpPressureMilli(double)}. */
	@Deprecated
	public static int torqueMilliToPumpPressureMilli(int torqueMilli) {
		return torqueNmToPumpPressureMilli(torqueMilli / (double) PRESSURE_MILLI_PER_KPA);
	}

	/** Display only. */
	public static String formatKpa(int pressureMilli) {
		return String.format("%.3f", milliToKpa(pressureMilli));
	}

	/** Display only. */
	public static String formatKpaSi(double kpa) {
		return String.format("%.3f", kpa);
	}

	/** {@code true} when pressure exceeds the system safe maximum. */
	public static boolean exceedsMaxSafePressure(int pressureMilli) {
		return pressureMilli > MAX_SAFE_PRESSURE_MILLI;
	}

	public static boolean exceedsMaxSafePressureKpa(double kpa) {
		return Double.isFinite(kpa) && kpa > MAX_SAFE_PRESSURE_KPA;
	}

	/**
	 * Tick transfer budget: {@code min(1000, round(κ × p_kPa × (q / C)))}.
	 * Reference: 10 kPa + full 1 B → 1 B/tick ({@code κ = 100}). {@code p ≤ 0 → 0}.
	 *
	 * @param carriedKpa sender carried pressure in kPa (≥ 0; negatives treated as 0)
	 * @param amountMb   sender fluid amount q
	 * @param capacityMb sender capacity C (pipe = 1000)
	 */
	public static int flowRateMb(double carriedKpa, int amountMb, int capacityMb) {
		if (!(carriedKpa > 0.0) || amountMb <= 0 || capacityMb <= 0) {
			return 0;
		}
		double fill = amountMb / (double) capacityMb;
		int mb = (int) Math.round(FLOW_RATE_KAPPA * carriedKpa * fill);
		return Math.min(FLOW_MAX_MB_PER_TICK, Math.max(0, mb));
	}

	public static int flowRateMbFromMilli(int carriedMilli, int amountMb, int capacityMb) {
		return flowRateMb(milliToKpa(carriedMilli), amountMb, capacityMb);
	}

	/**
	 * Pairwise pressure share for connected pipes.
	 * Horizontal: amount-weighted equal pressure.
	 * Vertical: hydrostatic — lift the upper reading by {@link #VERTICAL_PRESSURE_MILLI} to the lower
	 * datum, average, then set upper = max(0, lower − vertical). Never negative.
	 * <p>
	 * Neither side may rise above {@code max(self, other)}: unconstrained φ-averaging on a flat
	 * tall column invents bottom pressure (8×40 kPa → ~75 kPa, longer → 170+), which a 40 kPa pump
	 * without a check valve must not be able to create.
	 *
	 * @param selfPressureMilli  pressure of the first pipe
	 * @param selfAmountMb       amount of the first pipe (must be &gt; 0)
	 * @param otherPressureMilli pressure of the neighbor
	 * @param otherAmountMb      amount of the neighbor (must be &gt; 0)
	 * @param toOther            direction from self → other
	 * @return {@code int[2] = { selfPressure', otherPressure' }} in milli-kPa
	 */
	public static int[] equalizeAdjacentPipePressures(
		int selfPressureMilli,
		int selfAmountMb,
		int otherPressureMilli,
		int otherAmountMb,
		Direction toOther
	) {
		int a = Math.max(0, selfAmountMb);
		int b = Math.max(0, otherAmountMb);
		if (a <= 0 || b <= 0 || toOther == null) {
			return new int[] { Math.max(0, selfPressureMilli), Math.max(0, otherPressureMilli) };
		}
		int pa = Math.max(0, selfPressureMilli);
		int pb = Math.max(0, otherPressureMilli);
		int v = VERTICAL_PRESSURE_MILLI;
		int maxExisting = Math.max(pa, pb);
		if (toOther == Direction.UP) {
			long sharedLower = ((long) pa * a + (long) (pb + v) * b) / ((long) a + b);
			int lower = Math.min(maxExisting, clampPressureMilli(sharedLower));
			int upper = Math.max(0, lower - v);
			return new int[] { lower, upper };
		}
		if (toOther == Direction.DOWN) {
			long sharedLower = ((long) pb * b + (long) (pa + v) * a) / ((long) a + b);
			int lower = Math.min(maxExisting, clampPressureMilli(sharedLower));
			int upper = Math.max(0, lower - v);
			return new int[] { upper, lower };
		}
		int shared = clampPressureMilli(((long) pa * a + (long) pb * b) / ((long) a + b));
		return new int[] { shared, shared };
	}

	private static int clampPressureMilli(long milli) {
		if (milli <= 0L) {
			return 0;
		}
		return (int) Math.min(Integer.MAX_VALUE, milli);
	}

	/** Migrate NBT stored as legacy pressure eighths. */
	public static int legacyEighthsToMilli(int eighths) {
		if (eighths <= 0) {
			return 0;
		}
		return eighths * MILLI_PER_LEGACY_EIGHTH;
	}
}
