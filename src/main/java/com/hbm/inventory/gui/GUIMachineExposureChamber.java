package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineExposureChamber;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineExposureChamber;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachineExposureChamber extends GuiInfoContainer<ContainerMachineExposureChamber> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_exposure_chamber.png");
	private final TileEntityMachineExposureChamber chamber;

	public GUIMachineExposureChamber(ContainerMachineExposureChamber menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.chamber = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 186;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 152, topPos + 18, 16, 34, chamber.power, TileEntityMachineExposureChamber.maxPower);
		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 26, topPos + 36, 9, 16, mouseX, mouseY, chamber.savedParticles + " / " + TileEntityMachineExposureChamber.maxParticles);
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

		int p = chamber.progress * 42 / (chamber.processTime + 1);
		drawTexturedModalRect(graphics, texture, leftPos + 36, topPos + 39, 192, 0, p, 10);

		int c = chamber.savedParticles * 16 / TileEntityMachineExposureChamber.maxParticles;
		drawTexturedModalRect(graphics, texture, leftPos + 26, topPos + 52 - c, 192, 26 - c, 9, c);

		int e = (int) (chamber.power * 34 / TileEntityMachineExposureChamber.maxPower);
		drawTexturedModalRect(graphics, texture, leftPos + 152, topPos + 52 - e, 176, 34 - e, 16, e);

		if(chamber.consumption <= chamber.power) {
			drawTexturedModalRect(graphics, texture, leftPos + 156, topPos + 4, 176, 34, 9, 12);
		}
	}
}
