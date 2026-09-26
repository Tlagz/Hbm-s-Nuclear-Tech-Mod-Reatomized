package com.hbm.inventory.gui;

import java.util.Locale;

import com.hbm.inventory.container.ContainerFurnaceSteel;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityFurnaceSteel;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIFurnaceSteel extends GuiInfoContainer<ContainerFurnaceSteel> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_furnace_steel.png");
	private final TileEntityFurnaceSteel furnace;

	public GUIFurnaceSteel(ContainerFurnaceSteel menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.furnace = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 166;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int x, int y) {
		for(int i = 0; i < 3; i++) {
			this.drawCustomInfoStat(graphics, x, y, leftPos + 53, topPos + 17 + 18 * i, 70, 7, x, y, String.format(Locale.US, "%,d", furnace.progress[i]) + " / " + String.format(Locale.US, "%,d", TileEntityFurnaceSteel.processTime) + "TU");
			this.drawCustomInfoStat(graphics, x, y, leftPos + 53, topPos + 26 + 18 * i, 70, 7, x, y, "Bonus: " + furnace.bonus[i] + "%");
		}
		this.drawCustomInfoStat(graphics, x, y, leftPos + 151, topPos + 18, 9, 50, x, y, String.format(Locale.US, "%,d", furnace.heat) + " / " + String.format(Locale.US, "%,d", TileEntityFurnaceSteel.maxHeat) + "TU");
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, this.imageWidth / 2 - font.width(name) / 2, 6, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float interp, int x, int y) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		int h = furnace.heat * 48 / TileEntityFurnaceSteel.maxHeat;
		drawTexturedModalRect(graphics, texture, leftPos + 152, topPos + 67 - h, 176, 76 - h, 7, h);

		for(int i = 0; i < 3; i++) {
			int p = Math.min(furnace.progress[i] * 69 / TileEntityFurnaceSteel.processTime, 69);
			drawTexturedModalRect(graphics, texture, leftPos + 54, topPos + 18 + 18 * i, 176, 18, p, 5);

			int b = Math.min(furnace.bonus[i] * 69 / 100, 69);
			drawTexturedModalRect(graphics, texture, leftPos + 54, topPos + 27 + 18 * i, 176, 23, b, 5);

			if(furnace.wasOn)
				drawTexturedModalRect(graphics, texture, leftPos + 16, topPos + 16 + 18 * i, 176, 0, 18, 18);
		}
	}
}
