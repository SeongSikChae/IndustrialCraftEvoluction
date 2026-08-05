package com.industrialcraft.machine.client.gui;

import com.industrialcraft.machine.MachineMod;
import com.industrialcraft.machine.block.entity.FurnaceEngineBlockEntity;
import com.industrialcraft.machine.menu.FurnaceEngineMenu;
import com.industrialcraft.machine.util.MetricFormat;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class FurnaceEngineScreen extends AbstractContainerScreen<FurnaceEngineMenu> {
	private static final Identifier TEXTURE = MachineMod.id("textures/gui/container/furnace_engine.png");
	private static final Identifier LIT_PROGRESS_SPRITE = Identifier.withDefaultNamespace("container/furnace/lit_progress");
	private static final int LABEL_COLOR = 0xFF404040;
	private static final int STATS_X = 8;
	private static final int STATS_Y = 20;
	private static final int FLAME_SIZE = 14;
	private static final int FLAME_X = 135;
	private static final int FLAME_Y = 36;

	private static final int SLIDER_X = 8;
	private static final int SLIDER_Y = 66;
	private static final int SLIDER_WIDTH = 90;
	private static final int SLIDER_HEIGHT = 8;
	private static final int KNOB_WIDTH = 4;
	private static final int TRACK_COLOR = 0xFF373737;
	private static final int FILL_COLOR = 0xFF8B8B8B;
	private static final int KNOB_COLOR = 0xFFFFFFFF;
	private static final int KNOB_SHADOW = 0xFF555555;

	private boolean draggingThrottle;
	private int localThrottlePercent = -1;

	public FurnaceEngineScreen(FurnaceEngineMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 176, 166);
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		int xo = this.leftPos;
		int yo = this.topPos;
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xo, yo, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

		if (this.menu.isLit()) {
			int litHeight = Mth.ceil(this.menu.getLitProgress() * 13.0F) + 1;
			graphics.blitSprite(
				RenderPipelines.GUI_TEXTURED,
				LIT_PROGRESS_SPRITE,
				FLAME_SIZE,
				FLAME_SIZE,
				0,
				FLAME_SIZE - litHeight,
				xo + FLAME_X,
				yo + FLAME_Y + FLAME_SIZE - litHeight,
				FLAME_SIZE,
				litHeight
			);
		}

		if (this.menu.hasGovernor()) {
			this.renderThrottleSlider(graphics, xo, yo);
		}
	}

	private int displayedThrottlePercent() {
		if (this.draggingThrottle && this.localThrottlePercent >= 0) {
			return this.localThrottlePercent;
		}
		return this.menu.getThrottlePercent();
	}

	private void renderThrottleSlider(GuiGraphicsExtractor graphics, int xo, int yo) {
		int x = xo + SLIDER_X;
		int y = yo + SLIDER_Y;
		graphics.fill(x, y, x + SLIDER_WIDTH, y + SLIDER_HEIGHT, TRACK_COLOR);

		int percent = this.displayedThrottlePercent();
		int fillWidth = Mth.ceil(SLIDER_WIDTH * percent / 100.0F);
		if (fillWidth > 0) {
			graphics.fill(x, y, x + fillWidth, y + SLIDER_HEIGHT, FILL_COLOR);
		}

		int knobTravel = SLIDER_WIDTH - KNOB_WIDTH;
		int knobX = x + Mth.clamp(Math.round(knobTravel * percent / 100.0F), 0, knobTravel);
		graphics.fill(knobX, y - 1, knobX + KNOB_WIDTH, y + SLIDER_HEIGHT + 1, KNOB_SHADOW);
		graphics.fill(knobX, y - 1, knobX + KNOB_WIDTH - 1, y + SLIDER_HEIGHT, KNOB_COLOR);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		super.extractLabels(graphics, mouseX, mouseY);

		int lineHeight = this.font.lineHeight + 2;
		graphics.text(
			this.font,
			Component.translatable("gui.machine.furnace_engine.torque", MetricFormat.formatWithUnit(this.menu.getTorque(), "Nm")),
			STATS_X,
			STATS_Y,
			LABEL_COLOR,
			false
		);
		graphics.text(
			this.font,
			Component.translatable("gui.machine.furnace_engine.omega", MetricFormat.formatWithUnit(this.menu.getOmega(), "rad/s")),
			STATS_X,
			STATS_Y + lineHeight,
			LABEL_COLOR,
			false
		);
		graphics.text(
			this.font,
			Component.translatable("gui.machine.furnace_engine.power", MetricFormat.formatWithUnit(this.menu.getPower(), "W")),
			STATS_X,
			STATS_Y + lineHeight * 2,
			LABEL_COLOR,
			false
		);

		if (this.menu.hasGovernor()) {
			graphics.text(
				this.font,
				Component.translatable("gui.machine.furnace_engine.throttle", this.displayedThrottlePercent()),
				STATS_X,
				SLIDER_Y - this.font.lineHeight - 2,
				LABEL_COLOR,
				false
			);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (this.menu.hasGovernor() && event.button() == 0 && this.isOverThrottleSlider(event.x(), event.y())) {
			this.draggingThrottle = true;
			this.updateThrottleFromMouse(event.x());
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		if (this.draggingThrottle && this.menu.hasGovernor() && event.button() == 0) {
			this.updateThrottleFromMouse(event.x());
			return true;
		}
		return super.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (this.draggingThrottle && event.button() == 0) {
			this.draggingThrottle = false;
			this.localThrottlePercent = -1;
			return true;
		}
		return super.mouseReleased(event);
	}

	private boolean isOverThrottleSlider(double mouseX, double mouseY) {
		int x = this.leftPos + SLIDER_X;
		int y = this.topPos + SLIDER_Y;
		return mouseX >= x - 1 && mouseX < x + SLIDER_WIDTH + 1 && mouseY >= y - 2 && mouseY < y + SLIDER_HEIGHT + 2;
	}

	private void updateThrottleFromMouse(double mouseX) {
		double relative = (mouseX - (this.leftPos + SLIDER_X)) / (double) SLIDER_WIDTH;
		int percent = Mth.clamp(
			(int) Math.round(relative * 100.0),
			FurnaceEngineBlockEntity.THROTTLE_PERCENT_MIN,
			FurnaceEngineBlockEntity.THROTTLE_PERCENT_MAX
		);
		if (percent == this.localThrottlePercent) {
			return;
		}
		this.localThrottlePercent = percent;
		if (this.minecraft != null && this.minecraft.gameMode != null) {
			this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, percent);
		}
	}
}
