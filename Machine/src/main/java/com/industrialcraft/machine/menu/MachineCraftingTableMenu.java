package com.industrialcraft.machine.menu;

import com.industrialcraft.machine.block.ModBlocks;
import com.industrialcraft.machine.recipe.MachineShapedRecipe;
import com.industrialcraft.machine.recipe.ModRecipes;
import java.util.List;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class MachineCraftingTableMenu extends AbstractCraftingMenu {
	public static final int RESULT_SLOT = 0;
	public static final int CRAFT_SLOT_START = 1;
	public static final int CRAFT_SLOT_END = 10;
	public static final int INV_SLOT_START = 10;
	public static final int INV_SLOT_END = 46;

	private final ContainerLevelAccess access;
	private final Player player;
	private boolean placingRecipe;

	public MachineCraftingTableMenu(int containerId, Inventory inventory) {
		this(containerId, inventory, ContainerLevelAccess.NULL);
	}

	public MachineCraftingTableMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
		super(ModMenus.MACHINE_CRAFTING_TABLE, containerId, 3, 3);
		this.access = access;
		this.player = inventory.player;

		this.addSlot(new MachineCraftingResultSlot(this.player, this.craftSlots, this.resultSlots, 0, 124, 35));
		this.addCraftingGridSlots(30, 17);
		this.addStandardInventorySlots(inventory, 8, 84);
	}

	protected static void slotChangedCraftingGrid(
		AbstractCraftingMenu menu,
		ServerLevel level,
		Player player,
		CraftingContainer craftSlots,
		ResultContainer resultSlots
	) {
		CraftingInput input = craftSlots.asCraftInput();
		ServerPlayer serverPlayer = (ServerPlayer) player;
		ItemStack result = ItemStack.EMPTY;

		RecipeHolder<MachineShapedRecipe> holder = level.getServer()
			.getRecipeManager()
			.getRecipeFor(ModRecipes.MACHINE_CRAFTING, input, level)
			.orElse(null);

		if (holder != null && resultSlots.setRecipeUsed(serverPlayer, holder)) {
			ItemStack assembled = holder.value().assemble(input);
			if (assembled.isItemEnabled(level.enabledFeatures())) {
				result = assembled;
			}
		}

		resultSlots.setItem(0, result);
		menu.setRemoteSlot(RESULT_SLOT, result);
		serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), RESULT_SLOT, result));
	}

	@Override
	public void slotsChanged(net.minecraft.world.Container container) {
		if (this.placingRecipe) {
			return;
		}
		this.access.execute((level, pos) -> {
			if (level instanceof ServerLevel serverLevel) {
				slotChangedCraftingGrid(this, serverLevel, this.player, this.craftSlots, this.resultSlots);
			}
		});
	}

	@Override
	protected void beginPlacingRecipe() {
		this.placingRecipe = true;
	}

	@Override
	protected void finishPlacingRecipe(ServerLevel level, RecipeHolder<CraftingRecipe> recipe) {
		this.placingRecipe = false;
		slotChangedCraftingGrid(this, level, this.player, this.craftSlots, this.resultSlots);
	}

	@Override
	public RecipeBookMenu.PostPlaceAction handlePlacement(
		boolean useMaxItems,
		boolean craftAll,
		RecipeHolder<?> recipe,
		ServerLevel level,
		Inventory inventory
	) {
		if (!(recipe.value() instanceof MachineShapedRecipe)) {
			return RecipeBookMenu.PostPlaceAction.NOTHING;
		}

		@SuppressWarnings("unchecked")
		RecipeHolder<MachineShapedRecipe> machineRecipe = (RecipeHolder<MachineShapedRecipe>) recipe;

		this.beginPlacingRecipe();
		try {
			List<Slot> inputSlots = this.getInputGridSlots();
			return ServerPlaceRecipe.placeRecipe(
				new ServerPlaceRecipe.CraftingMenuAccess<>() {
					@Override
					public void fillCraftSlotsStackedContents(StackedItemContents contents) {
						MachineCraftingTableMenu.this.fillCraftSlotsStackedContents(contents);
					}

					@Override
					public void clearCraftingContent() {
						MachineCraftingTableMenu.this.resultSlots.clearContent();
						MachineCraftingTableMenu.this.craftSlots.clearContent();
					}

					@Override
					public boolean recipeMatches(RecipeHolder<MachineShapedRecipe> holder) {
						return holder.value().matches(
							MachineCraftingTableMenu.this.craftSlots.asCraftInput(),
							MachineCraftingTableMenu.this.owner().level()
						);
					}
				},
				this.getGridWidth(),
				this.getGridHeight(),
				inputSlots,
				inputSlots,
				inventory,
				machineRecipe,
				useMaxItems,
				craftAll
			);
		} finally {
			this.finishPlacingRecipe(level, null);
		}
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.access.execute((level, pos) -> this.clearContainer(player, this.craftSlots));
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(this.access, player, ModBlocks.MACHINE_CRAFTING_TABLE);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex) {
		ItemStack clicked = ItemStack.EMPTY;
		Slot slot = this.slots.get(slotIndex);
		if (slot == null || !slot.hasItem()) {
			return clicked;
		}

		ItemStack stack = slot.getItem();
		clicked = stack.copy();

		if (slotIndex == RESULT_SLOT) {
			stack.getItem().onCraftedBy(stack, player);
			if (!this.moveItemStackTo(stack, INV_SLOT_START, INV_SLOT_END, true)) {
				return ItemStack.EMPTY;
			}
			slot.onQuickCraft(stack, clicked);
		} else if (slotIndex >= INV_SLOT_START && slotIndex < INV_SLOT_END) {
			if (!this.moveItemStackTo(stack, CRAFT_SLOT_START, CRAFT_SLOT_END, false)) {
				if (slotIndex < 37) {
					if (!this.moveItemStackTo(stack, 37, INV_SLOT_END, false)) {
						return ItemStack.EMPTY;
					}
				} else if (!this.moveItemStackTo(stack, INV_SLOT_START, 37, false)) {
					return ItemStack.EMPTY;
				}
			}
		} else if (!this.moveItemStackTo(stack, INV_SLOT_START, INV_SLOT_END, false)) {
			return ItemStack.EMPTY;
		}

		if (stack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}

		if (stack.getCount() == clicked.getCount()) {
			return ItemStack.EMPTY;
		}

		slot.onTake(player, stack);
		if (slotIndex == RESULT_SLOT) {
			player.drop(stack, false);
		}

		return clicked;
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
		return slot.container != this.resultSlots && super.canTakeItemForPickAll(stack, slot);
	}

	@Override
	public Slot getResultSlot() {
		return this.slots.get(RESULT_SLOT);
	}

	@Override
	public List<Slot> getInputGridSlots() {
		return this.slots.subList(CRAFT_SLOT_START, CRAFT_SLOT_END);
	}

	@Override
	public RecipeBookType getRecipeBookType() {
		// Reuses vanilla CRAFTING open/filter flags; machine recipes use a separate RecipeBookCategory.
		return RecipeBookType.CRAFTING;
	}

	@Override
	protected Player owner() {
		return this.player;
	}
}
