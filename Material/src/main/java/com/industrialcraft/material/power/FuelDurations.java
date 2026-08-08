package com.industrialcraft.material.power;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Shared fuel-duration helpers for furnace minecarts. Scales
 * {@link net.minecraft.world.level.block.entity.FuelValues} burn time so coal
 * (1600) maps to the vanilla minecart duration (3600). Machine engines use the
 * same formula independently and pick up these fuel values when Material is loaded.
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
			return 0;
		}
		return furnaceBurn * VANILLA_MINECART_FUEL_TICKS / VANILLA_COAL_FURNACE_BURN;
	}

	public static boolean canAcceptFuel(int currentFuel, int additionalFuel) {
		return additionalFuel > 0 && currentFuel + additionalFuel <= MAX_FUEL_TICKS;
	}
}
