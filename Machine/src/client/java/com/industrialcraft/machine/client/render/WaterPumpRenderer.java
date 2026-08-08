package com.industrialcraft.machine.client.render;

import com.industrialcraft.machine.block.WaterPumpBlock;
import com.industrialcraft.machine.block.entity.WaterPumpBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Power intake shaft on {@link WaterPumpBlock#FACING}.
 */
public class WaterPumpRenderer implements BlockEntityRenderer<WaterPumpBlockEntity, WaterPumpRenderState> {
	private static final float ASSEMBLY_X = 0.58F;
	private static final float ASSEMBLY_SCALE = 0.95F;

	private final ItemModelResolver itemModelResolver;

	public WaterPumpRenderer(BlockEntityRendererProvider.Context context) {
		this.itemModelResolver = context.itemModelResolver();
	}

	@Override
	public WaterPumpRenderState createRenderState() {
		return new WaterPumpRenderState();
	}

	@Override
	public void extractRenderState(
		WaterPumpBlockEntity blockEntity,
		WaterPumpRenderState state,
		float partialTicks,
		Vec3 cameraPos,
		ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay
	) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPos, crumblingOverlay);
		state.facing = WaterPumpBlock.getInputFace(blockEntity.getBlockState());
		state.shaftAngle = blockEntity.getShaftAngle(partialTicks);
		ShaftAssemblyRenderer.resolveGear(
			this.itemModelResolver,
			state.shaftAssembly,
			blockEntity.getLevel(),
			blockEntity.getBlockPos()
		);
	}

	@Override
	public void submit(
		WaterPumpRenderState state,
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		CameraRenderState camera
	) {
		ShaftAssemblyRenderer.submit(
			poseStack,
			submitNodeCollector,
			state.shaftAssembly,
			state.facing,
			state.shaftAngle,
			ASSEMBLY_X,
			ASSEMBLY_SCALE,
			state.lightCoords
		);
	}
}
