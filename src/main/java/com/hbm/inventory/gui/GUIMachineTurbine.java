package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineTurbine;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineTurbine;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachineTurbine extends GuiInfoContainer<ContainerMachineTurbine> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/gui_turbine.png");
	private final TileEntityMachineTurbine turbine;

	public GUIMachineTurbine(ContainerMachineTurbine menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.turbine = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 166;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.renderTankInfo(graphics, turbine.tanks[0], mouseX, mouseY, leftPos + 62, topPos + 69 - 52, 16, 52);
		this.renderTankInfo(graphics, turbine.tanks[1], mouseX, mouseY, leftPos + 134, topPos + 69 - 52, 16, 52);

		if(turbine.tanks[1].getTankType() == Fluids.NONE) {
			this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos - 16, topPos + 36 + 32, 16, 16, leftPos - 8, topPos + 36 + 16 + 32, "Error: Invalid fluid!");
		}

		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 123, topPos + 69 - 34, 7, 34, turbine.power, TileEntityMachineTurbine.maxPower);
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

		if(turbine.tanks[0].getTankType() == Fluids.STEAM) drawTexturedModalRect(graphics, texture, leftPos + 99, topPos + 18, 183, 0, 14, 14);
		if(turbine.tanks[0].getTankType() == Fluids.HOTSTEAM) drawTexturedModalRect(graphics, texture, leftPos + 99, topPos + 18, 183, 14, 14, 14);
		if(turbine.tanks[0].getTankType() == Fluids.SUPERHOTSTEAM) drawTexturedModalRect(graphics, texture, leftPos + 99, topPos + 18, 183, 28, 14, 14);
		if(turbine.tanks[0].getTankType() == Fluids.ULTRAHOTSTEAM) drawTexturedModalRect(graphics, texture, leftPos + 99, topPos + 18, 183, 42, 14, 14);

		int i = (int) turbine.getPowerScaled(34);
		drawTexturedModalRect(graphics, texture, leftPos + 123, topPos + 69 - i, 176, 34 - i, 7, i);

		if(turbine.tanks[1].getTankType() == Fluids.NONE) {
			this.drawInfoPanel(graphics, leftPos - 16, topPos + 36 + 32, 16, 16, 6);
		}

		this.renderTank(graphics, turbine.tanks[0], leftPos + 62, topPos + 69, 16, 52);
		this.renderTank(graphics, turbine.tanks[1], leftPos + 134, topPos + 69, 16, 52);
	}
}
