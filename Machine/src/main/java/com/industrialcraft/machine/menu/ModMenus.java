package com.industrialcraft.machine.menu;

import com.industrialcraft.machine.MachineMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {
	public static final MenuType<MachineCraftingTableMenu> MACHINE_CRAFTING_TABLE = register("machine_crafting_table", MachineCraftingTableMenu::new);
	public static final MenuType<FurnaceEngineMenu> FURNACE_ENGINE = register("furnace_engine", FurnaceEngineMenu::new);
	public static final MenuType<ReservoirMenu> RESERVOIR = register("reservoir", ReservoirMenu::new);

	private ModMenus() {
	}

	private static <T extends AbstractContainerMenu> MenuType<T> register(String name, MenuType.MenuSupplier<T> constructor) {
		return Registry.register(
			BuiltInRegistries.MENU,
			MachineMod.id(name),
			new MenuType<>(constructor, FeatureFlags.VANILLA_SET)
		);
	}

	public static void initialize() {
		// Static field registration.
	}
}
