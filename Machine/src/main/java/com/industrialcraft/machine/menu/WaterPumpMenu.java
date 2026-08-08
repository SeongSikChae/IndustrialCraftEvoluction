package com.industrialcraft.machine.menu;

import com.industrialcraft.machine.block.entity.WaterPumpBlockEntity;
import com.industrialcraft.machine.fluid.FluidUnits;
import com.industrialcraft.machine.power.SyncedSiFloat;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * Pump telemetry + check-valve accessory slot.
 */
public class WaterPumpMenu extends AbstractContainerMenu {
	public static final int CHECK_VALVE_SLOT = WaterPumpBlockEntity.CHECK_VALVE_SLOT;
	/** Top-right; clear of the left telemetry column. */
	public static final int CHECK_VALVE_SLOT_X = 152;
	public static final int CHECK_VALVE_SLOT_Y = 18;
	public static final int CHECK_VALVE_LABEL_X = CHECK_VALVE_SLOT_X + 9;
	public static final int CHECK_VALVE_LABEL_Y = CHECK_VALVE_SLOT_Y + 20;

	private static final int INVENTORY_START_X = 8;
	private static final int INVENTORY_START_Y = 84;

	private final Container pump;
	private final ContainerData data;

	public WaterPumpMenu(int containerId, Inventory inventory) {
		this(containerId, inventory, new net.minecraft.world.SimpleContainer(WaterPumpBlockEntity.CONTAINER_SIZE), new SimpleContainerData(WaterPumpBlockEntity.DATA_COUNT));
	}

	public WaterPumpMenu(int containerId, Inventory inventory, Container pump, ContainerData data) {
		super(ModMenus.WATER_PUMP, containerId);
		checkContainerSize(pump, WaterPumpBlockEntity.CONTAINER_SIZE);
		checkContainerDataCount(data, WaterPumpBlockEntity.DATA_COUNT);
		this.pump = pump;
		this.data = data;

		this.addSlot(new Slot(pump, CHECK_VALVE_SLOT, CHECK_VALVE_SLOT_X, CHECK_VALVE_SLOT_Y) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return WaterPumpBlockEntity.isCheckValve(stack);
			}

			@Override
			public int getMaxStackSize() {
				return 1;
			}
		});
		this.addStandardInventorySlots(inventory, INVENTORY_START_X, INVENTORY_START_Y);
		this.addDataSlots(data);
	}

	public int getAmountMb() {
		return this.data.get(WaterPumpBlockEntity.DATA_AMOUNT);
	}

	public Fluid getFluid() {
		Fluid fluid = BuiltInRegistries.FLUID.byId(this.data.get(WaterPumpBlockEntity.DATA_FLUID_ID));
		return fluid != null ? fluid : Fluids.EMPTY;
	}

	public float getFillProgress() {
		return Mth.clamp(this.getAmountMb() / (float) WaterPumpBlockEntity.CAPACITY_MB, 0.0F, 1.0F);
	}

	public double getBufferPressureKpa() {
		return SyncedSiFloat.fromData(
			this.data.get(WaterPumpBlockEntity.DATA_BUFFER_KPA_LO),
			this.data.get(WaterPumpBlockEntity.DATA_BUFFER_KPA_HI)
		);
	}

	public double getTorque() {
		return SyncedSiFloat.fromData(
			this.data.get(WaterPumpBlockEntity.DATA_TORQUE_LO),
			this.data.get(WaterPumpBlockEntity.DATA_TORQUE_HI)
		);
	}

	public double getOmega() {
		return SyncedSiFloat.fromData(
			this.data.get(WaterPumpBlockEntity.DATA_OMEGA_LO),
			this.data.get(WaterPumpBlockEntity.DATA_OMEGA_HI)
		);
	}

	/** ω-commanded intake (B/tick); pump suction attempt per machine.mdc. */
	public double getInletBucketsPerTick() {
		return WaterPumpBlockEntity.rateMb(this.getOmega()) / (double) WaterPumpBlockEntity.CAPACITY_MB;
	}

	/**
	 * Actual outlet this tick (B/tick). Pump attempts ω-rate into the adjacent pipe;
	 * accepted amount is limited by dest space — line throughput then follows κ·p·(q/C).
	 */
	public double getOutletBucketsPerTick() {
		return this.data.get(WaterPumpBlockEntity.DATA_OUTLET_MB) / (double) WaterPumpBlockEntity.CAPACITY_MB;
	}

	public double getInletPressureKpa() {
		return SyncedSiFloat.fromData(
			this.data.get(WaterPumpBlockEntity.DATA_INLET_KPA_LO),
			this.data.get(WaterPumpBlockEntity.DATA_INLET_KPA_HI)
		);
	}

	public double getOutletPressureKpa() {
		return SyncedSiFloat.fromData(
			this.data.get(WaterPumpBlockEntity.DATA_OUTLET_KPA_LO),
			this.data.get(WaterPumpBlockEntity.DATA_OUTLET_KPA_HI)
		);
	}

	/** Screen/legacy: outlet pressure as milli-kPa. */
	public int getOutletPressureMilli() {
		return FluidUnits.kpaToMilli(this.getOutletPressureKpa());
	}

	public String formatBufferFu() {
		return FluidUnits.formatBuckets(this.getAmountMb()) + "/" + FluidUnits.formatBuckets(WaterPumpBlockEntity.CAPACITY_MB);
	}

	@Override
	public boolean stillValid(Player player) {
		return this.pump.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack result = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			result = stack.copy();
			if (index == CHECK_VALVE_SLOT) {
				if (!this.moveItemStackTo(stack, 1, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (WaterPumpBlockEntity.isCheckValve(stack)) {
				if (!this.moveItemStackTo(stack, CHECK_VALVE_SLOT, CHECK_VALVE_SLOT + 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index < 1 + 27) {
				if (!this.moveItemStackTo(stack, 1 + 27, this.slots.size(), false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(stack, 1, 1 + 27, false)) {
				return ItemStack.EMPTY;
			}
			if (stack.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}
		return result;
	}
}
