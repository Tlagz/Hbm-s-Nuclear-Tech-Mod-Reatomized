package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerPyroOven;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.oil.TileEntityMachinePyroOven;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIPyroOven extends GuiInfoContainer<ContainerPyroOven> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_pyrooven.png");
	private final TileEntityMachinePyroOven pyro;

	public GUIPyroOven(ContainerPyroOven menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.pyro = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 204;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.renderTankInfo(graphics, pyro.tanks[0], mouseX, mouseY, leftPos + 8, topPos + 18, 16, 52);
		this.renderTankInfo(graphics, pyro.tanks[1], mouseX, mouseY, leftPos + 116, topPos + 18, 16, 52);
		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 152, topPos + 18, 16, 52, pyro.getPower(), pyro.getMaxPower());

		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 108, topPos + 76, 8, 8, leftPos + 108, topPos + 76, this.getUpgradeInfo(pyro));
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2 - 18, 6, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		int i = (int) (pyro.power * 52 / TileEntityMachinePyroOven.maxPower);
		drawTexturedModalRect(graphics, texture, leftPos + 152, topPos + 70 - i, 176, 64 - i, 16, i);

		int p = (int) (pyro.progress * 27);
		drawTexturedModalRect(graphics, texture, leftPos + 57, topPos + 47, 176, 0, p, 12);

		this.renderTank(graphics, pyro.tanks[0], leftPos + 8, topPos + 70, 16, 52);
		this.renderTank(graphics, pyro.tanks[1], leftPos + 116, topPos + 70, 16, 52);

		this.drawInfoPanel(graphics, leftPos + 108, topPos + 76, 8, 8, 8);
	}
}
