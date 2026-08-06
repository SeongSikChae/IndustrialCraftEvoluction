package com.industrialcraft.machine.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class FluidPipeRenderState extends BlockEntityRenderState {
	public boolean empty = true;
	public float fill;
	public int fluidArgb = 0x803F76E4;
	public boolean north;
	public boolean south;
	public boolean east;
	public boolean west;
	public boolean up;
	public boolean down;
}
