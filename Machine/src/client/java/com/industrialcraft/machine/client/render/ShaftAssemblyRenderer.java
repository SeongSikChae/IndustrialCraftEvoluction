package com.industrialcraft.machine.client.render;

import com.industrialcraft.machine.item.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Shared BER helpers for the {@link ModItems#SHAFT_GEAR} mesh.
 * Authored mesh shaft is +X; rotate so local +X aligns with FACING, then offset along +X.
 */
public final class ShaftAssemblyRenderer {
	private ShaftAssemblyRenderer() {
	}

	public static void resolveGear(
		ItemModelResolver itemModelResolver,
		ItemStackRenderState dest,
		@Nullable Level level,
		BlockPos pos
	) {
		itemModelResolver.updateForTopItem(
			dest,
			new ItemStack(ModItems.SHAFT_GEAR),
			ItemDisplayContext.FIXED,
			level,
			null,
			(int) pos.asLong()
		);
	}

	public static void submit(
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		ItemStackRenderState shaftAssembly,
		Direction facing,
		float shaftAngle,
		float assemblyX,
		float assemblyScale,
		int lightCoords
	) {
		if (shaftAssembly.isEmpty()) {
			return;
		}

		poseStack.pushPose();
		poseStack.translate(0.5F, 0.5F, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(-facing.getClockWise().toYRot()));
		poseStack.translate(assemblyX, 0.0F, 0.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(shaftAngle));
		poseStack.scale(assemblyScale, assemblyScale, assemblyScale);
		shaftAssembly.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, 0);
		poseStack.popPose();
	}
}
