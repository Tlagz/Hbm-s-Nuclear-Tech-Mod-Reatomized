package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineRefinery;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.RefineryRecipes;
import com.hbm.inventory.recipes.RefineryRecipes.RefineryRecipe;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.oil.TileEntityMachineRefinery;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachineRefinery extends GuiInfoContainer<ContainerMachineRefinery> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_refinery.png");
	private final TileEntityMachineRefinery refinery;

	public GUIMachineRefinery(ContainerMachineRefinery menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		refinery = menu.tile;

		this.imageWidth = 182;
		this.imageHeight = 240;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.renderTankInfo(graphics, refinery.tanks[0], mouseX, mouseY, leftPos + 12, topPos + 17, 16, 70);
		this.renderTankInfo(graphics, refinery.tanks[1], mouseX, mouseY, leftPos + 64, topPos + 35, 16, 52);
		this.renderTankInfo(graphics, refinery.tanks[2], mouseX, mouseY, leftPos + 82, topPos + 35, 16, 52);
		this.renderTankInfo(graphics, refinery.tanks[3], mouseX, mouseY, leftPos + 100, topPos + 35, 16, 52);
		this.renderTankInfo(graphics, refinery.tanks[4], mouseX, mouseY, leftPos + 118, topPos + 35, 16, 52);
		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 158, topPos + 18, 16, 88, refinery.power, TileEntityMachineRefinery.maxPower);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, this.imageWidth / 2 - 36 / 2 - font.width(name) / 2, 6, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 11, inventoryLabelY, 4210752, false);
	}

	private void tinted(GuiGraphics graphics, int color, int x, int y, int u, int v, int w, int h) {
		graphics.setColor(((color >> 16) & 0xFF) / 255F, ((color >> 8) & 0xFF) / 255F, (color & 0xFF) / 255F, 1F);
		drawTexturedModalRect(graphics, texture, x, y, u, v, w, h);
		graphics.setColor(1F, 1F, 1F, 1F);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		// power
		int j = (int) refinery.getPowerScaled(88);
		drawTexturedModalRect(graphics, texture, leftPos + 158, topPos + 106 - j, 182, 88 - j, 16, j);

		// input tank
		FluidTank inputOil = refinery.tanks[0];
		if(inputOil.getFill() != 0) {
			this.renderTank(graphics, inputOil, leftPos + 12, topPos + 88, 16, 70);
		}

		// the pipes, tinted in the color of the product going through them
		RefineryRecipe recipe = RefineryRecipes.getRefinery(inputOil.getTankType());

		if(recipe == null) {
			drawTexturedModalRect(graphics, texture, leftPos + 30, topPos + 30, 0, 248, 43, 4);
			drawTexturedModalRect(graphics, texture, leftPos + 30, topPos + 26, 0, 240, 61, 8);
			drawTexturedModalRect(graphics, texture, leftPos + 30, topPos + 22, 61, 240, 79, 12);
			drawTexturedModalRect(graphics, texture, leftPos + 30, topPos + 18, 140, 240, 97, 16);
		} else {
			tinted(graphics, recipe.outputs[0].type.getColor(), leftPos + 30, topPos + 30, 0, 248, 43, 4);
			tinted(graphics, recipe.outputs[1].type.getColor(), leftPos + 30, topPos + 26, 0, 240, 61, 8);
			tinted(graphics, recipe.outputs[2].type.getColor(), leftPos + 30, topPos + 22, 61, 240, 79, 12);
			tinted(graphics, recipe.outputs[3].type.getColor(), leftPos + 30, topPos + 18, 140, 240, 97, 16);
		}

		// output tanks
		this.renderTank(graphics, refinery.tanks[1], leftPos + 64, topPos + 88, 16, 52);
		this.renderTank(graphics, refinery.tanks[2], leftPos + 82, topPos + 88, 16, 52);
		this.renderTank(graphics, refinery.tanks[3], leftPos + 100, topPos + 88, 16, 52);
		this.renderTank(graphics, refinery.tanks[4], leftPos + 118, topPos + 88, 16, 52);
	}
}
