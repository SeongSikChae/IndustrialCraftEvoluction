package com.industrialcraft.machine.client.render;

import com.industrialcraft.machine.block.FurnaceEngineBlock;
import com.industrialcraft.machine.block.entity.FurnaceEngineBlockEntity;
import com.industrialcraft.machine.item.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Renders one rigid shaft+sprocket assembly that spins as a single mesh,
 * seated into the engine's side bearing.
 */
public class FurnaceEngineRenderer implements BlockEntityRenderer<FurnaceEngineBlockEntity, FurnaceEngineRenderState> {
	/**
	 * Assembly origin after facing rotation (+Z front, +X right).
	 * Chosen so the model's recessed shaft end sits inside the bearing housing.
	 */
	private static final float ASSEMBLY_X = 0.52F;
	private static final float ASSEMBLY_SCALE = 0.95F;

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
		state.lit = blockEntity.getBlockState().getValue(FurnaceEngineBlock.LIT);
		state.facing = blockEntity.getBlockState().getValue(FurnaceEngineBlock.FACING);
		state.shaftAngle = blockEntity.getShaftAngle(partialTicks);

		this.itemModelResolver.updateForTopItem(
			state.shaftAssembly,
			new ItemStack(ModItems.FURNACE_ENGINE_GEAR),
			ItemDisplayContext.FIXED,
			blockEntity.getLevel(),
			null,
			(int) blockEntity.getBlockPos().asLong()
		);
	}

	@Override
	public void submit(
		FurnaceEngineRenderState state,
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		CameraRenderState camera
	) {
		if (state.shaftAssembly.isEmpty()) {
			return;
		}

		poseStack.pushPose();
		poseStack.translate(0.5F, 0.5F, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));
		poseStack.translate(ASSEMBLY_X, 0.0F, 0.0F);
		// Model shaft is along +Z; map that to +X (out of the right side).
		poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
		// Spin the whole rigid mesh around the shaft axis.
		poseStack.mulPose(Axis.ZP.rotationDegrees(state.shaftAngle));
		poseStack.scale(ASSEMBLY_SCALE, ASSEMBLY_SCALE, ASSEMBLY_SCALE);
		state.shaftAssembly.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		poseStack.popPose();
	}
}
