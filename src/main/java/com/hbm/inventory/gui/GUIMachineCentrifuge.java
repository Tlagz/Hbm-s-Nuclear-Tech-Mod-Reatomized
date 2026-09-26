package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerCentrifuge;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineCentrifuge;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachineCentrifuge extends GuiInfoContainer<ContainerCentrifuge> {

	public static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_centrifuge.png");
	private final TileEntityMachineCentrifuge centrifuge;

	public GUIMachineCentrifuge(ContainerCentrifuge menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.centrifuge = menu.tile;

		this.imageWidth = 182;
		this.imageHeight = 189;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 8, topPos + 18, 16, 37, centrifuge.power, TileEntityMachineCentrifuge.maxPower);

		String[] upgradeText = new String[4];
		upgradeText[0] = I18nUtil.resolveKey("desc.gui.upgrade");
		upgradeText[1] = I18nUtil.resolveKey("desc.gui.upgrade.speed");
		upgradeText[2] = I18nUtil.resolveKey("desc.gui.upgrade.power");
		upgradeText[3] = I18nUtil.resolveKey("desc.gui.upgrade.overdrive");
		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 160, topPos + 16, 8, 8, mouseX, mouseY, upgradeText);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, this.imageWidth / 2 + 36 / 2 - font.width(name) / 2, 6, 0xffffff, false);
		graphics.drawString(font, playerInventoryTitle, 11, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		if(centrifuge.hasPower()) {
			int i1 = (int) centrifuge.getPowerRemainingScaled(37);
			drawTexturedModalRect(graphics, texture, leftPos + 8, topPos + 55 - i1, 182, 37 - i1, 16, i1);
		}

		if(centrifuge.isProcessing()) {
			int p = centrifuge.getCentrifugeProgressScaled(145);

			for(int i = 0; i < 4; i++) {
				int h = Math.min(p, 36);
				drawTexturedModalRect(graphics, texture, leftPos + 72 + i * 20, topPos + 57 - h, 182, 73 - h, 12, h);
				p -= h;

				if(p <= 0)
					break;
			}
		}

		this.drawInfoPanel(graphics, leftPos + 160, topPos + 16, 8, 8, 8);
	}
}
