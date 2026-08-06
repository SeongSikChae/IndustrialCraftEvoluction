package com.industrialcraft.showcase.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Shared ≤30s fuel-grade clip: floor-dropped item + ore + deepslate ore (~24s at 10 fps).
 */
@SuppressWarnings("UnstableApiUsage")
public final class FuelGradeShowcaseClip implements ShowcaseClip {
	static final int FRAMES_PER_SEGMENT = 80;
	private static final double[] ANGLE_YAWS = { 20.0, 40.0, 60.0 };

	private static final BlockPos ORIGIN = new BlockPos(8, -60, 8);
	private static final BlockPos ITEM_DROP_POS = ORIGIN;
	private static final BlockPos ORE_POS = ORIGIN.offset(10, 0, 0);
	private static final BlockPos DEEPSLATE_ORE_POS = ORIGIN.offset(20, 0, 0);

	private final FuelGradeSpec spec;
	private Vec3 itemLookTarget = Vec3.ZERO;

	public FuelGradeShowcaseClip(FuelGradeSpec spec) {
		this.spec = spec;
	}

	@Override
	public String id() {
		return spec.clipId();
	}

	public FuelGradeSpec spec() {
		return spec;
	}

	@Override
	public void setup(ClientGameTestContext context, TestSingleplayerContext world) {
		ShowcaseWorlds.prepareCreativeFlying(world);
		ShowcaseWorlds.clearPlatform(world, ORIGIN.offset(10, 0, 0), 18, 5, Blocks.SMOOTH_STONE);

		itemLookTarget = world.getServer().computeOnServer(server -> {
			ServerLevel level = world.getConnection().getServerLevel();
			ServerPlayer player = world.getConnection().getServerPlayer();
			player.getInventory().clearContent();

			Vec3 drop = new Vec3(
				ITEM_DROP_POS.getX() + 0.5,
				ITEM_DROP_POS.getY() + 0.35,
				ITEM_DROP_POS.getZ() + 0.5
			);
			Vec3 look = ItemDrops.dropOnFloor(level, new ItemStack(spec.item()), drop)
				.add(0.0, -0.15, 0.0);

			level.setBlock(ORE_POS.below(), Blocks.STONE.defaultBlockState(), 3);
			level.setBlock(ORE_POS, spec.ore().defaultBlockState(), 3);

			level.setBlock(DEEPSLATE_ORE_POS.below(), Blocks.DEEPSLATE.defaultBlockState(), 3);
			level.setBlock(DEEPSLATE_ORE_POS, spec.deepslateOre().defaultBlockState(), 3);
			return look;
		});
		world.getConnection().waitForClientboundPackets();
		world.getConnection().waitForChunksDownload();
		world.getConnection().waitForChunksRender();
		context.waitTicks(40);
		Captions.prepareFrame(context);
		context.waitTicks(10);
	}

	@Override
	public void record(ClientGameTestContext context, TestSingleplayerContext world, FrameCapture frames) {
		recordSegment(context, world, frames, itemLookTarget, 2.2, 0.55);
		recordSegment(context, world, frames, Vec3.atCenterOf(ORE_POS), 2.8, 0.25);
		recordSegment(context, world, frames, Vec3.atCenterOf(DEEPSLATE_ORE_POS), 2.8, 0.25);
	}

	private static void recordSegment(
		ClientGameTestContext context,
		TestSingleplayerContext world,
		FrameCapture frames,
		Vec3 subject,
		double radius,
		double heightOffset
	) {
		int framesPerAngle = FRAMES_PER_SEGMENT / ANGLE_YAWS.length;
		int captured = 0;
		for (int a = 0; a < ANGLE_YAWS.length; a++) {
			int count = (a == ANGLE_YAWS.length - 1)
				? (FRAMES_PER_SEGMENT - captured)
				: framesPerAngle;
			Vec3 eye = Camera.orbitEye(subject, radius, heightOffset, ANGLE_YAWS[a]);
			Camera.moveLook(context, world, eye, subject, 20, true);
			Captions.hideHud(context);
			for (int i = 0; i < count; i++) {
				Captions.hideHud(context);
				System.out.println("Saved frame: " + frames.capture().toAbsolutePath());
			}
			captured += count;
		}
	}
}
