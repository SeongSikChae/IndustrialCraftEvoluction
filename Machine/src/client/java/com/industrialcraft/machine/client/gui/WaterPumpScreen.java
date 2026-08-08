package com.industrialcraft.machine.client.gui;

import com.industrialcraft.machine.MachineMod;
import com.industrialcraft.machine.fluid.FluidUnits;
import com.industrialcraft.machine.fluid.FluidVisuals;
import com.industrialcraft.machine.menu.WaterPumpMenu;
import com.industrialcraft.machine.util.MetricFormat;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluids;

/**
 * Pump telemetry: left gauge + short stats; check-valve slot top-right.
 */
public class WaterPumpScreen extends AbstractContainerScreen<WaterPumpMenu> {
	private static final Identifier TEXTURE = MachineMod.id("textures/gui/container/water_pump.png");
	private static final int LABEL_COLOR = 0xFF404040;
	private static final int GAUGE_X = 26;
	private static final int GAUGE_Y = 18;
	private static final int GAUGE_WIDTH = 12;
	private static final int GAUGE_HEIGHT = 46;
	private static final int GAUGE_BG = 0xFF202020;
	private static final int STATS_X = 44;
	private static final int STATS_Y = 16;

	public WaterPumpScreen(WaterPumpMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 176, 166);
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
		this.inventoryLabelY = 74;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		int xo = this.leftPos;
		int yo = this.topPos;
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xo, yo, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

		int gx = xo + GAUGE_X;
		int gy = yo + GAUGE_Y;
		graphics.fill(gx, gy, gx + GAUGE_WIDTH, gy + GAUGE_HEIGHT, GAUGE_BG);

		float progress = this.menu.getFillProgress();
		if (progress > 0.0F) {
			int fillHeight = Mth.ceil(GAUGE_HEIGHT * progress);
			int color = FluidVisuals.argb(this.menu.getFluid());
			if (this.menu.getFluid() == Fluids.EMPTY) {
				color = 0xFF4A90D9;
			} else {
				color |= 0xFF000000;
			}
			graphics.fill(gx + 1, gy + GAUGE_HEIGHT - fillHeight, gx + GAUGE_WIDTH - 1, gy + GAUGE_HEIGHT, color);
		}
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		super.extractLabels(graphics, mouseX, mouseY);
		int line = this.font.lineHeight;
		int y = STATS_Y;

		graphics.text(
			this.font,
			Component.translatable("gui.machine.water_pump.buffer", this.menu.formatBufferFu()),
			STATS_X,
			y,
			LABEL_COLOR,
			false
		);
		y += line;
		graphics.text(
			this.font,
			Component.translatable("gui.machine.water_pump.inlet_rate", formatRate(this.menu.getInletBucketsPerTick())),
			STATS_X,
			y,
			LABEL_COLOR,
			false
		);
		y += line;
		graphics.text(
			this.font,
			Component.translatable(
				"gui.machine.water_pump.torque",
				MetricFormat.formatWithUnit(Math.max(0.0, this.menu.getTorque()), "Nm")
			),
			STATS_X,
			y,
			LABEL_COLOR,
			false
		);
		y += line;
		graphics.text(
			this.font,
			Component.translatable(
				"gui.machine.water_pump.omega",
				MetricFormat.formatWithUnit(Math.max(0.0, this.menu.getOmega()), "rad/s")
			),
			STATS_X,
			y,
			LABEL_COLOR,
			false
		);
		y += line;
		graphics.text(
			this.font,
			Component.translatable("gui.machine.water_pump.outlet_rate", formatRate(this.menu.getOutletBucketsPerTick())),
			STATS_X,
			y,
			LABEL_COLOR,
			false
		);
		y += line;
		graphics.text(
			this.font,
			Component.translatable("gui.machine.water_pump.outlet_p", formatKpa(this.menu.getOutletPressureKpa())),
			STATS_X,
			y,
			LABEL_COLOR,
			false
		);

		Component slotLabel = Component.translatable("gui.machine.water_pump.slot.check_valve");
		int slotLabelX = WaterPumpMenu.CHECK_VALVE_LABEL_X - this.font.width(slotLabel) / 2;
		graphics.text(this.font, slotLabel, slotLabelX, WaterPumpMenu.CHECK_VALVE_LABEL_Y, LABEL_COLOR, false);
	}

	private static String formatRate(double bucketsPerSec) {
		return String.format("%.2f", bucketsPerSec);
	}

	private static String formatKpa(double kpa) {
		return FluidUnits.formatKpaSi(kpa);
	}
}
