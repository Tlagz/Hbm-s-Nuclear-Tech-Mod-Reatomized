package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineEPress;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineEPress;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachineEPress extends GuiInfoContainer<ContainerMachineEPress> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_electric_press.png");
	private final TileEntityMachineEPress press;

	public GUIMachineEPress(ContainerMachineEPress menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.press = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 186;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 152, topPos + 52 - 34, 16, 34, press.power, TileEntityMachineEPress.maxPower);
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

		int i = (int) (press.power * 34 / TileEntityMachineEPress.maxPower);
		drawTexturedModalRect(graphics, texture, leftPos + 152, topPos + 52 - i, 176, 34 - i, 16, i);

		int k = (int) (press.renderPress * 16 / TileEntityMachineEPress.maxPress);
		drawTexturedModalRect(graphics, texture, leftPos + 18, topPos + 33, 192, 0, 18, k);
	}
}
