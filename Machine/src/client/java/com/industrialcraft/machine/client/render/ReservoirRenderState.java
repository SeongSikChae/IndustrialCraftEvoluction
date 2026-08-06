package com.industrialcraft.machine.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class ReservoirRenderState extends BlockEntityRenderState {
	public boolean empty = true;
	public float fill;
	public int fluidArgb = 0x803F76E4;
}
