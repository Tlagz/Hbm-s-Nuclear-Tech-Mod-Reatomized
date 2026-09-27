package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachinePUREX;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.lib.RefStrings;
import com.hbm.module.machine.ModuleMachineBase;
import com.hbm.tileentity.machine.TileEntityMachinePUREX;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachinePUREX extends GuiInfoContainerProcessor<ContainerMachinePUREX> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_purex.png");
	private final TileEntityMachinePUREX purex;

	public GUIMachinePUREX(ContainerMachinePUREX menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.purex = menu.tile;

		this.processorModule = new ModuleMachineBase[] {purex.purexModule};

		this.imageWidth = 176;
		this.imageHeight = 256;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override public int[][] getSelectorPositions() { return new int[][] {{7, 125, 1}}; }
	@Override public BlockPos getControlReceiver() { return this.purex.getBlockPos(); }
	@Override public ResourceLocation getTexture() { return texture; }

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		super.drawTooltips(graphics, mouseX, mouseY);

		for(int i = 0; i < 3; i++) {
			this.renderTankInfo(graphics, purex.inputTanks[i], mouseX, mouseY, leftPos + 8 + i * 18, topPos + 18, 16, 52);
		}
		this.renderTankInfo(graphics, purex.outputTanks[0], mouseX, mouseY, leftPos + 116, topPos + 36, 16, 52);

		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 152, topPos + 18, 16, 61, purex.power, purex.maxPower);
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

		int p = (int) (purex.power * 61 / Math.max(purex.maxPower, 1));
		drawTexturedModalRect(graphics, texture, leftPos + 152, topPos + 79 - p, 176, 61 - p, 16, p);

		if(purex.purexModule.progress > 0) {
			int j = (int) Math.ceil(70 * purex.purexModule.progress);
			drawTexturedModalRect(graphics, texture, leftPos + 62, topPos + 126, 176, 61, j, 16);
		}

		GenericRecipe recipe = purex.purexModule.getRecipe();
		this.renderStandardLEDs(graphics, purex.didProcess, recipe, purex.power, 51, 121, 195, 0);
		this.renderRecipeIcons(graphics);

		for(int i = 0; i < 3; i++) {
			this.renderTank(graphics, purex.inputTanks[i], leftPos + 8 + i * 18, topPos + 70, 16, 52);
		}
		this.renderTank(graphics, purex.outputTanks[0], leftPos + 116, topPos + 88, 16, 52);
	}
}
