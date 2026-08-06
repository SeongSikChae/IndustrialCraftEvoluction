package com.industrialcraft.machine.client.render;

import com.industrialcraft.machine.block.FluidPipeBlock;
import com.industrialcraft.machine.block.entity.FluidPipeBlockEntity;
import com.industrialcraft.machine.fluid.FluidVisuals;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Draws translucent fluid inside the hollow thin-tube core and connected arms.
 */
public class FluidPipeRenderer implements BlockEntityRenderer<FluidPipeBlockEntity, FluidPipeRenderState> {
	private static final float OUTER_MIN = 6.0F / 16.0F;
	private static final float OUTER_MAX = 10.0F / 16.0F;
	private static final float INNER_MIN = 6.5F / 16.0F;
	private static final float INNER_MAX = 9.5F / 16.0F;
	private static final float ARM_END = 1.0F;
	private static final float ARM_START = 0.0F;

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
		int amount = blockEntity.getAmount();
		state.empty = amount <= 0;
		state.fill = state.empty ? 0.0F : Mth.clamp(amount / (float) blockEntity.getCapacity(), 0.0F, 1.0F);
		state.fluidArgb = FluidVisuals.argb(blockEntity.getFluid());
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
		if (state.empty || state.fill <= 0.0F) {
			return;
		}

		float y0 = INNER_MIN;
		float y1 = INNER_MIN + (INNER_MAX - INNER_MIN) * state.fill;
		int argb = state.fluidArgb;

		submitNodeCollector.submitCustomGeometry(
			poseStack,
			RenderTypes.debugFilledBox(),
			(pose, consumer) -> {
				// Core
				putBox(pose, consumer, INNER_MIN, y0, INNER_MIN, INNER_MAX, y1, INNER_MAX, argb);
				if (state.south) {
					putBox(pose, consumer, INNER_MIN, y0, OUTER_MAX, INNER_MAX, y1, ARM_END, argb);
				}
				if (state.north) {
					putBox(pose, consumer, INNER_MIN, y0, ARM_START, INNER_MAX, y1, OUTER_MIN, argb);
				}
				if (state.east) {
					putBox(pose, consumer, OUTER_MAX, y0, INNER_MIN, ARM_END, y1, INNER_MAX, argb);
				}
				if (state.west) {
					putBox(pose, consumer, ARM_START, y0, INNER_MIN, OUTER_MIN, y1, INNER_MAX, argb);
				}
				if (state.down) {
					float downTop = OUTER_MIN;
					float downBottom = ARM_START;
					float dy0 = downBottom;
					float dy1 = downBottom + (downTop - downBottom) * state.fill;
					putBox(pose, consumer, INNER_MIN, dy0, INNER_MIN, INNER_MAX, dy1, INNER_MAX, argb);
				}
				if (state.up) {
					float upBottom = OUTER_MAX;
					float upTop = ARM_END;
					float uy0 = upBottom;
					float uy1 = upBottom + (upTop - upBottom) * state.fill;
					putBox(pose, consumer, INNER_MIN, uy0, INNER_MIN, INNER_MAX, uy1, INNER_MAX, argb);
				}
			}
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
