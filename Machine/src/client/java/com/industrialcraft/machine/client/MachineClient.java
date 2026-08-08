package com.industrialcraft.machine.client;

import com.industrialcraft.machine.block.entity.ModBlockEntities;
import com.industrialcraft.machine.client.gui.FurnaceEngineScreen;
import com.industrialcraft.machine.client.gui.MachineCraftingTableScreen;
import com.industrialcraft.machine.client.gui.ReservoirScreen;
import com.industrialcraft.machine.client.gui.WaterPumpScreen;
import com.industrialcraft.machine.client.render.DynamoRenderer;
import com.industrialcraft.machine.client.render.FluidPipeRenderer;
import com.industrialcraft.machine.client.render.FurnaceEngineRenderer;
import com.industrialcraft.machine.client.render.ReservoirRenderer;
import com.industrialcraft.machine.client.render.WaterPumpRenderer;
import com.industrialcraft.machine.menu.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class MachineClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(ModMenus.MACHINE_CRAFTING_TABLE, MachineCraftingTableScreen::new);
		MenuScreens.register(ModMenus.FURNACE_ENGINE, FurnaceEngineScreen::new);
		MenuScreens.register(ModMenus.RESERVOIR, ReservoirScreen::new);
		MenuScreens.register(ModMenus.WATER_PUMP, WaterPumpScreen::new);
		BlockEntityRenderers.register(ModBlockEntities.FURNACE_ENGINE, FurnaceEngineRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.DYNAMO, DynamoRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.FLUID_PIPE, FluidPipeRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.RESERVOIR, ReservoirRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.WATER_PUMP, WaterPumpRenderer::new);
		FluidPipeLookOverlay.register();
	}
}
