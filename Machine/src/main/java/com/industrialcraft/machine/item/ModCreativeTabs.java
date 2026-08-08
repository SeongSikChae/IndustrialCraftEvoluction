package com.industrialcraft.machine.item;

import com.industrialcraft.machine.MachineMod;
import com.industrialcraft.machine.block.ModBlocks;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTabs {
	public static final ResourceKey<CreativeModeTab> FUNCTIONAL_BLOCKS_KEY = key("functional_blocks");

	private ModCreativeTabs() {
	}

	public static void initialize() {
		Registry.register(
			BuiltInRegistries.CREATIVE_MODE_TAB,
			FUNCTIONAL_BLOCKS_KEY,
			FabricCreativeModeTab.builder()
				.title(Component.translatable("itemGroup.machine.functional_blocks"))
				.icon(() -> new ItemStack(ModBlocks.FURNACE_ENGINE))
				.displayItems((params, output) -> {
					output.accept(ModBlocks.MACHINE_CRAFTING_TABLE);
					output.accept(ModBlocks.FURNACE_ENGINE);
					output.accept(ModBlocks.DYNAMO);
					output.accept(ModBlocks.RESERVOIR);
					output.accept(ModBlocks.RAIN_COLLECTOR);
					output.accept(ModBlocks.FLUID_PIPE);
					output.accept(ModBlocks.WATER_PUMP);
					output.accept(ModItems.GOVERNOR_ACCESSORY);
					output.accept(ModItems.CHECK_VALVE_ACCESSORY);
				})
				.build()
		);
	}

	private static ResourceKey<CreativeModeTab> key(String path) {
		return ResourceKey.create(Registries.CREATIVE_MODE_TAB, MachineMod.id(path));
	}
}
