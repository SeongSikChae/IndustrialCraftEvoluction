package com.industrialcraft.machine.client.render;

import com.industrialcraft.machine.block.DynamoBlock;
import com.industrialcraft.machine.block.entity.DynamoBlockEntity;
import com.industrialcraft.machine.item.ModItems;
import com.industrialcraft.machine.util.MetricFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Output shaft (same gear mesh as furnace engine) + torque / omega / power on four non-I/O faces.
 */
public class DynamoRenderer implements BlockEntityRenderer<DynamoBlockEntity, DynamoRenderState> {
	/** Body I/O faces sit at ±0.375 from center; keep axle seated in the output collar. */
	private static final float ASSEMBLY_X = 0.42F;
	private static final float ASSEMBLY_SCALE = 1.0F;
	private static final float TEXT_SCALE = 0.012F;
	private static final float FACE_OFFSET = 0.515F;
	private static final int TEXT_COLOR = 0xFFE8F0FF;
	private static final int BACKGROUND_COLOR = 0x00000000;
	private static final int OUTLINE_COLOR = 0xFF101820;

	private final Font font;
	private final ItemModelResolver itemModelResolver;

	public DynamoRenderer(BlockEntityRendererProvider.Context context) {
		this.font = context.font();
		this.itemModelResolver = context.itemModelResolver();
	}

	@Override
	public DynamoRenderState createRenderState() {
		return new DynamoRenderState();
	}

	@Override
	public void extractRenderState(
		DynamoBlockEntity blockEntity,
		DynamoRenderState state,
		float partialTicks,
		Vec3 cameraPos,
		ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay
	) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPos, crumblingOverlay);
		state.facing = blockEntity.getBlockState().getValue(DynamoBlock.FACING);
		state.shaftAngle = blockEntity.getShaftAngle(partialTicks);
		state.torqueLabel = Component.literal(MetricFormat.formatWithUnit(blockEntity.getTorque(), "Nm"));
		state.omegaLabel = Component.literal(MetricFormat.formatWithUnit(blockEntity.getOmega(), "rad/s"));
		state.powerLabel = Component.literal(MetricFormat.formatWithUnit(blockEntity.getPower(), "W"));

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
		DynamoRenderState state,
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		CameraRenderState camera
	) {
		submitShaft(state, poseStack, submitNodeCollector);

		Direction axisPositive = state.facing;
		for (Direction face : Direction.values()) {
			if (face.getAxis() == axisPositive.getAxis()) {
				continue;
			}
			poseStack.pushPose();
			orientToFace(poseStack, face, axisPositive);
			submitFaceLabels(state, poseStack, submitNodeCollector, state.lightCoords);
			poseStack.popPose();
		}
	}

	private static void submitShaft(
		DynamoRenderState state,
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector
	) {
		if (state.shaftAssembly.isEmpty()) {
			return;
		}

		poseStack.pushPose();
		poseStack.translate(0.5F, 0.5F, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.getClockWise().toYRot()));
		poseStack.translate(ASSEMBLY_X, 0.0F, 0.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(state.shaftAngle));
		poseStack.scale(ASSEMBLY_SCALE, ASSEMBLY_SCALE, ASSEMBLY_SCALE);
		state.shaftAssembly.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		poseStack.popPose();
	}

	private void submitFaceLabels(
		DynamoRenderState state,
		PoseStack poseStack,
		SubmitNodeCollector collector,
		int light
	) {
		poseStack.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
		int lineHeight = this.font.lineHeight + 2;
		float startY = -lineHeight * 1.5F;
		submitCenteredLine(collector, poseStack, state.torqueLabel, startY, light);
		submitCenteredLine(collector, poseStack, state.omegaLabel, startY + lineHeight, light);
		submitCenteredLine(collector, poseStack, state.powerLabel, startY + lineHeight * 2, light);
	}

	private void submitCenteredLine(
		SubmitNodeCollector collector,
		PoseStack poseStack,
		Component label,
		float y,
		int light
	) {
		FormattedCharSequence text = label.getVisualOrderText();
		float x = -this.font.width(text) / 2.0F;
		collector.submitText(
			poseStack,
			x,
			y,
			text,
			false,
			Font.DisplayMode.POLYGON_OFFSET,
			light,
			TEXT_COLOR,
			BACKGROUND_COLOR,
			OUTLINE_COLOR
		);
	}

	private static void orientToFace(PoseStack poseStack, Direction face, Direction output) {
		poseStack.translate(0.5F, 0.5F, 0.5F);
		switch (face) {
			case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
			case SOUTH -> {
			}
			case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
			case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
			case UP -> {
				poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
				poseStack.mulPose(Axis.ZP.rotationDegrees(-output.toYRot()));
			}
			case DOWN -> {
				poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
				poseStack.mulPose(Axis.ZP.rotationDegrees(output.toYRot()));
			}
		}
		poseStack.translate(0.0F, 0.0F, FACE_OFFSET);
	}
}
