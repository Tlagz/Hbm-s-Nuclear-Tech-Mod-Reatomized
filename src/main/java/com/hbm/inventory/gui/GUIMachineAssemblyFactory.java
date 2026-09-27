package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineAssemblyFactory;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.lib.RefStrings;
import com.hbm.module.machine.ModuleMachineBase;
import com.hbm.tileentity.machine.TileEntityMachineAssemblyFactory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachineAssemblyFactory extends GuiInfoContainerProcessor<ContainerMachineAssemblyFactory> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_assembly_factory.png");
	private final TileEntityMachineAssemblyFactory assembler;

	public GUIMachineAssemblyFactory(ContainerMachineAssemblyFactory menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.assembler = menu.tile;

		this.processorModule = new ModuleMachineBase[4];
		for(int i = 0; i < 4; i++) this.processorModule[i] = assembler.assemblerModule[i];

		this.imageWidth = 256;
		this.imageHeight = 240;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	public int[][] getSelectorPositions() {
		int[][] positions = new int[4][];
		for(int i = 0; i < 4; i++) positions[i] = new int[] {6 + (i % 2) * 109, 53 + (i / 2) * 56, 4 + i * 14};
		return positions;
	}

	@Override public BlockPos getControlReceiver() { return this.assembler.getBlockPos(); }
	@Override public ResourceLocation getTexture() { return texture; }

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		super.drawTooltips(graphics, mouseX, mouseY);

		for(int j = 0; j < 4; j++) {
			this.renderTankInfo(graphics, assembler.inputTanks[j], mouseX, mouseY, leftPos + 105 + (j % 2) * 109, topPos + 20 + (j / 2) * 56, 5, 32);
			this.renderTankInfo(graphics, assembler.outputTanks[j], mouseX, mouseY, leftPos + 105 + (j % 2) * 109, topPos + 54 + (j / 2) * 56, 5, 16);
		}

		this.renderTankInfo(graphics, assembler.water, mouseX, mouseY, leftPos + 232, topPos + 149, 7, 52);
		this.renderTankInfo(graphics, assembler.lps, mouseX, mouseY, leftPos + 241, topPos + 149, 7, 52);

		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 234, topPos + 18, 16, 92, assembler.power, assembler.maxPower);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, 113 - font.width(name) / 2, 6, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 33, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, 256, 140);
		drawTexturedModalRect(graphics, texture, leftPos + 25, topPos + 140, 25, 140, 231, 100);

		int p = (int) (assembler.power * 92 / Math.max(assembler.maxPower, 1));
		drawTexturedModalRect(graphics, texture, leftPos + 234, topPos + 110 - p, 0, 232 - p, 16, p);

		for(int i = 0; i < 4; i++) if(assembler.assemblerModule[i].progress > 0) {
			int j = (int) Math.ceil(37 * assembler.assemblerModule[i].progress);
			drawTexturedModalRect(graphics, texture, leftPos + 45 + (i % 2) * 109, topPos + 63 + (i / 2) * 56, 0, 240, j, 6);
		}

		for(int g = 0; g < 4; g++) {
			GenericRecipe recipe = assembler.assemblerModule[g].getRecipe();
			int x = leftPos + (g % 2) * 109;
			int y = topPos + 55 + (g / 2) * 56;

			/// LEFT LED
			if(assembler.didProcess[g]) {
				drawTexturedModalRect(graphics, texture, x + 45, y, 4, 236, 4, 4);
			} else if(recipe != null) {
				drawTexturedModalRect(graphics, texture, x + 45, y, 0, 236, 4, 4);
			}

			/// RIGHT LED
			if(assembler.didProcess[g]) {
				drawTexturedModalRect(graphics, texture, x + 53, y, 4, 236, 4, 4);
			} else if(recipe != null && assembler.power >= recipe.power && assembler.canCool()) {
				drawTexturedModalRect(graphics, texture, x + 53, y, 0, 236, 4, 4);
			}
		}

		this.renderRecipeIcons(graphics);

		for(int j = 0; j < 4; j++) {
			this.renderTank(graphics, assembler.inputTanks[j], leftPos + 105 + (j % 2) * 109, topPos + 52 + (j / 2) * 56, 5, 32);
			this.renderTank(graphics, assembler.outputTanks[j], leftPos + 105 + (j % 2) * 109, topPos + 70 + (j / 2) * 56, 5, 16);
		}

		this.renderTank(graphics, assembler.water, leftPos + 232, topPos + 201, 7, 52);
		this.renderTank(graphics, assembler.lps, leftPos + 241, topPos + 201, 7, 52);
	}
}
