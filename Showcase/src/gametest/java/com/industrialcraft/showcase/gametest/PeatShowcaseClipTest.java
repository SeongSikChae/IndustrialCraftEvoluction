package com.industrialcraft.showcase.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Records peat showcase via shared {@link FuelGradeShowcaseClip}.
 */
@SuppressWarnings("UnstableApiUsage")
public class PeatShowcaseClipTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		FuelGradeShowcaseClip clip = new FuelGradeShowcaseClip(FuelGradeSpecs.PEAT);
		try (TestSingleplayerContext world = ShowcaseWorlds.open(context)) {
			clip.setup(context, world);
			FrameCapture frames = new FrameCapture(context, clip.id());
			clip.record(context, world, frames);
			System.out.println("Peat showcase complete: " + frames.index() + " frames → "
				+ frames.outDir().toAbsolutePath());
		}
	}
}
