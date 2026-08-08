package com.industrialcraft.machine.power;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * Engine fuel durations per {@code machine.mdc}: item/charcoal = furnace-minecart ticks;
 * compressed fuel blocks = furnace burn × 2. Buffer cap 40_000.
 */
public final class FuelDurations {
	public static final int VANILLA_MINECART_FUEL_TICKS = 3600;
	public static final int VANILLA_COAL_FURNACE_BURN = 1600;
	public static final int MAX_FUEL_TICKS = 40000;

	private FuelDurations() {
	}

	public static boolean isEngineFuel(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		Item item = stack.getItem();
		if (item == Items.COAL || item == Items.CHARCOAL || item == Blocks.COAL_BLOCK.asItem()) {
			return true;
		}
		Identifier id = BuiltInRegistries.ITEM.getKey(item);
		if (!"material".equals(id.getNamespace())) {
			return false;
		}
		return switch (id.getPath()) {
			case "peat", "lignite", "sub_bituminous", "anthracite",
				"peat_block", "lignite_block", "sub_bituminous_block", "anthracite_block" -> true;
			default -> false;
		};
	}

	public static boolean isCompressedFuelBlock(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		Item item = stack.getItem();
		if (item == Blocks.COAL_BLOCK.asItem()) {
			return true;
		}
		Identifier id = BuiltInRegistries.ITEM.getKey(item);
		if (!"material".equals(id.getNamespace())) {
			return false;
		}
		return switch (id.getPath()) {
			case "peat_block", "lignite_block", "sub_bituminous_block", "anthracite_block" -> true;
			default -> false;
		};
	}

	/** Furnace-minecart style scaling: furnaceBurn × 3600 / 1600. */
	public static int minecartStyleFuelTicks(Level level, ItemStack stack) {
		int furnaceBurn = level.fuelValues().burnDuration(stack);
		if (furnaceBurn <= 0) {
			return 0;
		}
		return furnaceBurn * VANILLA_MINECART_FUEL_TICKS / VANILLA_COAL_FURNACE_BURN;
	}

	/** Engine burn ticks per {@code machine.mdc}. */
	public static int engineFuelTicks(Level level, ItemStack stack) {
		if (!isEngineFuel(stack)) {
			return 0;
		}
		int furnaceBurn = level.fuelValues().burnDuration(stack);
		if (furnaceBurn <= 0) {
			return 0;
		}
		if (isCompressedFuelBlock(stack)) {
			return furnaceBurn * 2;
		}
		return minecartStyleFuelTicks(level, stack);
	}

	public static boolean canAcceptFuel(int currentFuel, int additionalFuel) {
		return additionalFuel > 0 && currentFuel + additionalFuel <= MAX_FUEL_TICKS;
	}
}
