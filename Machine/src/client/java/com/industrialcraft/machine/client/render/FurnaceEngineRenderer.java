package com.industrialcraft.machine.client.render;

import com.industrialcraft.machine.block.FurnaceEngineBlock;
import com.industrialcraft.machine.block.entity.FurnaceEngineBlockEntity;
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
 * Output shaft gear: {@link FurnaceEngineBlock#FACING} = shaft world direction.
 */
public class FurnaceEngineRenderer implements BlockEntityRenderer<FurnaceEngineBlockEntity, FurnaceEngineRenderState> {
	/** Model origin at block center + this along FACING; short axle reaches back into the collar. */
	private static final float ASSEMBLY_X = 0.50F;
	private static final float ASSEMBLY_SCALE = 1.0F;

	private final ItemModelResolver itemModelResolver;

	public FurnaceEngineRenderer(BlockEntityRendererProvider.Context context) {
		this.itemModelResolver = context.itemModelResolver();
	}

	@Override
	public FurnaceEngineRenderState createRenderState() {
		return new FurnaceEngineRenderState();
	}

	@Override
	public void extractRenderState(
		FurnaceEngineBlockEntity blockEntity,
		FurnaceEngineRenderState state,
		float partialTicks,
		Vec3 cameraPos,
		ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay
	) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPos, crumblingOverlay);
		state.facing = blockEntity.getBlockState().getValue(FurnaceEngineBlock.FACING);
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
		FurnaceEngineRenderState state,
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
