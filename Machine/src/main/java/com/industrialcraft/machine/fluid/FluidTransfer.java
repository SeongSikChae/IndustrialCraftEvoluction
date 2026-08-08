package com.industrialcraft.machine.fluid;

import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * Sender-centric transfer. Locked principles:
 * <ol>
 *   <li>Fluid flows from higher hydraulic pressure to lower (carried kPa &gt; receiver gate).</li>
 *   <li>Connected pipes ({@link FluidHandler#sharesPressureVolume()}) also <b>balance amounts</b>
 *       toward equality even when pressures are equal (rate still applies, min 1 mB when moving).
 *       The {@code ⌊excess/2⌋} amount-balance cap uses {@code max(1, ⌊excess/2⌋)} so a single leftover
 *       millibucket can still move under pressure drive (otherwise U-bends stall).</li>
 *   <li>Height adjustment is applied before the pressure comparison.</li>
 *   <li>Rate comes from carried kPa and sender fill fraction; after a gate pass, rounded-down 0
 *       becomes at least 1 mB while the sender still has fluid. Pumps set outlet pressure separately.</li>
 *   <li>Inserted pressure is always the height-adjusted carried value (never the raw sender reading).
 *       Otherwise vertical amount-balance would ignore −10 kPa/block and hydrostatic share would ratchet.</li>
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
		if (!to.getFluid().isSame(fluid) && to.getAmount() > 0) {
			return 0;
		}

		int carriedMilli = carryPressureMilli(from.getPressureMilli(), moveDir);
		if (FluidUnits.exceedsMaxSafePressure(carriedMilli) && to.rupturesAboveMaxPressure()) {
			return ruptureOverpressure(from, to, maxMb, carriedMilli, simulate);
		}

		int gateMilli = to.getReceiveGatePressureMilli();
		boolean pressureDrive = carriedMilli > gateMilli;
		boolean lineBalance = from.sharesPressureVolume() && to.sharesPressureVolume();
		int excess = from.getAmount() - to.getAmount();

		if (!pressureDrive && !lineBalance) {
			return 0;
		}
		// Pipe↔pipe balance: only push toward the emptier neighbor.
		if (lineBalance && excess <= 0) {
			return 0;
		}
		if (!pressureDrive && excess <= 1) {
			// Integer equality (diff 0 or 1) — nothing useful to balance.
			return 0;
		}

		int capacity = Math.max(1, from.getCapacity());
		int rateBasisMilli = pressureDrive
			? carriedMilli
			: Math.max(from.getPressureMilli(), to.getPressureMilli());
		int rateMb = FluidUnits.flowRateMbFromMilli(rateBasisMilli, from.getAmount(), capacity);
		if (rateMb <= 0 && from.getAmount() > 0 && (pressureDrive || lineBalance)) {
			rateMb = 1;
		}
		rateMb = Math.min(maxMb, rateMb);
		if (lineBalance) {
			// Cap by ⌊excess/2⌋ for stability, but never floor to 0 when excess ≥ 1 —
			// pressure drive with excess==1 was stalling pressurized U-bends (leftover mB).
			rateMb = Math.min(rateMb, Math.max(1, excess / 2));
		}
		if (rateMb <= 0) {
			return 0;
		}

		return transfer(from, to, fluid, carriedMilli, rateMb, simulate);
	}

	/**
	 * Overpressure path: remove the would-be tick transfer from the source (fluid lost) and rupture
	 * the receiving pressure-rated handler.
	 */
	private static int ruptureOverpressure(
		FluidHandler from,
		FluidHandler to,
		int maxMb,
		int carriedMilli,
		boolean simulate
	) {
		int capacity = Math.max(1, from.getCapacity());
		int wasteMb = Math.min(
			maxMb,
			Math.max(1, FluidUnits.flowRateMbFromMilli(carriedMilli, from.getAmount(), capacity))
		);
		if (simulate) {
			return Math.min(wasteMb, from.getAmount());
		}
		int wasted = from.extract(wasteMb, false);
		to.ruptureFromOverpressure();
		return wasted;
	}

	private static int transfer(
		FluidHandler from,
		FluidHandler to,
		Fluid fluid,
		int carriedMilli,
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
		int accepted = to.insert(fluid, available, carriedMilli, true);
		if (accepted <= 0) {
			return 0;
		}
		if (!simulate) {
			int extracted = from.extract(accepted, false);
			int inserted = to.insert(fluid, extracted, carriedMilli, false);
			if (inserted < extracted) {
				from.insert(fluid, extracted - inserted, carriedMilli, false);
				return inserted;
			}
			return extracted;
		}
		return accepted;
	}

	public static int carryPressureMilli(int sourceMilli, Direction moveDir) {
		int carried = Math.max(0, sourceMilli);
		if (moveDir == Direction.DOWN) {
			carried += FluidUnits.VERTICAL_PRESSURE_MILLI;
		} else if (moveDir == Direction.UP) {
			carried = Math.max(0, carried - FluidUnits.VERTICAL_PRESSURE_MILLI);
		}
		return carried;
	}
}
