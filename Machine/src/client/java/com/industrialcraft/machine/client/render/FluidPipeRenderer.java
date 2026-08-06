package com.industrialcraft.machine.client.render;

import com.industrialcraft.machine.block.FluidPipeBlock;
import com.industrialcraft.machine.block.entity.FluidPipeBlockEntity;
import com.industrialcraft.machine.fluid.FluidFillSteps;
import com.industrialcraft.machine.fluid.FluidVisuals;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Discrete fill level inside the thin pipe core / arms (0..{@link FluidFillSteps#STEPS}).
 */
public class FluidPipeRenderer implements BlockEntityRenderer<FluidPipeBlockEntity, FluidPipeRenderState> {
	private static final float OUTER_MIN = 6.0F / 16.0F;
	private static final float OUTER_MAX = 10.0F / 16.0F;
	private static final float INNER_MIN = 6.5F / 16.0F;
	private static final float INNER_MAX = 9.5F / 16.0F;

	public FluidPipeRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public FluidPipeRenderState createRenderState() {
		return new FluidPipeRenderState();
	}

	@Override
	public void extractRenderState(
		FluidPipeBlockEntity blockEntity,
		FluidPipeRenderState state,
		float partialTicks,
		Vec3 cameraPos,
		ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay
	) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPos, crumblingOverlay);
		state.fillStep = FluidFillSteps.step(blockEntity.getAmount(), blockEntity.getCapacity());
		state.fluid = blockEntity.getFluid();
		state.fluidArgb = FluidVisuals.argb(state.fluid);
		BlockState blockState = blockEntity.getBlockState();
		state.north = FluidPipeBlock.isConnected(blockState, Direction.NORTH);
		state.south = FluidPipeBlock.isConnected(blockState, Direction.SOUTH);
		state.east = FluidPipeBlock.isConnected(blockState, Direction.EAST);
		state.west = FluidPipeBlock.isConnected(blockState, Direction.WEST);
		state.up = FluidPipeBlock.isConnected(blockState, Direction.UP);
		state.down = FluidPipeBlock.isConnected(blockState, Direction.DOWN);
	}

	@Override
	public void submit(
		FluidPipeRenderState state,
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		CameraRenderState camera
	) {
		float fill = FluidFillSteps.fillRatio(state.fillStep);
		if (fill <= 0.0F) {
			return;
		}

		float y0 = INNER_MIN;
		float y1 = INNER_MIN + (INNER_MAX - INNER_MIN) * fill;
		int argb = state.fluidArgb;
		int light = state.lightCoords;

		// Core
		FluidVolumeGeometry.submitBox(
			poseStack, submitNodeCollector,
			INNER_MIN, y0, INNER_MIN, INNER_MAX, y1, INNER_MAX,
			argb, light
		);

		float armFillY1 = OUTER_MIN + (OUTER_MAX - OUTER_MIN) * fill;
		if (state.north) {
			FluidVolumeGeometry.submitBox(
				poseStack, submitNodeCollector,
				INNER_MIN, y0, 0.0F, INNER_MAX, y1, INNER_MIN,
				argb, light
			);
		}
		if (state.south) {
			FluidVolumeGeometry.submitBox(
				poseStack, submitNodeCollector,
				INNER_MIN, y0, INNER_MAX, INNER_MAX, y1, 1.0F,
				argb, light
			);
		}
		if (state.west) {
			FluidVolumeGeometry.submitBox(
				poseStack, submitNodeCollector,
				0.0F, y0, INNER_MIN, INNER_MIN, y1, INNER_MAX,
				argb, light
			);
		}
		if (state.east) {
			FluidVolumeGeometry.submitBox(
				poseStack, submitNodeCollector,
				INNER_MAX, y0, INNER_MIN, 1.0F, y1, INNER_MAX,
				argb, light
			);
		}
		if (state.down) {
			FluidVolumeGeometry.submitBox(
				poseStack, submitNodeCollector,
				INNER_MIN, 0.0F, INNER_MIN, INNER_MAX, INNER_MIN, INNER_MAX,
				argb, light
			);
		}
		if (state.up) {
			FluidVolumeGeometry.submitBox(
				poseStack, submitNodeCollector,
				INNER_MIN, INNER_MAX, INNER_MIN, INNER_MAX, armFillY1, INNER_MAX,
				argb, light
			);
		}
	}
}
