package com.industrialcraft.machine.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

public class DynamoRenderState extends BlockEntityRenderState {
	public Direction facing = Direction.NORTH;
	public float shaftAngle;
	public Component torqueLabel = Component.empty();
	public Component omegaLabel = Component.empty();
	public Component powerLabel = Component.empty();
	public final ItemStackRenderState shaftAssembly = new ItemStackRenderState();
}
