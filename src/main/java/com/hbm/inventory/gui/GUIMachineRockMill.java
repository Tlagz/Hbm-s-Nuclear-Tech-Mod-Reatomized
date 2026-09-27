package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineRockMill;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.lib.RefStrings;
import com.hbm.module.machine.ModuleMachineBase;
import com.hbm.tileentity.machine.TileEntityMachineRockMill;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachineRockMill extends GuiInfoContainerProcessor<ContainerMachineRockMill> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_rockmill.png");
	private final TileEntityMachineRockMill rockMill;

	public GUIMachineRockMill(ContainerMachineRockMill menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.rockMill = menu.tile;

		this.processorModule = new ModuleMachineBase[] {rockMill.rockMillModule};

		this.imageWidth = 176;
		this.imageHeight = 220;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override public int[][] getSelectorPositions() { return new int[][] {{7, 89, 1}}; }
	@Override public BlockPos getControlReceiver() { return this.rockMill.getBlockPos(); }
	@Override public ResourceLocation getTexture() { return texture; }

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		super.drawTooltips(graphics, mouseX, mouseY);

		this.renderTankInfo(graphics, rockMill.inputTanks[0], mouseX, mouseY, leftPos + 8, topPos + 63, 52, 16);
		this.renderTankInfo(graphics, rockMill.outputTanks[0], mouseX, mouseY, leftPos + 80, topPos + 63, 52, 16);

		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 152, topPos + 18, 16, 71, rockMill.power, rockMill.maxPower);
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

		int p = (int) (rockMill.power * 71 / Math.max(rockMill.maxPower, 1));
		drawTexturedModalRect(graphics, texture, leftPos + 152, topPos + 89 - p, 176, 71 - p, 16, p);

		if(rockMill.rockMillModule.progress > 0) {
			int j = (int) Math.ceil(70 * rockMill.rockMillModule.progress);
			drawTexturedModalRect(graphics, texture, leftPos + 62, topPos + 90, 176, 71, j, 16);
		}

		GenericRecipe recipe = rockMill.rockMillModule.getRecipe();
		this.renderStandardLEDs(graphics, rockMill.didProcess, recipe, rockMill.power, 51, 85, 195, 0);
		this.renderRecipeIcons(graphics);

		this.renderTankHorizontal(graphics, rockMill.inputTanks[0], leftPos + 8, topPos + 79, 52, 16);
		this.renderTankHorizontal(graphics, rockMill.outputTanks[0], leftPos + 80, topPos + 79, 52, 16);
	}
}
