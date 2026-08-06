package com.industrialcraft.showcase.gametest;

import java.nio.file.Path;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;

/**
 * Writes numbered PNG frames for ffmpeg ({@code frame_%04d.png}).
 */
@SuppressWarnings("UnstableApiUsage")
public final class FrameCapture {
	public static final int DEFAULT_WIDTH = 1280;
	public static final int DEFAULT_HEIGHT = 720;
	/** Capture every N game ticks → at 20 TPS, N=2 yields 10 fps video. */
	public static final int DEFAULT_TICKS_PER_FRAME = 2;

	private final ClientGameTestContext context;
	private final Path outDir;
	private final int width;
	private final int height;
	private final int ticksPerFrame;
	private int index;

	public FrameCapture(ClientGameTestContext context, String clipId) {
		this(context, clipId, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_TICKS_PER_FRAME);
	}

	public FrameCapture(
		ClientGameTestContext context,
		String clipId,
		int width,
		int height,
		int ticksPerFrame
	) {
		this.context = context;
		this.outDir = Path.of("docs", "clips", clipId);
		this.width = width;
		this.height = height;
		this.ticksPerFrame = ticksPerFrame;
		this.index = 0;
	}

	public Path outDir() {
		return outDir;
	}

	public int index() {
		return index;
	}

	public Path capture() {
		String name = String.format("frame_%04d", index++);
		Path saved = context.takeScreenshot(
			TestScreenshotOptions.of(name)
				.disableCounterPrefix()
				.withDestinationDir(outDir)
				.withSize(width, height)
		);
		if (ticksPerFrame > 0) {
			context.waitTicks(ticksPerFrame);
		}
		return saved;
	}
}
