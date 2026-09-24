package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineAssemblyMachine;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.lib.RefStrings;
import com.hbm.module.machine.ModuleMachineBase;
import com.hbm.tileentity.machine.TileEntityMachineAssemblyMachine;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachineAssemblyMachine extends GuiInfoContainerProcessor<ContainerMachineAssemblyMachine> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_assembler.png");
	private final TileEntityMachineAssemblyMachine assembler;

	public GUIMachineAssemblyMachine(ContainerMachineAssemblyMachine menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.assembler = menu.tile;

		this.processorModule = new ModuleMachineBase[1];
		this.processorModule[0] = assembler.assemblerModule;

		this.imageWidth = 176;
		this.imageHeight = 256;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override public int[][] getSelectorPositions() { return new int[][] {{7, 125, 1}}; }
	@Override public BlockPos getControlReceiver() { return this.assembler.getBlockPos(); }
	@Override public ResourceLocation getTexture() { return texture; }

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		super.drawTooltips(graphics, mouseX, mouseY);

		this.renderTankInfo(graphics, assembler.inputTank, mouseX, mouseY, leftPos + 8, topPos + 99, 52, 16);
		this.renderTankInfo(graphics, assembler.outputTank, mouseX, mouseY, leftPos + 80, topPos + 99, 52, 16);

		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 152, topPos + 18, 16, 61, assembler.power, assembler.maxPower);
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

		int p = (int) (assembler.power * 61 / Math.max(assembler.maxPower, 1));
		drawTexturedModalRect(graphics, texture, leftPos + 152, topPos + 79 - p, 176, 61 - p, 16, p);

		if(assembler.assemblerModule.progress > 0) {
			int j = (int) Math.ceil(70 * assembler.assemblerModule.progress);
			drawTexturedModalRect(graphics, texture, leftPos + 62, topPos + 126, 176, 61 + (assembler.assemblerModule.restrictedMode ? 16 : 0), j, 16);
		}

		GenericRecipe recipe = assembler.assemblerModule.getRecipe();
		this.renderStandardLEDs(graphics, assembler.didProcess, recipe, assembler.power, 51, 121, 195, 0);
		this.renderRecipeIcons(graphics);

		this.renderTankHorizontal(graphics, assembler.inputTank, leftPos + 8, topPos + 115, 52, 16);
		this.renderTankHorizontal(graphics, assembler.outputTank, leftPos + 80, topPos + 115, 52, 16);
	}
}
