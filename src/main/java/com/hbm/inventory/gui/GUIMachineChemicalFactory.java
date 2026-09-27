package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineChemicalFactory;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.lib.RefStrings;
import com.hbm.module.machine.ModuleMachineBase;
import com.hbm.tileentity.machine.TileEntityMachineChemicalFactory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachineChemicalFactory extends GuiInfoContainerProcessor<ContainerMachineChemicalFactory> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_chemical_factory.png");
	private final TileEntityMachineChemicalFactory chemplant;

	public GUIMachineChemicalFactory(ContainerMachineChemicalFactory menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.chemplant = menu.tile;

		this.processorModule = new ModuleMachineBase[4];
		for(int i = 0; i < 4; i++) this.processorModule[i] = chemplant.chemplantModule[i];

		this.imageWidth = 248;
		this.imageHeight = 216;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	public int[][] getSelectorPositions() {
		int[][] positions = new int[4][];
		for(int i = 0; i < 4; i++) positions[i] = new int[] {74, 19 + i * 22, 4 + i * 7};
		return positions;
	}

	@Override public BlockPos getControlReceiver() { return this.chemplant.getBlockPos(); }
	@Override public ResourceLocation getTexture() { return texture; }

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		super.drawTooltips(graphics, mouseX, mouseY);

		for(int i = 0; i < 3; i++) for(int j = 0; j < 4; j++) {
			this.renderTankInfo(graphics, chemplant.inputTanks[i + j * 3], mouseX, mouseY, leftPos + 60 + i * 5, topPos + 20 + j * 22, 3, 16);
			this.renderTankInfo(graphics, chemplant.outputTanks[i + j * 3], mouseX, mouseY, leftPos + 189 + i * 5, topPos + 20 + j * 22, 3, 16);
		}

		this.renderTankInfo(graphics, chemplant.water, mouseX, mouseY, leftPos + 224, topPos + 125, 7, 52);
		this.renderTankInfo(graphics, chemplant.lps, mouseX, mouseY, leftPos + 233, topPos + 125, 7, 52);

		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 224, topPos + 18, 16, 68, chemplant.power, chemplant.maxPower);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, 106 - font.width(name) / 2, 6, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 26, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, 248, 116);
		drawTexturedModalRect(graphics, texture, leftPos + 18, topPos + 116, 18, 116, 230, 100);

		int p = (int) (chemplant.power * 68 / Math.max(chemplant.maxPower, 1));
		drawTexturedModalRect(graphics, texture, leftPos + 224, topPos + 86 - p, 0, 184 - p, 16, p);

		for(int i = 0; i < 4; i++) if(chemplant.chemplantModule[i].progress > 0) {
			int j = (int) Math.ceil(22 * chemplant.chemplantModule[i].progress);
			drawTexturedModalRect(graphics, texture, leftPos + 113, topPos + 29 + i * 22, 0, 216, j, 6);
		}

		for(int g = 0; g < 4; g++) {
			GenericRecipe recipe = chemplant.chemplantModule[g].getRecipe();

			/// LEFT LED
			if(chemplant.didProcess[g]) {
				drawTexturedModalRect(graphics, texture, leftPos + 113, topPos + 21 + g * 22, 4, 222, 4, 4);
			} else if(recipe != null) {
				drawTexturedModalRect(graphics, texture, leftPos + 113, topPos + 21 + g * 22, 0, 222, 4, 4);
			}

			/// RIGHT LED
			if(chemplant.didProcess[g]) {
				drawTexturedModalRect(graphics, texture, leftPos + 121, topPos + 21 + g * 22, 4, 222, 4, 4);
			} else if(recipe != null && chemplant.power >= recipe.power && chemplant.canCool()) {
				drawTexturedModalRect(graphics, texture, leftPos + 121, topPos + 21 + g * 22, 0, 222, 4, 4);
			}
		}

		this.renderRecipeIcons(graphics);

		for(int i = 0; i < 3; i++) for(int j = 0; j < 4; j++) {
			this.renderTank(graphics, chemplant.inputTanks[i + j * 3], leftPos + 60 + i * 5, topPos + 36 + j * 22, 3, 16);
			this.renderTank(graphics, chemplant.outputTanks[i + j * 3], leftPos + 189 + i * 5, topPos + 36 + j * 22, 3, 16);
		}

		this.renderTank(graphics, chemplant.water, leftPos + 224, topPos + 177, 7, 52);
		this.renderTank(graphics, chemplant.lps, leftPos + 233, topPos + 177, 7, 52);
	}
}
