package com.industrialcraft.machine.fluid;

import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * Sender-centric transfer:
 * <ol>
 *   <li>carried PU = source PU ± direction</li>
 *   <li>if carried PU &gt; {@link FluidUnits#MAX_SAFE_PRESSURE_PU} and the receiver ruptures,
 *       waste the tick transfer from the source and rupture the receiver</li>
 *   <li>receiver gate: accept when {@code carried >= gate} (refuse only lower PU)</li>
 *   <li>rate from carried PU alone</li>
 *   <li>pipe↔pipe: move only toward {@code amount ∝ PU} so more fluid sits near the source</li>
 * </ol>
 */
public final class FluidTransfer {
	private FluidTransfer() {
	}

	public static int move(FluidHandler from, FluidHandler to, Direction moveDir, boolean simulate) {
		return move(from, to, moveDir, FluidUnits.FLOW_MAX_MB_PER_TICK, simulate);
	}

	/**
	 * Moves up to {@code maxMb} along {@code moveDir} (source → destination).
	 *
	 * @return millibuckets actually moved
	 */
	public static int move(FluidHandler from, FluidHandler to, Direction moveDir, int maxMb, boolean simulate) {
		if (from == null || to == null || from == to || moveDir == null || maxMb <= 0) {
			return 0;
		}
		Fluid fluid = from.getFluid();
		if (fluid == Fluids.EMPTY || from.getAmount() <= 0) {
			return 0;
		}

		int carriedEighths = carryPressureEighths(from.getPressureEighths(), moveDir);
		if (FluidUnits.exceedsMaxSafePressure(carriedEighths) && to.rupturesAboveMaxPressure()) {
			return ruptureOverpressure(from, to, maxMb, carriedEighths, simulate);
		}

		int gateEighths = to.getReceiveGatePressureEighths();
		if (carriedEighths < gateEighths) {
			return 0;
		}

		double carriedPu = FluidUnits.eighthsToPu(carriedEighths);
		int rateMb = Math.min(maxMb, FluidUnits.flowRateMb(carriedPu));
		rateMb = Math.min(rateMb, shareLimitedSendMb(from, to, carriedEighths));
		return transfer(from, to, fluid, carriedEighths, rateMb, simulate);
	}

	/**
	 * Overpressure path: remove the would-be tick transfer from the source (fluid lost) and rupture
	 * the receiving pressure-rated handler.
	 */
	private static int ruptureOverpressure(
		FluidHandler from,
		FluidHandler to,
		int maxMb,
		int carriedEighths,
		boolean simulate
	) {
		double carriedPu = FluidUnits.eighthsToPu(carriedEighths);
		int wasteMb = Math.min(maxMb, FluidUnits.flowRateMb(carriedPu));
		if (simulate) {
			return Math.min(wasteMb, from.getAmount());
		}
		int wasted = from.extract(wasteMb, false);
		to.ruptureFromOverpressure();
		return wasted;
	}

	/**
	 * Caps send so the pair's amounts move toward {@code amount ∝ PU}.
	 * Tanks (no pressure share) are uncapped here — rate + gate only.
	 */
	private static int shareLimitedSendMb(FluidHandler from, FluidHandler to, int carriedEighths) {
		if (!from.sharesPressureVolume() || !to.sharesPressureVolume()) {
			return Integer.MAX_VALUE;
		}
		int puFrom = from.getPressureEighths();
		if (puFrom <= 0) {
			return Integer.MAX_VALUE;
		}
		int puTo = to.getAmount() <= 0 ? carriedEighths : to.getPressureEighths();
		if (puTo <= 0) {
			// Next hop would arrive at 0 PU — do not push further for share fill.
			return 0;
		}
		long total = (long) from.getAmount() + to.getAmount();
		long sumPu = (long) puFrom + puTo;
		int wantFrom = (int) (total * puFrom / sumPu);
		return Math.max(0, from.getAmount() - wantFrom);
	}

	private static int transfer(
		FluidHandler from,
		FluidHandler to,
		Fluid fluid,
		int carriedEighths,
		int rateMb,
		boolean simulate
	) {
		if (rateMb <= 0) {
			return 0;
		}
		int available = from.extract(rateMb, true);
		if (available <= 0) {
			return 0;
		}
		int accepted = to.insert(fluid, available, carriedEighths, true);
		if (accepted <= 0) {
			return 0;
		}
		if (!simulate) {
			int extracted = from.extract(accepted, false);
			int inserted = to.insert(fluid, extracted, carriedEighths, false);
			if (inserted < extracted) {
				from.insert(fluid, extracted - inserted, carriedEighths, false);
				return inserted;
			}
			return extracted;
		}
		return accepted;
	}

	public static int carryPressureEighths(int sourceEighths, Direction moveDir) {
		int carried = Math.max(0, sourceEighths);
		if (moveDir == Direction.DOWN) {
			carried += FluidUnits.FALL_PRESSURE_EIGHTHS;
		} else if (moveDir == Direction.UP) {
			carried = Math.max(0, carried - FluidUnits.FALL_PRESSURE_EIGHTHS);
		} else if (moveDir.getAxis().isHorizontal()) {
			carried = Math.max(0, carried - FluidUnits.HORIZONTAL_PRESSURE_LOSS_EIGHTHS);
		}
		return carried;
	}
}
