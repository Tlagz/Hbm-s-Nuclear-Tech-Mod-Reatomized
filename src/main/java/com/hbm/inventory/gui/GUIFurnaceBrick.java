package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerFurnaceBrick;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityFurnaceBrick;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIFurnaceBrick extends GuiInfoContainer<ContainerFurnaceBrick> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_furnace_brick.png");
	private final TileEntityFurnaceBrick furnace;

	public GUIFurnaceBrick(ContainerFurnaceBrick menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.furnace = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 166;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) { }

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2, 6, 0xffffff, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0xffffff, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		if(this.furnace.burnTime > 0) {
			int b = furnace.burnTime * 13 / Math.max(furnace.maxBurnTime, 1);
			drawTexturedModalRect(graphics, texture, leftPos + 62, topPos + 54 + 12 - b, 176, 12 - b, 14, b + 1);
			int p = this.furnace.progress * 24 / 200;
			drawTexturedModalRect(graphics, texture, leftPos + 85, topPos + 34, 176, 14, p + 1, 16);
		}
	}
}
