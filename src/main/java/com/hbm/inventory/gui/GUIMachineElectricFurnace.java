package com.hbm.inventory.gui;

import com.hbm.blocks.machine.MachineElectricFurnace;
import com.hbm.inventory.container.ContainerElectricFurnace;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineElectricFurnace;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachineElectricFurnace extends GuiInfoContainer<ContainerElectricFurnace> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_electric_furnace.png");
	private final TileEntityMachineElectricFurnace furnace;

	public GUIMachineElectricFurnace(ContainerElectricFurnace menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.furnace = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 186;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 152, topPos + 52 - 34, 16, 34, furnace.power, TileEntityMachineElectricFurnace.maxPower);

		String[] upgradeText = new String[3];
		upgradeText[0] = I18nUtil.resolveKey("desc.gui.upgrade");
		upgradeText[1] = I18nUtil.resolveKey("desc.gui.upgrade.speed");
		upgradeText[2] = I18nUtil.resolveKey("desc.gui.upgrade.power");
		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 115, topPos + 19, 8, 8, mouseX, mouseY, upgradeText);
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

		if(furnace.hasPower()) {
			int p = (int) furnace.getPowerScaled(34);
			drawTexturedModalRect(graphics, texture, leftPos + 152, topPos + 52 - p, 176, 64 - p, 16, p);
		}

		if(furnace.getBlockState().hasProperty(MachineElectricFurnace.LIT) && furnace.getBlockState().getValue(MachineElectricFurnace.LIT)) {
			drawTexturedModalRect(graphics, texture, leftPos + 45, topPos + 20, 192, 12, 18, 16);
			drawTexturedModalRect(graphics, texture, leftPos + 46, topPos + 47, 192, 28, 18, 16);
		}

		int p = furnace.getProgressScaled(28);
		drawTexturedModalRect(graphics, texture, leftPos + 43, topPos + 36, 176, 0, p, 12);

		this.drawInfoPanel(graphics, leftPos + 115, topPos + 19, 8, 8, 8);
	}
}
