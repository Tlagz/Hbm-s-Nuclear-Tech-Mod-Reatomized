package com.hbm.inventory.gui;

import java.util.Locale;

import com.hbm.inventory.container.ContainerFurnaceCombo;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityFurnaceCombination;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIFurnaceCombo extends GuiInfoContainer<ContainerFurnaceCombo> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_furnace_combination.png");
	private final TileEntityFurnaceCombination furnace;

	public GUIFurnaceCombo(ContainerFurnaceCombo menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.furnace = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 186;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int x, int y) {
		this.renderTankInfo(graphics, furnace.tank, x, y, leftPos + 118, topPos + 18, 16, 52);

		this.drawCustomInfoStat(graphics, x, y, leftPos + 44, topPos + 36, 39, 7, x, y, String.format(Locale.US, "%,d", furnace.progress) + " / " + String.format(Locale.US, "%,d", TileEntityFurnaceCombination.processTime) + "TU");
		this.drawCustomInfoStat(graphics, x, y, leftPos + 44, topPos + 45, 39, 7, x, y, String.format(Locale.US, "%,d", furnace.heat) + " / " + String.format(Locale.US, "%,d", TileEntityFurnaceCombination.maxHeat) + "TU");
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2, 6, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		int p = furnace.progress * 38 / TileEntityFurnaceCombination.processTime;
		drawTexturedModalRect(graphics, texture, leftPos + 45, topPos + 37, 176, 0, p, 5);

		int h = furnace.heat * 37 / TileEntityFurnaceCombination.maxHeat;
		drawTexturedModalRect(graphics, texture, leftPos + 45, topPos + 46, 176, 5, h, 5);

		this.renderTank(graphics, furnace.tank, leftPos + 118, topPos + 70, 16, 52);
	}
}
