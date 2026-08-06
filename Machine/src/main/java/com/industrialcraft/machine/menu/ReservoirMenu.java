package com.industrialcraft.machine.menu;

import com.industrialcraft.machine.block.entity.ReservoirBlockEntity;
import com.industrialcraft.machine.fluid.FluidBuckets;
import com.industrialcraft.machine.fluid.FluidUnits;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class ReservoirMenu extends AbstractContainerMenu {
	public static final int BUCKET_SLOT = ReservoirBlockEntity.BUCKET_SLOT;
	private static final int BUCKET_SLOT_X = 80;
	private static final int BUCKET_SLOT_Y = 35;
	private static final int INVENTORY_START_X = 8;
	private static final int INVENTORY_START_Y = 84;
	private static final int PLAYER_INV_START = ReservoirBlockEntity.CONTAINER_SIZE;

	private final Container container;
	private final ContainerData data;

	public ReservoirMenu(int containerId, Inventory inventory) {
		this(
			containerId,
			inventory,
			new SimpleContainer(ReservoirBlockEntity.CONTAINER_SIZE),
			new SimpleContainerData(ReservoirBlockEntity.DATA_COUNT)
		);
	}

	public ReservoirMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
		super(ModMenus.RESERVOIR, containerId);
		checkContainerSize(container, ReservoirBlockEntity.CONTAINER_SIZE);
		checkContainerDataCount(data, ReservoirBlockEntity.DATA_COUNT);
		this.container = container;
		this.data = data;

		container.startOpen(inventory.player);
		this.addSlot(new Slot(container, BUCKET_SLOT, BUCKET_SLOT_X, BUCKET_SLOT_Y) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return FluidBuckets.isFilledBucket(stack);
			}

			@Override
			public int getMaxStackSize() {
				return 1;
			}
		});
		this.addStandardInventorySlots(inventory, INVENTORY_START_X, INVENTORY_START_Y);
		this.addDataSlots(data);
	}

	/**
	 * Container set-data packets still carry values as a signed short.
	 * Reconstruct unsigned 16-bit so amounts above 32767 mB (≈32.8 FU) do not appear negative.
	 * Reservoir capacity is 64000 mB, which fits in 0..65535.
	 */
	public int getAmountMb() {
		return this.data.get(ReservoirBlockEntity.DATA_AMOUNT) & 0xFFFF;
	}

	public Fluid getFluid() {
		Fluid fluid = BuiltInRegistries.FLUID.byId(this.data.get(ReservoirBlockEntity.DATA_FLUID_ID));
		return fluid != null ? fluid : Fluids.EMPTY;
	}

	public float getFillProgress() {
		return Mth.clamp(this.getAmountMb() / (float) FluidUnits.RESERVOIR_CAPACITY_MB, 0.0F, 1.0F);
	}

	public String getAmountFuLabel() {
		return FluidUnits.formatFu(this.getAmountMb());
	}

	public String getCapacityFuLabel() {
		return FluidUnits.formatFu(FluidUnits.RESERVOIR_CAPACITY_MB);
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
			} else if (FluidBuckets.isFilledBucket(stack)) {
				if (!this.moveItemStackTo(stack, BUCKET_SLOT, BUCKET_SLOT + 1, false)) {
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
