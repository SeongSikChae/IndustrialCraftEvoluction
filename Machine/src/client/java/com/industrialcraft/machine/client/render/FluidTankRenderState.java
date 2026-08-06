package com.industrialcraft.machine.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FluidTankRenderState extends BlockEntityRenderState {
	public int fillStep;
	public int fluidArgb;
	public Fluid fluid = Fluids.EMPTY;
}
