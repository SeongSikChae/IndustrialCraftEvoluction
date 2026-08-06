package com.industrialcraft.showcase.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Contract for a documentation showcase or gameplay scenario clip.
 * Setup places content; record writes a numbered PNG frame sequence under {@code docs/clips/<id>/}.
 */
@SuppressWarnings("UnstableApiUsage")
public interface ShowcaseClip {
	/** Clip folder name under {@code docs/clips/} (e.g. {@code peat}). */
	String id();

	void setup(ClientGameTestContext context, TestSingleplayerContext world);

	void record(ClientGameTestContext context, TestSingleplayerContext world, FrameCapture frames);
}
