package com.industrialcraft.machine.client.gui;

import com.industrialcraft.machine.MachineMod;
import com.industrialcraft.machine.menu.FurnaceEngineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class FurnaceEngineScreen extends AbstractContainerScreen<FurnaceEngineMenu> {
	private static final Identifier TEXTURE = MachineMod.id("textures/gui/container/furnace_engine.png");
	private static final Identifier FURNACE_TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/furnace.png");

	public FurnaceEngineScreen(FurnaceEngineMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 176, 166);
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		int xo = (this.width - this.imageWidth) / 2;
		int yo = (this.height - this.imageHeight) / 2;
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xo, yo, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

		if (this.menu.isLit()) {
			int progress = this.menu.getBurnProgress();
			graphics.blit(
				RenderPipelines.GUI_TEXTURED,
				FURNACE_TEXTURE,
				xo + 81,
				yo + 54 + 12 - progress,
				176.0F,
				12.0F - progress,
				14,
				progress + 1,
				256,
				256
			);
		}
	}
}
