package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineTurbofan;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineTurbofan;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachineTurbofan extends GuiInfoContainer<ContainerMachineTurbofan> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/generators/gui_turbofan.png");
	/** The original's GUIElements.Gauge.ROUND_SMALL: 13 frames of 18x18 stacked vertically */
	private static final ResourceLocation gauge = RefStrings.loc("textures/gui/gauges/small_round.png");
	private final TileEntityMachineTurbofan turbofan;

	public GUIMachineTurbofan(ContainerMachineTurbofan menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.turbofan = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 203;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.renderTankInfo(graphics, turbofan.tank, mouseX, mouseY, leftPos + 35, topPos + 17, 34, 52);
		if(turbofan.showBlood) this.renderTankInfo(graphics, turbofan.blood, mouseX, mouseY, leftPos + 98, topPos + 17, 16, 16);
		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 143, topPos + 17, 16, 52, turbofan.power, TileEntityMachineTurbofan.maxPower);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, 43 - font.width(name) / 2, 6, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		int i = (int) turbofan.getPowerScaled(52);
		drawTexturedModalRect(graphics, texture, leftPos + 152 - 9, topPos + 69 - i, 176 + 16, 52 - i, 16, i);

		if(turbofan.afterburner > 0) {
			int a = Math.min(turbofan.afterburner, 6);
			drawTexturedModalRect(graphics, texture, leftPos + 98, topPos + 44, 176, (a - 1) * 16, 16, 16);
		}

		if(turbofan.showBlood) {
			int frames = 13;
			int frame = (int) Math.round((frames - 1) * ((double) turbofan.blood.getFill() / (double) turbofan.blood.getMaxFill()));
			graphics.blit(gauge, leftPos + 97, topPos + 16, 0, frame * 18, 18, 18, 18, 18 * frames);
		}

		this.renderTank(graphics, turbofan.tank, leftPos + 35, topPos + 69, 34, 52);
	}
}
