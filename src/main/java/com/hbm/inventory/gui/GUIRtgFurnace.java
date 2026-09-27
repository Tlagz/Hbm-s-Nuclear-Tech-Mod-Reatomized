package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerRtgFurnace;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityRtgFurnace;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIRtgFurnace extends GuiInfoContainer<ContainerRtgFurnace> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/rtgfurnace.png");
	private final TileEntityRtgFurnace furnace;

	public GUIRtgFurnace(ContainerRtgFurnace menu, Inventory invPlayer, Component title) {
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
		graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2, 6, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		if(furnace.hasPower()) {
			drawTexturedModalRect(graphics, texture, leftPos + 55, topPos + 35, 176, 0, 18, 16);
		}

		int j1 = furnace.getDiFurnaceProgressScaled(24);
		drawTexturedModalRect(graphics, texture, leftPos + 79, topPos + 34, 176, 16, j1 + 1, 17);
	}
}
