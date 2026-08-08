package com.industrialcraft.machine.client.gui;

import com.industrialcraft.machine.MachineMod;
import com.industrialcraft.machine.fluid.FluidUnits;
import com.industrialcraft.machine.fluid.FluidVisuals;
import com.industrialcraft.machine.menu.ReservoirMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class ReservoirScreen extends AbstractContainerScreen<ReservoirMenu> {
	private static final Identifier TEXTURE = MachineMod.id("textures/gui/container/reservoir.png");
	private static final int LABEL_COLOR = 0xFF404040;
	/** Left gauge well matches textures/gui/container/reservoir.png */
	private static final int GAUGE_X = 26;
	private static final int GAUGE_Y = 18;
	private static final int GAUGE_WIDTH = 12;
	private static final int GAUGE_HEIGHT = 46;
	private static final int GAUGE_BG = 0xFF202020;
	/** Below left gauge; above the player-inventory title. */
	private static final int LABEL_X = 8;
	private static final int LABEL_Y = 66;

	public ReservoirScreen(ReservoirMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 176, 166);
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
		// Default inventoryLabelY is imageHeight - 94 (=72); keep it clear of the FU line.
		this.inventoryLabelY = 74;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		int xo = this.leftPos;
		int yo = this.topPos;
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xo, yo, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

		int gx = xo + GAUGE_X;
		int gy = yo + GAUGE_Y;
		graphics.fill(gx, gy, gx + GAUGE_WIDTH, gy + GAUGE_HEIGHT, GAUGE_BG);

		float progress = this.menu.getFillProgress();
		if (progress > 0.0F) {
			int fillHeight = Mth.ceil(GAUGE_HEIGHT * progress);
			int color = FluidVisuals.argb(this.menu.getFluid()) | 0xFF000000;
			graphics.fill(gx + 1, gy + GAUGE_HEIGHT - fillHeight, gx + GAUGE_WIDTH - 1, gy + GAUGE_HEIGHT, color);
		}
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		super.extractLabels(graphics, mouseX, mouseY);
		graphics.text(
			this.font,
			Component.translatable(
				"gui.machine.reservoir.amount",
				this.menu.getAmountFuLabel(),
				this.menu.getCapacityFuLabel()
			),
			LABEL_X,
			LABEL_Y,
			LABEL_COLOR,
			false
		);
	}
}
