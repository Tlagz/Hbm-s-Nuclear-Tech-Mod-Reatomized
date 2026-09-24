package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachinePress;
import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachinePress;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachinePress extends GuiInfoContainer<ContainerMachinePress> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_press.png");
	private final TileEntityMachinePress press;

	public GUIMachinePress(ContainerMachinePress menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		press = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 214;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 25, topPos + 16, 18, 18, mouseX, mouseY, (press.speed * 100 / TileEntityMachinePress.maxSpeed) + "%");
		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 25, topPos + 34, 18, 18, mouseX, mouseY, (press.burnTime / 200) + " operations left");
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, this.imageWidth / 2 - font.width(name) / 2, 5, 0xffffff, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		if(press.burnTime >= 20) {
			drawTexturedModalRect(graphics, texture, leftPos + 26, topPos + 36, 0, 214, 14, 14);
		}

		int k = (int) (press.renderPress * 16 / TileEntityMachinePress.maxPress);
		drawTexturedModalRect(graphics, texture, leftPos + 79, topPos + 35, 15, 214, 18, k);

		double i = (double) press.speed / (double) TileEntityMachinePress.maxSpeed;
		GUIElements.drawSmoothGauge(graphics, leftPos + 34, topPos + 25, 0, i, 5, 2, 1, 0x7f0000);
	}
}
