package com.industrialcraft.showcase.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Records lignite / sub-bituminous / bituminous / anthracite clips (one world per grade).
 */
@SuppressWarnings("UnstableApiUsage")
public class CoalGradeShowcaseClipTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		for (FuelGradeSpec spec : FuelGradeSpecs.COAL_SERIES) {
			FuelGradeShowcaseClip clip = new FuelGradeShowcaseClip(spec);
			try (TestSingleplayerContext world = ShowcaseWorlds.open(context)) {
				clip.setup(context, world);
				FrameCapture frames = new FrameCapture(context, clip.id());
				clip.record(context, world, frames);
				System.out.println(spec.clipId() + " showcase complete: " + frames.index()
					+ " frames → " + frames.outDir().toAbsolutePath());
			}
		}
	}
}
