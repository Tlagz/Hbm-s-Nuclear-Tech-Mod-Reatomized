package com.hbm.inventory.gui;

import java.util.List;

import com.hbm.inventory.container.ContainerBlastFurnace;
import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineBlastFurnace;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class GUIBlastFurnace extends GuiInfoContainer<ContainerBlastFurnace> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_blast_furnace.png");
	private final TileEntityMachineBlastFurnace furnace;

	public GUIBlastFurnace(ContainerBlastFurnace menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		furnace = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 222;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int x, int y) {

		if(this.menu.getCarried().isEmpty()) {
			Slot slot = this.menu.getSlot(0);
			if(this.isHovering(slot, x, y) && !slot.hasItem()) {
				List<String> bonuses = this.furnace.burnModule.getHeatDesc();
				if(!bonuses.isEmpty()) drawInfo(graphics, bonuses.toArray(new String[0]), x, y);
			}
		}

		String label = "Speed: " + (int) (furnace.speed * 100) + "%";
		drawCustomInfoStat(graphics, x, y, leftPos + 79, topPos + 62, 18, 18, x, y, label);

		this.renderTankInfo(graphics, furnace.tanks[0], x, y, leftPos + 25, topPos + 71, 18, 18);
		this.renderTankInfo(graphics, furnace.tanks[1], x, y, leftPos + 25, topPos + 17, 18, 18);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, this.imageWidth / 2 - font.width(name) / 2, 6, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float interp, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		int fuel = (int) Math.round((double) furnace.fuel * 26D / (double) TileEntityMachineBlastFurnace.MAX_FUEL);
		int prog = (int) Math.round(furnace.progress * (88D - fuel));

		drawTexturedModalRect(graphics, texture, leftPos + 62, topPos + 106 - prog - fuel, 176, 102 - prog - fuel, 56, prog);

		drawTexturedModalRect(graphics, texture, leftPos + 62, topPos + 106 - fuel, 176, 128 - fuel, 56, fuel);

		if(furnace.isProgressing) {
			drawTexturedModalRect(graphics, texture, leftPos + 81, topPos + 64, 176, 0, 14, 14);
		}

		GUIElements.drawSmoothGauge(graphics, leftPos + 34, topPos + 80, 0, (double) furnace.tanks[0].getFill() / (double) furnace.tanks[0].getMaxFill(), 5, 2, 1, 0x800000);
		GUIElements.drawSmoothGauge(graphics, leftPos + 34, topPos + 26, 0, (double) furnace.tanks[1].getFill() / (double) furnace.tanks[1].getMaxFill(), 5, 2, 1, 0x800000);
	}
}
