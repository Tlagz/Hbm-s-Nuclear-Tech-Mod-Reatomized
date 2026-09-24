package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineOilWell;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.oil.TileEntityOilDrillBase;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachineOilWell extends GuiInfoContainer<ContainerMachineOilWell> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/machine/gui_well.png");
	private final TileEntityOilDrillBase derrick;

	public GUIMachineOilWell(ContainerMachineOilWell menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		derrick = menu.tile;

		this.imageWidth = 184;
		this.imageHeight = 190;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {

		this.renderTankInfo(graphics, derrick.tanks[0], mouseX, mouseY, leftPos + 76, topPos + 74 - 52, 16, 52);
		this.renderTankInfo(graphics, derrick.tanks[1], mouseX, mouseY, leftPos + 112, topPos + 74 - 52, 16, 52);

		if(derrick.tanks.length >= 3) {
			this.renderTankInfo(graphics, derrick.tanks[2], mouseX, mouseY, leftPos + 54, topPos + 45, 6, 32);
		}

		String[] upgradeText = new String[5];
		upgradeText[0] = I18nUtil.resolveKey("desc.gui.upgrade");
		upgradeText[1] = I18nUtil.resolveKey("desc.gui.upgrade.speed");
		upgradeText[2] = I18nUtil.resolveKey("desc.gui.upgrade.power");
		upgradeText[3] = I18nUtil.resolveKey("desc.gui.upgrade.afterburner");
		upgradeText[4] = I18nUtil.resolveKey("desc.gui.upgrade.overdrive");
		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 160, topPos + 21, 8, 8, mouseX, mouseY, upgradeText);

		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 8, topPos + 22, 16, 34, derrick.power, derrick.getMaxPower());
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, 126 - font.width(name) / 2, 10, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 12, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		int i = (int) (derrick.getPower() * 34 / derrick.getMaxPower());
		drawTexturedModalRect(graphics, texture, leftPos + 8, topPos + 56 - i, 184, 34 - i, 16, i);

		int k = derrick.indicator;

		if(k != 0)
			drawTexturedModalRect(graphics, texture, leftPos + 50, topPos + 19, 184 + (k - 1) * 14, 34, 14, 14);

		if(derrick.tanks.length < 3) {
			drawTexturedModalRect(graphics, texture, leftPos + 48, topPos + 44, 200, 0, 18, 34);
		}

		this.renderTank(graphics, derrick.tanks[0], leftPos + 76, topPos + 74, 16, 52);
		this.renderTank(graphics, derrick.tanks[1], leftPos + 112, topPos + 74, 16, 52);

		if(derrick.tanks.length > 2) {
			this.renderTank(graphics, derrick.tanks[2], leftPos + 54, topPos + 77, 6, 32);
		}

		this.drawInfoPanel(graphics, leftPos + 160, topPos + 21, 8, 8, 8);
	}
}
