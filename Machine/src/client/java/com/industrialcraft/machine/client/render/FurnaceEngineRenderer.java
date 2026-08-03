package com.industrialcraft.machine.client.render;

import com.industrialcraft.machine.block.FurnaceEngineBlock;
import com.industrialcraft.machine.block.entity.FurnaceEngineBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Renders a decorative shaft. Visual spin is intentionally slower than the
 * mechanical omega (256) so the animation stays readable in-game.
 */
public class FurnaceEngineRenderer implements BlockEntityRenderer<FurnaceEngineBlockEntity, FurnaceEngineRenderState> {
	/** Degrees per tick while generating (~1.5s per revolution). */
	public static final float VISUAL_DEG_PER_TICK = 4.0F;

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

		if (state.lit && blockEntity.getLevel() != null) {
			state.shaftAngle = (blockEntity.getLevel().getGameTime() + partialTicks) * VISUAL_DEG_PER_TICK;
		} else {
			state.shaftAngle = 0.0F;
		}

		this.itemModelResolver.updateForTopItem(
			state.shaftItem,
			new ItemStack(Items.IRON_BARS),
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
		if (state.shaftItem.isEmpty()) {
			return;
		}

		Direction facing = state.facing;
		poseStack.pushPose();
		poseStack.translate(0.5F, 0.5F, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
		poseStack.translate(0.0F, 0.0F, 0.55F);
		poseStack.mulPose(Axis.ZP.rotationDegrees(state.shaftAngle));
		poseStack.scale(0.55F, 0.55F, 0.85F);
		state.shaftItem.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		poseStack.popPose();
	}
}
