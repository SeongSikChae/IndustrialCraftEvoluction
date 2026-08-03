package com.industrialcraft.machine.client.gui;

import com.industrialcraft.machine.MachineMod;
import com.industrialcraft.machine.menu.FurnaceEngineMenu;
import com.industrialcraft.machine.util.MetricFormat;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
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
	}
}
