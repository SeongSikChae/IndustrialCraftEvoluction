package com.industrialcraft.machine.client.render;

import com.industrialcraft.machine.block.entity.ReservoirBlockEntity;
import com.industrialcraft.machine.fluid.FluidFillSteps;
import com.industrialcraft.machine.fluid.FluidVisuals;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Discrete fill level inside the reservoir glass (0..{@link FluidFillSteps#STEPS}).
 */
public class ReservoirRenderer implements BlockEntityRenderer<ReservoirBlockEntity, FluidTankRenderState> {
	private static final float INNER_MIN = 3.0F / 16.0F;
	private static final float INNER_MAX = 13.0F / 16.0F;
	private static final float FLUID_Y0 = 2.0F / 16.0F;
	private static final float FLUID_Y1 = 14.0F / 16.0F;

	public ReservoirRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public FluidTankRenderState createRenderState() {
		return new FluidTankRenderState();
	}

	@Override
	public void extractRenderState(
		ReservoirBlockEntity blockEntity,
		FluidTankRenderState state,
		float partialTicks,
		Vec3 cameraPos,
		ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay
	) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPos, crumblingOverlay);
		state.fillStep = FluidFillSteps.step(blockEntity.getAmount(), blockEntity.getCapacity());
		state.fluid = blockEntity.getFluid();
		state.fluidArgb = FluidVisuals.argb(state.fluid);
	}

	@Override
	public void submit(
		FluidTankRenderState state,
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		CameraRenderState camera
	) {
		float fill = FluidFillSteps.fillRatio(state.fillStep);
		if (fill <= 0.0F) {
			return;
		}
		float y1 = FLUID_Y0 + (FLUID_Y1 - FLUID_Y0) * fill;
		FluidVolumeGeometry.submitBox(
			poseStack,
			submitNodeCollector,
			INNER_MIN,
			FLUID_Y0,
			INNER_MIN,
			INNER_MAX,
			y1,
			INNER_MAX,
			state.fluidArgb,
			state.lightCoords
		);
	}
}
