package com.industrialcraft.machine.menu;

import com.industrialcraft.machine.block.entity.FurnaceEngineBlockEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FurnaceEngineMenu extends AbstractContainerMenu {
	public static final int FUEL_SLOT = FurnaceEngineBlockEntity.FUEL_SLOT;
	public static final int GOVERNOR_SLOT = FurnaceEngineBlockEntity.GOVERNOR_SLOT;
	private static final int FUEL_SLOT_X = 134;
	private static final int FUEL_SLOT_Y = 53;
	private static final int GOVERNOR_SLOT_X = 110;
	private static final int GOVERNOR_SLOT_Y = 53;
	/** Slot caption centers (18×18 slots). */
	public static final int FUEL_SLOT_LABEL_X = FUEL_SLOT_X + 9;
	public static final int GOVERNOR_SLOT_LABEL_X = GOVERNOR_SLOT_X + 9;
	public static final int SLOT_LABEL_Y = 72;
	private static final int INVENTORY_START_X = 8;
	private static final int INVENTORY_START_Y = 84;
	private static final int PLAYER_INV_START = FurnaceEngineBlockEntity.CONTAINER_SIZE;

	private final Container container;
	private final ContainerData data;

	public FurnaceEngineMenu(int containerId, Inventory inventory) {
		this(
			containerId,
			inventory,
			new SimpleContainer(FurnaceEngineBlockEntity.CONTAINER_SIZE),
			new SimpleContainerData(FurnaceEngineBlockEntity.DATA_COUNT)
		);
	}

	public FurnaceEngineMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
		super(ModMenus.FURNACE_ENGINE, containerId);
		checkContainerSize(container, FurnaceEngineBlockEntity.CONTAINER_SIZE);
		checkContainerDataCount(data, FurnaceEngineBlockEntity.DATA_COUNT);
		this.container = container;
		this.data = data;

		container.startOpen(inventory.player);
		this.addSlot(new Slot(container, FUEL_SLOT, FUEL_SLOT_X, FUEL_SLOT_Y) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return FurnaceEngineBlockEntity.isFuel(stack);
			}
		});
		this.addSlot(new Slot(container, GOVERNOR_SLOT, GOVERNOR_SLOT_X, GOVERNOR_SLOT_Y) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return FurnaceEngineBlockEntity.isGovernor(stack);
			}

			@Override
			public int getMaxStackSize() {
				return 1;
			}
		});
		this.addStandardInventorySlots(inventory, INVENTORY_START_X, INVENTORY_START_Y);
		this.addDataSlots(data);
	}

	public float getLitProgress() {
		int burnTime = this.data.get(FurnaceEngineBlockEntity.DATA_BURN_TIME);
		int burnDuration = this.data.get(FurnaceEngineBlockEntity.DATA_BURN_DURATION);
		if (burnDuration == 0) {
			return 0.0F;
		}
		return Mth.clamp((float) burnTime / (float) burnDuration, 0.0F, 1.0F);
	}

	public boolean isLit() {
		return this.data.get(FurnaceEngineBlockEntity.DATA_BURN_TIME) > 0;
	}

	public float getSpinFactor() {
		return this.data.get(FurnaceEngineBlockEntity.DATA_SPIN_MILLI) / (float) FurnaceEngineBlockEntity.SPIN_MILLI_MAX;
	}

	public boolean hasGovernor() {
		return FurnaceEngineBlockEntity.isGovernor(this.container.getItem(GOVERNOR_SLOT));
	}

	public float getStoredThrottle() {
		return this.data.get(FurnaceEngineBlockEntity.DATA_THROTTLE_MILLI)
			/ (float) FurnaceEngineBlockEntity.THROTTLE_MILLI_MAX;
	}

	public float getEffectiveThrottle() {
		return this.data.get(FurnaceEngineBlockEntity.DATA_APPLIED_THROTTLE_MILLI)
			/ (float) FurnaceEngineBlockEntity.THROTTLE_MILLI_MAX;
	}

	public int getThrottlePercent() {
		return Mth.clamp(
			Math.round(this.getStoredThrottle() * 100.0F),
			FurnaceEngineBlockEntity.THROTTLE_PERCENT_MIN,
			FurnaceEngineBlockEntity.THROTTLE_PERCENT_MAX
		);
	}

	public float getOutputScale() {
		return FurnaceEngineBlockEntity.outputScale(this.getSpinFactor(), this.getEffectiveThrottle());
	}

	public double getTorque() {
		return FurnaceEngineBlockEntity.TORQUE * this.getOutputScale();
	}

	public double getOmega() {
		return FurnaceEngineBlockEntity.OMEGA * this.getOutputScale();
	}

	public double getPower() {
		return this.getTorque() * this.getOmega();
	}

	@Override
	public boolean clickMenuButton(Player player, int buttonId) {
		if (!this.hasGovernor()
			|| buttonId < FurnaceEngineBlockEntity.THROTTLE_PERCENT_MIN
			|| buttonId > FurnaceEngineBlockEntity.THROTTLE_PERCENT_MAX) {
			return false;
		}
		if (this.container instanceof FurnaceEngineBlockEntity engine) {
			engine.setThrottlePercent(buttonId);
			return true;
		}
		return false;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex) {
		ItemStack clicked = ItemStack.EMPTY;
		Slot slot = this.slots.get(slotIndex);
		if (slot.hasItem()) {
			ItemStack stack = slot.getItem();
			clicked = stack.copy();
			if (slotIndex < PLAYER_INV_START) {
				if (!this.moveItemStackTo(stack, PLAYER_INV_START, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (FurnaceEngineBlockEntity.isFuel(stack)) {
				if (!this.moveItemStackTo(stack, FUEL_SLOT, FUEL_SLOT + 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (FurnaceEngineBlockEntity.isGovernor(stack)) {
				if (!this.moveItemStackTo(stack, GOVERNOR_SLOT, GOVERNOR_SLOT + 1, false)) {
					return ItemStack.EMPTY;
				}
			} else {
				return ItemStack.EMPTY;
			}

			if (stack.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}
		return clicked;
	}

	@Override
	public boolean stillValid(Player player) {
		return this.container.stillValid(player);
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.container.stopOpen(player);
	}
}
