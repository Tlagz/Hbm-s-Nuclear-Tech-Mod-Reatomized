package com.hbm.inventory.gui;

import java.util.Locale;

import com.hbm.inventory.container.ContainerMachineCoker;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.oil.TileEntityMachineCoker;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachineCoker extends GuiInfoContainer<ContainerMachineCoker> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_coker.png");
	private final TileEntityMachineCoker coker;

	public GUIMachineCoker(ContainerMachineCoker menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.coker = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 204;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.renderTankInfo(graphics, coker.tanks[0], mouseX, mouseY, leftPos + 35, topPos + 18, 16, 52);
		this.renderTankInfo(graphics, coker.tanks[1], mouseX, mouseY, leftPos + 125, topPos + 18, 16, 52);

		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 60, topPos + 45, 54, 7, mouseX, mouseY, String.format(Locale.US, "%,d", coker.progress) + " / " + String.format(Locale.US, "%,d", TileEntityMachineCoker.processTime) + "TU");
		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 60, topPos + 54, 54, 7, mouseX, mouseY, String.format(Locale.US, "%,d", coker.heat) + " / " + String.format(Locale.US, "%,d", TileEntityMachineCoker.maxHeat) + "TU");
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2, 6, 0xC7C1A3, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		int p = coker.progress * 53 / TileEntityMachineCoker.processTime;
		drawTexturedModalRect(graphics, texture, leftPos + 61, topPos + 46, 176, 0, p, 5);

		int h = coker.heat * 52 / TileEntityMachineCoker.maxHeat;
		drawTexturedModalRect(graphics, texture, leftPos + 61, topPos + 55, 176, 5, h, 5);

		this.renderTank(graphics, coker.tanks[0], leftPos + 35, topPos + 70, 16, 52);
		this.renderTank(graphics, coker.tanks[1], leftPos + 125, topPos + 70, 16, 52);
	}
}
