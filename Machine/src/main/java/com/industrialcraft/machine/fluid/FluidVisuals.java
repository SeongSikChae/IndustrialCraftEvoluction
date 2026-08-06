package com.industrialcraft.machine.fluid;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * Shared display helpers for fluid amounts in GUIs / HUD / BER.
 */
public final class FluidVisuals {
	public static final int WATER_ARGB = 0x803F76E4;
	public static final int LAVA_ARGB = 0xE0E85A16;
	public static final int DEFAULT_ARGB = 0x807FDBFF;

	private FluidVisuals() {
	}

	public static int argb(Fluid fluid) {
		if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
			return WATER_ARGB;
		}
		if (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA) {
			return LAVA_ARGB;
		}
		return DEFAULT_ARGB;
	}

	public static Component displayName(Fluid fluid) {
		if (fluid == null || fluid == Fluids.EMPTY) {
			return Component.translatable("gui.machine.fluid_pipe.empty");
		}
		return fluid.defaultFluidState().createLegacyBlock().getBlock().getName();
	}
}
