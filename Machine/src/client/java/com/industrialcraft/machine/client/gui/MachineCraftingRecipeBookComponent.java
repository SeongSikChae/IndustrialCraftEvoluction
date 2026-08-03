package com.industrialcraft.machine.client.gui;

import com.industrialcraft.machine.block.ModBlocks;
import com.industrialcraft.machine.recipe.ModRecipeBookCategories;
import java.util.List;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.recipebook.PlaceRecipeHelper;

public class MachineCraftingRecipeBookComponent extends RecipeBookComponent<AbstractCraftingMenu> {
	private static final WidgetSprites FILTER_BUTTON_SPRITES = new WidgetSprites(
		Identifier.withDefaultNamespace("recipe_book/filter_enabled"),
		Identifier.withDefaultNamespace("recipe_book/filter_disabled"),
		Identifier.withDefaultNamespace("recipe_book/filter_enabled_highlighted"),
		Identifier.withDefaultNamespace("recipe_book/filter_disabled_highlighted")
	);
	private static final Component ONLY_CRAFTABLES_TOOLTIP = Component.translatable("gui.recipebook.toggleRecipes.craftable");
	private static final List<TabInfo> TABS = List.of(
		new TabInfo(ModBlocks.MACHINE_CRAFTING_TABLE.asItem(), ModRecipeBookCategories.MACHINE)
	);

	public MachineCraftingRecipeBookComponent(AbstractCraftingMenu menu) {
		super(menu, TABS);
	}

	@Override
	protected boolean isCraftingSlot(Slot slot) {
		return this.menu.getResultSlot() == slot || this.menu.getInputGridSlots().contains(slot);
	}

	@Override
	protected void fillGhostRecipe(GhostSlots ghostSlots, RecipeDisplay display, ContextMap context) {
		ghostSlots.setResult(this.menu.getResultSlot(), context, display.result());

		if (display instanceof ShapedCraftingRecipeDisplay shaped) {
			List<Slot> inputSlots = this.menu.getInputGridSlots();
			PlaceRecipeHelper.placeRecipe(
				this.menu.getGridWidth(),
				this.menu.getGridHeight(),
				shaped.width(),
				shaped.height(),
				shaped.ingredients(),
				(SlotDisplay ingredient, int gridIndex, int gridX, int gridY) ->
					ghostSlots.setInput(inputSlots.get(gridIndex), context, ingredient)
			);
		} else if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
			List<Slot> inputSlots = this.menu.getInputGridSlots();
			int slotCount = Math.min(shapeless.ingredients().size(), inputSlots.size());
			for (int i = 0; i < slotCount; i++) {
				ghostSlots.setInput(inputSlots.get(i), context, shapeless.ingredients().get(i));
			}
		}
	}

	@Override
	protected WidgetSprites getFilterButtonTextures() {
		return FILTER_BUTTON_SPRITES;
	}

	@Override
	protected Component getRecipeFilterName() {
		return ONLY_CRAFTABLES_TOOLTIP;
	}

	@Override
	protected void selectMatchingRecipes(RecipeCollection collection, StackedItemContents stackedContents) {
		collection.selectRecipes(stackedContents, this::canDisplay);
	}

	private boolean canDisplay(RecipeDisplay display) {
		int gridWidth = this.menu.getGridWidth();
		int gridHeight = this.menu.getGridHeight();
		if (display instanceof ShapedCraftingRecipeDisplay shaped) {
			return gridWidth >= shaped.width() && gridHeight >= shaped.height();
		}
		if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
			return gridWidth * gridHeight >= shapeless.ingredients().size();
		}
		return false;
	}
}
