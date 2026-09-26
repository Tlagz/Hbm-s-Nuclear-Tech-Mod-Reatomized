package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerCrystallizer;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineCrystallizer;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUICrystallizer extends GuiInfoContainer<ContainerCrystallizer> {

	public static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_crystallizer_alt.png");
	private final TileEntityMachineCrystallizer acidomatic;

	public GUICrystallizer(ContainerCrystallizer menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.acidomatic = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 204;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 152, topPos + 18, 16, 52, acidomatic.power, TileEntityMachineCrystallizer.maxPower);
		this.renderTankInfo(graphics, acidomatic.tank, mouseX, mouseY, leftPos + 35, topPos + 18, 16, 52);

		String[] upgradeText = new String[4];
		upgradeText[0] = I18nUtil.resolveKey("desc.gui.upgrade");
		upgradeText[1] = I18nUtil.resolveKey("desc.gui.upgrade.speed");
		upgradeText[2] = I18nUtil.resolveKey("desc.gui.upgrade.effectiveness");
		upgradeText[3] = I18nUtil.resolveKey("desc.gui.upgrade.overdrive");
		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 117, topPos + 22, 8, 8, leftPos + 200, topPos + 45, upgradeText);
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

		int i = (int) acidomatic.getPowerScaled(52);
		drawTexturedModalRect(graphics, texture, leftPos + 152, topPos + 70 - i, 176, 64 - i, 16, i);

		int j = acidomatic.getProgressScaled(28);
		drawTexturedModalRect(graphics, texture, leftPos + 80, topPos + 47, 176, 0, j, 12);

		this.drawInfoPanel(graphics, leftPos + 117, topPos + 22, 8, 8, 8);

		this.renderTank(graphics, acidomatic.tank, leftPos + 35, topPos + 70, 16, 52);
	}
}
