package com.hbm.inventory.gui;

import java.util.List;

import com.hbm.inventory.container.ContainerFurnaceIron;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityFurnaceIron;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class GUIFurnaceIron extends GuiInfoContainer<ContainerFurnaceIron> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_furnace_iron.png");
	private final TileEntityFurnaceIron furnace;

	public GUIFurnaceIron(ContainerFurnaceIron menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.furnace = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 166;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int x, int y) {

		// empty fuel slots list the fuel bonuses
		if(this.menu.getCarried().isEmpty()) {
			for(int i = 1; i < 3; ++i) {
				Slot slot = this.menu.getSlot(i);
				if(this.isHovering(slot, x, y) && !slot.hasItem()) {
					List<String> bonuses = this.furnace.burnModule.getTimeDesc();
					if(!bonuses.isEmpty()) drawInfo(graphics, bonuses.toArray(new String[0]), x, y);
				}
			}
		}

		this.drawCustomInfoStat(graphics, x, y, leftPos + 52, topPos + 35, 71, 7, x, y, (furnace.progress * 100 / Math.max(furnace.processingTime, 1)) + "%");
		this.drawCustomInfoStat(graphics, x, y, leftPos + 52, topPos + 44, 71, 7, x, y, (furnace.burnTime / 20) + "s");
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

		int i = furnace.progress * 70 / Math.max(furnace.processingTime, 1);
		drawTexturedModalRect(graphics, texture, leftPos + 53, topPos + 36, 176, 18, i, 5);

		int j = furnace.burnTime * 70 / Math.max(furnace.maxBurnTime, 1);
		drawTexturedModalRect(graphics, texture, leftPos + 53, topPos + 45, 176, 23, j, 5);

		if(furnace.wasOn)
			drawTexturedModalRect(graphics, texture, leftPos + 70, topPos + 16, 176, 0, 18, 18);
	}
}
