package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineChemicalPlant;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.lib.RefStrings;
import com.hbm.module.machine.ModuleMachineBase;
import com.hbm.tileentity.machine.TileEntityMachineChemicalPlant;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachineChemicalPlant extends GuiInfoContainerProcessor<ContainerMachineChemicalPlant> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_chemplant.png");
	private final TileEntityMachineChemicalPlant chemplant;

	public GUIMachineChemicalPlant(ContainerMachineChemicalPlant menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.chemplant = menu.tile;

		this.processorModule = new ModuleMachineBase[1];
		this.processorModule[0] = chemplant.chemplantModule;

		this.imageWidth = 176;
		this.imageHeight = 256;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override public int[][] getSelectorPositions() { return new int[][] {{7, 125, 1}}; }
	@Override public BlockPos getControlReceiver() { return this.chemplant.getBlockPos(); }
	@Override public ResourceLocation getTexture() { return texture; }

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		super.drawTooltips(graphics, mouseX, mouseY);

		for(int i = 0; i < 3; i++) {
			this.renderTankInfo(graphics, chemplant.inputTanks[i], mouseX, mouseY, leftPos + 8 + i * 18, topPos + 18, 16, 34);
			this.renderTankInfo(graphics, chemplant.outputTanks[i], mouseX, mouseY, leftPos + 80 + i * 18, topPos + 18, 16, 34);
		}

		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 152, topPos + 18, 16, 61, chemplant.power, chemplant.maxPower);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, 70 - font.width(name) / 2, 6, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		int p = (int) (chemplant.power * 61 / Math.max(chemplant.maxPower, 1));
		drawTexturedModalRect(graphics, texture, leftPos + 152, topPos + 79 - p, 176, 61 - p, 16, p);

		if(chemplant.chemplantModule.progress > 0) {
			int j = (int) Math.ceil(70 * chemplant.chemplantModule.progress);
			drawTexturedModalRect(graphics, texture, leftPos + 62, topPos + 126, 176, 61 + (chemplant.chemplantModule.restrictedMode ? 16 : 0), j, 16);
		}

		GenericRecipe recipe = chemplant.chemplantModule.getRecipe();
		this.renderStandardLEDs(graphics, chemplant.didProcess, recipe, chemplant.power, 51, 121, 195, 0);
		this.renderRecipeIcons(graphics);

		for(int i = 0; i < 3; i++) {
			this.renderTank(graphics, chemplant.inputTanks[i], leftPos + 8 + i * 18, topPos + 52, 16, 34);
			this.renderTank(graphics, chemplant.outputTanks[i], leftPos + 80 + i * 18, topPos + 52, 16, 34);
		}
	}
}
