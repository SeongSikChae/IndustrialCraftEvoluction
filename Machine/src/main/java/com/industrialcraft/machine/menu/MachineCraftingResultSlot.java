package com.industrialcraft.machine.menu;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;

/**
 * Result slot for Machine crafting-table recipes. Vanilla {@link net.minecraft.world.inventory.ResultSlot}
 * hardcodes {@code RecipeType.CRAFTING} when computing remainders, so this menu needs its own slot.
 */
public class MachineCraftingResultSlot extends Slot {
	private final CraftingContainer craftSlots;
	private final Player player;
	private int removeCount;

	public MachineCraftingResultSlot(Player player, CraftingContainer craftSlots, net.minecraft.world.Container container, int slot, int x, int y) {
		super(container, slot, x, y);
		this.player = player;
		this.craftSlots = craftSlots;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return false;
	}

	@Override
	public ItemStack remove(int amount) {
		if (this.hasItem()) {
			this.removeCount += Math.min(amount, this.getItem().getCount());
		}
		return super.remove(amount);
	}

	@Override
	protected void onQuickCraft(ItemStack stack, int amount) {
		this.removeCount += amount;
		this.checkTakeAchievements(stack);
	}

	@Override
	protected void onSwapCraft(int amount) {
		this.removeCount += amount;
	}

	@Override
	public ItemStack safeClone(Player player) {
		ItemStack stack = super.safeClone(player);
		stack.getItem().onCraftedBy(stack, player);
		return stack;
	}

	@Override
	protected void checkTakeAchievements(ItemStack stack) {
		if (this.removeCount > 0) {
			stack.onCraftedBy(this.player, this.removeCount);
		}

		if (this.container instanceof RecipeCraftingHolder holder) {
			holder.awardUsedRecipes(this.player, this.craftSlots.getItems());
		}

		this.removeCount = 0;
	}

	@Override
	public void onTake(Player player, ItemStack stack) {
		this.checkTakeAchievements(stack);

		CraftingInput.Positioned positioned = this.craftSlots.asPositionedCraftInput();
		CraftingInput input = positioned.input();
		int left = positioned.left();
		int top = positioned.top();
		NonNullList<ItemStack> remaining = CraftingRecipe.defaultCraftingReminder(input);

		for (int row = 0; row < input.height(); row++) {
			for (int col = 0; col < input.width(); col++) {
				int slotIndex = col + left + (row + top) * this.craftSlots.getWidth();
				ItemStack current = this.craftSlots.getItem(slotIndex);
				ItemStack remainder = remaining.get(col + row * input.width());

				if (!current.isEmpty()) {
					this.craftSlots.removeItem(slotIndex, 1);
					current = this.craftSlots.getItem(slotIndex);
				}

				if (remainder.isEmpty()) {
					continue;
				}

				if (current.isEmpty()) {
					this.craftSlots.setItem(slotIndex, remainder);
				} else if (ItemStack.isSameItemSameComponents(current, remainder)) {
					remainder.grow(current.getCount());
					this.craftSlots.setItem(slotIndex, remainder);
				} else if (!this.player.getInventory().add(remainder)) {
					this.player.drop(remainder, false);
				}
			}
		}
	}

	@Override
	public boolean isFake() {
		return true;
	}
}
