package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineCyclotron;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineCyclotron;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachineCyclotron extends GuiInfoContainer<ContainerMachineCyclotron> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/machine/gui_cyclotron.png");
	private final TileEntityMachineCyclotron cyclotron;

	public GUIMachineCyclotron(ContainerMachineCyclotron menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.cyclotron = menu.tile;

		this.imageWidth = 190;
		this.imageHeight = 215;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 168, topPos + 18, 16, 63, cyclotron.power, TileEntityMachineCyclotron.maxPower);

		this.renderTankInfo(graphics, cyclotron.tanks[0], mouseX, mouseY, leftPos + 11, topPos + 81, 34, 7);
		this.renderTankInfo(graphics, cyclotron.tanks[1], mouseX, mouseY, leftPos + 11, topPos + 90, 34, 7);
		this.renderTankInfo(graphics, cyclotron.tanks[2], mouseX, mouseY, leftPos + 107, topPos + 81, 34, 16);

		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 49, topPos + 85, 8, 8, mouseX, mouseY,
				I18nUtil.resolveKey("desc.gui.upgrade"),
				I18nUtil.resolveKey("desc.gui.upgrade.speed"),
				I18nUtil.resolveKey("desc.gui.upgrade.effectiveness"),
				I18nUtil.resolveKey("desc.gui.upgrade.power"));
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, 79 - font.width(name) / 2, 6, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 15, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		int k = (int) cyclotron.getPowerScaled(63);
		drawTexturedModalRect(graphics, texture, leftPos + 168, topPos + 80 - k, 190, 62 - k, 16, k);

		int l = cyclotron.getProgressScaled(34);
		drawTexturedModalRect(graphics, texture, leftPos + 48, topPos + 27, 206, 0, l, 34);

		if(l > 0)
			drawTexturedModalRect(graphics, texture, leftPos + 172, topPos + 4, 190, 63, 9, 12);

		this.drawInfoPanel(graphics, leftPos + 49, topPos + 85, 8, 8, 8);

		this.renderTankHorizontal(graphics, cyclotron.tanks[0], leftPos + 11, topPos + 88, 34, 7);
		this.renderTankHorizontal(graphics, cyclotron.tanks[1], leftPos + 11, topPos + 97, 34, 7);
		this.renderTankHorizontal(graphics, cyclotron.tanks[2], leftPos + 107, topPos + 97, 34, 16);
	}
}
