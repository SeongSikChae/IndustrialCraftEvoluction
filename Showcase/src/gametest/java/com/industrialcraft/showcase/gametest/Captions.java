package com.industrialcraft.showcase.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

/**
 * Hides in-game HUD for clean showcase frames (MC 26.2: {@code Options.hideGui} removed).
 * Captions are burned in at encode time (ffmpeg).
 */
@SuppressWarnings("UnstableApiUsage")
public final class Captions {
	private Captions() {
	}

	public static void hideHud(ClientGameTestContext context) {
		context.runOnClient(client -> {
			var hud = client.gui.hud;
			if (!hud.isHidden()) {
				hud.toggle();
			}
		});
	}

	public static void prepareFrame(ClientGameTestContext context) {
		context.setScreen(() -> null);
		hideHud(context);
		context.waitTicks(2);
		hideHud(context);
	}
}
