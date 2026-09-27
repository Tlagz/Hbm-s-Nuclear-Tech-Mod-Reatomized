package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineRadGen;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineRadGen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachineRadGen extends GuiInfoContainer<ContainerMachineRadGen> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/reactors/gui_radgen.png");
	private final TileEntityMachineRadGen radgen;

	public GUIMachineRadGen(ContainerMachineRadGen menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.radgen = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 184;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 64, topPos + 83, 48, 4, radgen.power, TileEntityMachineRadGen.maxPower);

		for(int i = 0; i < 12; i++) {

			if(radgen.maxProgress[i] <= 0)
				continue;

			this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 65, topPos + 18 + i * 5, 46, 5, mouseX, mouseY,
					"Slot " + (i + 1) + ":",
					radgen.production[i] + "HE/t for",
					(radgen.maxProgress[i] - radgen.progress[i]) + " ticks (" + ((radgen.maxProgress[i] - radgen.progress[i]) * 100 / radgen.maxProgress[i]) + "%)");
		}
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

		for(int i = 0; i < 12; i++) {

			if(radgen.maxProgress[i] <= 0)
				continue;

			int j = radgen.progress[i] * 44 / radgen.maxProgress[i];
			drawTexturedModalRect(graphics, texture, leftPos + 66, topPos + 19 + i * 5, 176, 0, j, 3);
		}

		int j = (int) (radgen.power * 48 / TileEntityMachineRadGen.maxPower);
		drawTexturedModalRect(graphics, texture, leftPos + 64, topPos + 83, 176, 3, j, 4);
	}
}
