package com.industrialcraft.showcase.gametest;

import java.util.Set;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Positions the local player as a documentation camera.
 * Rotation is applied in one teleport (no lookAt snap) to avoid frame jitter.
 */
@SuppressWarnings("UnstableApiUsage")
public final class Camera {
	private Camera() {
	}

	public static void moveLook(
		ClientGameTestContext context,
		TestSingleplayerContext world,
		Vec3 eye,
		Vec3 lookTarget,
		int settleTicks,
		boolean waitForRender
	) {
		Vec3 delta = lookTarget.subtract(eye);
		double dist = delta.length();
		if (dist < 1.0E-4) {
			dist = 1.0E-4;
		}
		float yaw = (float) (Math.toDegrees(Math.atan2(-delta.x, delta.z)));
		float pitch = (float) (Math.toDegrees(-Math.asin(delta.y / dist)));

		world.getServer().runOnServer(server -> {
			ServerPlayer player = world.getConnection().getServerPlayer();
			player.teleportTo(
				world.getConnection().getServerLevel(),
				eye.x,
				eye.y - player.getEyeHeight(),
				eye.z,
				Set.of(),
				yaw,
				pitch,
				false
			);
		});
		world.getConnection().waitForClientboundPackets();
		context.waitTicks(2);
		if (waitForRender) {
			world.getConnection().waitForChunksRender();
		}
		if (settleTicks > 0) {
			context.waitTicks(settleTicks);
		}
	}

	/** Orbit around {@code center} in the XZ plane; yawAngleDeg is counter-clockwise from +Z. */
	public static Vec3 orbitEye(Vec3 center, double radius, double heightOffset, double yawAngleDeg) {
		double rad = Math.toRadians(yawAngleDeg);
		double x = center.x + Math.sin(rad) * radius;
		double z = center.z + Math.cos(rad) * radius;
		return new Vec3(x, center.y + heightOffset, z);
	}

	@Deprecated
	public static void moveLook(
		ClientGameTestContext context,
		TestSingleplayerContext world,
		Vec3 eye,
		BlockPos lookTarget,
		int settleTicks,
		boolean waitForRender
	) {
		moveLook(context, world, eye, Vec3.atCenterOf(lookTarget), settleTicks, waitForRender);
	}
}
