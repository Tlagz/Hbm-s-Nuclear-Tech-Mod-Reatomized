package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineArcWelder;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineArcWelder;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachineArcWelder extends GuiInfoContainer<ContainerMachineArcWelder> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_arc_welder.png");
	private final TileEntityMachineArcWelder welder;

	public GUIMachineArcWelder(ContainerMachineArcWelder menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);

		this.welder = menu.tile;
		this.imageWidth = 176;
		this.imageHeight = 204;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int x, int y) {
		this.renderTankInfo(graphics, welder.tank, x, y, leftPos + 35, topPos + 63, 34, 16);
		this.drawElectricityInfo(graphics, x, y, leftPos + 152, topPos + 18, 16, 52, welder.getPower(), welder.getMaxPower());

		this.drawCustomInfoStat(graphics, x, y, leftPos + 78, topPos + 67, 8, 8, leftPos + 78, topPos + 67, this.getUpgradeInfo(welder));
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, this.imageWidth / 2 - font.width(name) / 2 - 18, 6, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float interp, int x, int y) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		int p = (int) (welder.power * 52 / Math.max(welder.maxPower, 1));
		drawTexturedModalRect(graphics, texture, leftPos + 152, topPos + 70 - p, 176, 52 - p, 16, p);

		int i = welder.progress * 33 / Math.max(welder.processTime, 1);
		drawTexturedModalRect(graphics, texture, leftPos + 72, topPos + 37, 192, 0, i, 14);

		if(welder.power >= welder.consumption) {
			drawTexturedModalRect(graphics, texture, leftPos + 156, topPos + 4, 176, 52, 9, 12);
		}

		this.drawInfoPanel(graphics, leftPos + 78, topPos + 67, 8, 8, 8);
		this.renderTankHorizontal(graphics, welder.tank, leftPos + 35, topPos + 79, 34, 16);
	}
}
