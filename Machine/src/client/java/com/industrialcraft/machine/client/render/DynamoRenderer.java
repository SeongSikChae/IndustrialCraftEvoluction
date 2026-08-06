package com.industrialcraft.machine.client.render;

import com.industrialcraft.machine.block.DynamoBlock;
import com.industrialcraft.machine.block.entity.DynamoBlockEntity;
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
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Output shaft + torque / omega / power on four non-I/O faces.
 */
public class DynamoRenderer implements BlockEntityRenderer<DynamoBlockEntity, DynamoRenderState> {
	/** Place spinning hub along the extended output axle (nests toward neighbor input recess). */
	private static final float ASSEMBLY_X = 0.62F;
	private static final float ASSEMBLY_SCALE = 1.0F;
	/** Fits torque/omega/power inside the display bezel (model screen 3.5..12.5). */
	private static final float TEXT_SCALE = 0.0105F;
	/** Screen outer face is ±14.5/16 from center; sit just in front to avoid z-fight. */
	private static final float FACE_OFFSET = 0.408F;
	/** Cool LCD glyphs; line plate matches the dark inset screen behind them. */
	private static final int TEXT_COLOR = 0xFFB8F4FF;
	private static final int BACKGROUND_COLOR = 0xD0050C14;
	private static final int OUTLINE_COLOR = 0x00000000;
	/** Self-lit display readout (block light + skylight max). */
	private static final int DISPLAY_LIGHT = 0x00F000F0;
	private static final int LABEL_PAD = 3;

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
		ShaftAssemblyRenderer.resolveGear(
			this.itemModelResolver,
			state.shaftAssembly,
			blockEntity.getLevel(),
			blockEntity.getBlockPos()
		);
	}

	@Override
	public void submit(
		DynamoRenderState state,
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

		Direction axisPositive = state.facing;
		for (Direction face : Direction.values()) {
			if (face.getAxis() == axisPositive.getAxis()) {
				continue;
			}
			poseStack.pushPose();
			orientToFace(poseStack, face, axisPositive);
			submitFaceLabels(state, poseStack, submitNodeCollector);
			poseStack.popPose();
		}
	}

	private void submitFaceLabels(
		DynamoRenderState state,
		PoseStack poseStack,
		SubmitNodeCollector collector
	) {
		poseStack.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
		int lineHeight = this.font.lineHeight + 2;
		float startY = -lineHeight * 1.5F;
		int plateWidth = maxLabelWidth(state) + LABEL_PAD * 2;
		submitDisplayLine(collector, poseStack, state.torqueLabel, startY, plateWidth);
		submitDisplayLine(collector, poseStack, state.omegaLabel, startY + lineHeight, plateWidth);
		submitDisplayLine(collector, poseStack, state.powerLabel, startY + lineHeight * 2, plateWidth);
	}

	private int maxLabelWidth(DynamoRenderState state) {
		return Math.max(
			this.font.width(state.torqueLabel),
			Math.max(this.font.width(state.omegaLabel), this.font.width(state.powerLabel))
		);
	}

	private void submitDisplayLine(
		SubmitNodeCollector collector,
		PoseStack poseStack,
		Component label,
		float y,
		int plateWidth
	) {
		FormattedCharSequence plate = paddedPlate(plateWidth);
		float plateX = -plateWidth / 2.0F;
		collector.submitText(
			poseStack,
			plateX,
			y,
			plate,
			false,
			Font.DisplayMode.NORMAL,
			DISPLAY_LIGHT,
			0x00000000,
			BACKGROUND_COLOR,
			OUTLINE_COLOR
		);

		FormattedCharSequence text = label.getVisualOrderText();
		float textX = -this.font.width(text) / 2.0F;
		collector.submitText(
			poseStack,
			textX,
			y,
			text,
			false,
			Font.DisplayMode.NORMAL,
			DISPLAY_LIGHT,
			TEXT_COLOR,
			0x00000000,
			OUTLINE_COLOR
		);
	}

	private FormattedCharSequence paddedPlate(int plateWidth) {
		int spaceWidth = Math.max(1, this.font.width(" "));
		int spaces = Math.max(1, (int) Math.ceil(plateWidth / (double) spaceWidth));
		return Component.literal(" ".repeat(spaces)).getVisualOrderText();
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
