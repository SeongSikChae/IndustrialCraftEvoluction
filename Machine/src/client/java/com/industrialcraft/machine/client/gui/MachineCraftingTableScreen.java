package com.industrialcraft.machine.client.gui;

import com.industrialcraft.machine.menu.MachineCraftingTableMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class MachineCraftingTableScreen extends AbstractRecipeBookScreen<MachineCraftingTableMenu> {
	private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/crafting_table.png");

	public MachineCraftingTableScreen(MachineCraftingTableMenu menu, Inventory inventory, Component title) {
		super(menu, new MachineCraftingRecipeBookComponent(menu), inventory, title);
	}

	@Override
	protected void init() {
		super.init();
		this.titleLabelX = 29;
	}

	@Override
	protected ScreenPosition getRecipeBookButtonPosition() {
		return new ScreenPosition(this.leftPos + 5, this.height / 2 - 49);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		int xo = this.leftPos;
		int yo = (this.height - this.imageHeight) / 2;
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xo, yo, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
	}
}
