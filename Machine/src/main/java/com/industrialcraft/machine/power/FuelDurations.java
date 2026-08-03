package com.industrialcraft.machine.power;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Scales {@link net.minecraft.world.level.block.entity.FuelValues} burn time so
 * coal (1600) maps to the vanilla furnace-minecart duration (3600).
 *
 * <p>Without Material, only vanilla {@code #furnace_minecart_fuel} items
 * (coal, charcoal) apply with vanilla burn times. With Material loaded, its
 * fuel-value overrides and extra tagged fuels are picked up automatically.
 */
public final class FuelDurations {
	public static final int VANILLA_MINECART_FUEL_TICKS = 3600;
	public static final int VANILLA_COAL_FURNACE_BURN = 1600;
	public static final int MAX_FUEL_TICKS = 32000;

	private FuelDurations() {
	}

	public static int minecartStyleFuelTicks(Level level, ItemStack stack) {
		int furnaceBurn = level.fuelValues().burnDuration(stack);
		if (furnaceBurn <= 0) {
			return VANILLA_MINECART_FUEL_TICKS;
		}
		return furnaceBurn * VANILLA_MINECART_FUEL_TICKS / VANILLA_COAL_FURNACE_BURN;
	}

	public static boolean canAcceptFuel(int currentFuel, int additionalFuel) {
		return additionalFuel > 0 && currentFuel + additionalFuel <= MAX_FUEL_TICKS;
	}
}
