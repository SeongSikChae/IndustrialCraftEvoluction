package com.industrialcraft.machine.client;

import com.industrialcraft.machine.block.entity.FluidPipeBlockEntity;
import com.industrialcraft.machine.fluid.FluidUnits;
import com.industrialcraft.machine.fluid.FluidVisuals;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Shows pipe fluid fill on the action bar while looking at a fluid pipe.
 */
public final class FluidPipeLookOverlay {
	private FluidPipeLookOverlay() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(FluidPipeLookOverlay::onClientTick);
	}

	private static void onClientTick(Minecraft minecraft) {
		if (minecraft.player == null || minecraft.level == null) {
			return;
		}
		HitResult hit = minecraft.hitResult;
		if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
			return;
		}
		BlockEntity blockEntity = minecraft.level.getBlockEntity(blockHit.getBlockPos());
		if (!(blockEntity instanceof FluidPipeBlockEntity pipe)) {
			return;
		}

		Component message;
		if (pipe.getAmount() <= 0) {
			message = Component.translatable("gui.machine.fluid.empty");
		} else {
			message = Component.translatable(
				"gui.machine.fluid_pipe.amount",
				FluidVisuals.displayName(pipe.getFluid()),
				FluidUnits.formatBuckets(pipe.getAmount()),
				FluidUnits.formatBuckets(pipe.getCapacity()),
				FluidUnits.formatKpa(pipe.getPressureMilli())
			);
		}
		minecraft.player.sendOverlayMessage(message);
	}
}
