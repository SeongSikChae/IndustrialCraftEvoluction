package com.industrialcraft.machine.client.render;

import com.industrialcraft.machine.block.entity.ReservoirBlockEntity;
import com.industrialcraft.machine.fluid.FluidVisuals;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Fluid level inside the metal/glass reservoir tank.
 */
public class ReservoirRenderer implements BlockEntityRenderer<ReservoirBlockEntity, ReservoirRenderState> {
	private static final float INNER_MIN = 3.0F / 16.0F;
	private static final float INNER_MAX = 13.0F / 16.0F;
	private static final float FLUID_Y0 = 2.0F / 16.0F;
	private static final float FLUID_Y1 = 14.0F / 16.0F;

	public ReservoirRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public ReservoirRenderState createRenderState() {
		return new ReservoirRenderState();
	}

	@Override
	public void extractRenderState(
		ReservoirBlockEntity blockEntity,
		ReservoirRenderState state,
		float partialTicks,
		Vec3 cameraPos,
		ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay
	) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPos, crumblingOverlay);
		int amount = blockEntity.getAmount();
		state.empty = amount <= 0;
		state.fill = state.empty ? 0.0F : Mth.clamp(amount / (float) blockEntity.getCapacity(), 0.0F, 1.0F);
		state.fluidArgb = FluidVisuals.argb(blockEntity.getFluid());
	}

	@Override
	public void submit(
		ReservoirRenderState state,
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		CameraRenderState camera
	) {
		if (state.empty || state.fill <= 0.0F) {
			return;
		}

		float y0 = FLUID_Y0;
		float y1 = FLUID_Y0 + (FLUID_Y1 - FLUID_Y0) * state.fill;
		int argb = state.fluidArgb;

		submitNodeCollector.submitCustomGeometry(
			poseStack,
			RenderTypes.debugFilledBox(),
			(pose, consumer) -> putBox(pose, consumer, INNER_MIN, y0, INNER_MIN, INNER_MAX, y1, INNER_MAX, argb)
		);
	}

	private static void putBox(
		PoseStack.Pose pose,
		VertexConsumer consumer,
		float x0,
		float y0,
		float z0,
		float x1,
		float y1,
		float z1,
		int argb
	) {
		putQuad(pose, consumer, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, argb);
		putQuad(pose, consumer, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, argb);
		putQuad(pose, consumer, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, argb);
		putQuad(pose, consumer, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, argb);
		putQuad(pose, consumer, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0, argb);
		putQuad(pose, consumer, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, argb);
	}

	private static void putQuad(
		PoseStack.Pose pose,
		VertexConsumer consumer,
		float x0,
		float y0,
		float z0,
		float x1,
		float y1,
		float z1,
		float x2,
		float y2,
		float z2,
		float x3,
		float y3,
		float z3,
		int argb
	) {
		consumer.addVertex(pose, x0, y0, z0).setColor(argb);
		consumer.addVertex(pose, x1, y1, z1).setColor(argb);
		consumer.addVertex(pose, x2, y2, z2).setColor(argb);
		consumer.addVertex(pose, x3, y3, z3).setColor(argb);
	}
}
