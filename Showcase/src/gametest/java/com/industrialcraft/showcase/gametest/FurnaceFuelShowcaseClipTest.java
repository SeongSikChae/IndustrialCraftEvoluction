package com.industrialcraft.showcase.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Records the combined furnace fuel time-lapse clip ({@link FurnaceFuelShowcaseClip}).
 */
@SuppressWarnings("UnstableApiUsage")
public class FurnaceFuelShowcaseClipTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		FurnaceFuelShowcaseClip clip = new FurnaceFuelShowcaseClip();
		try (TestSingleplayerContext world = ShowcaseWorlds.open(context)) {
			clip.setup(context, world);
			FrameCapture frames = new FrameCapture(
				context,
				clip.id(),
				FrameCapture.DEFAULT_WIDTH,
				FrameCapture.DEFAULT_HEIGHT,
				FurnaceFuelShowcaseClip.TIMELAPSE_TICKS_PER_FRAME
			);
			clip.record(context, world, frames);
			System.out.println("Furnace fuel showcase complete: " + frames.index()
				+ " frames → " + frames.outDir().toAbsolutePath());
		}
	}
}
