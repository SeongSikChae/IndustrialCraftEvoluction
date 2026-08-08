package com.industrialcraft.machine.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

public class WaterPumpRenderState extends BlockEntityRenderState {
	public Direction facing = Direction.SOUTH;
	public float shaftAngle;
	public final ItemStackRenderState shaftAssembly = new ItemStackRenderState();
}
