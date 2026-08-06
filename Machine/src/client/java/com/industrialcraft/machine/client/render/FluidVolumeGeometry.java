package com.industrialcraft.machine.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/**
 * Shared solid fluid box submission for reservoir / pipe BER.
 */
public final class FluidVolumeGeometry {
	private FluidVolumeGeometry() {
	}

	public static void submitBox(
		PoseStack poseStack,
		SubmitNodeCollector collector,
		float x0,
		float y0,
		float z0,
		float x1,
		float y1,
		float z1,
		int argb,
		int lightCoords
	) {
		if (y1 <= y0) {
			return;
		}
		collector.submitCustomGeometry(
			poseStack,
			RenderTypes.debugFilledBox(),
			(pose, consumer) -> putBox(pose, consumer, x0, y0, z0, x1, y1, z1, argb)
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
