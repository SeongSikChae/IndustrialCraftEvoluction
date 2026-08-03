package com.industrialcraft.machine.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

public class FurnaceEngineRenderState extends BlockEntityRenderState {
	public boolean lit;
	public Direction facing = Direction.NORTH;
	public float shaftAngle;
	public final ItemStackRenderState shaftItem = new ItemStackRenderState();
}
