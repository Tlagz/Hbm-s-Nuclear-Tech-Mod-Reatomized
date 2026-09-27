package com.hbm.inventory.gui;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.container.ContainerMachineRTG;
import com.hbm.items.machine.ItemRTGPellet;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineRTG;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GUIMachineRTG extends GuiInfoContainer<ContainerMachineRTG> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/gui_rtg.png");
	private final TileEntityMachineRTG rtg;

	public GUIMachineRTG(ContainerMachineRTG menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.rtg = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 188;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 146, topPos + 9, 16, 51, rtg.power, rtg.powerMax);
		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 124, topPos + 9, 16, 51, mouseX, mouseY, I18nUtil.resolveKeyArray("desc.gui.rtg.heat", rtg.heat));

		// all pellets and their power
		List<String> pelletText = new ArrayList<>();
		pelletText.add(I18nUtil.resolveKey("desc.gui.rtg.pellets"));
		for(ItemRTGPellet pellet : ItemRTGPellet.pelletList) {
			pelletText.add(I18nUtil.resolveKey("desc.gui.rtg.pelletPower", new ItemStack(pellet).getHoverName().getString(), pellet.getHeat() * 5));
		}
		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos - 12, topPos + 25, 16, 16, leftPos - 8, topPos + 36 + 16, pelletText);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, 60 - font.width(name) / 2, 7, 10925486, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		if(rtg.hasHeat()) {
			int i = rtg.getHeatScaled(51);
			drawTexturedModalRect(graphics, texture, leftPos + 124, topPos + 61 - i, 176, 10 + (51 - i), 16, i);
		}

		if(rtg.hasPower()) {
			int i = (int) rtg.getPowerScaled(51);
			drawTexturedModalRect(graphics, texture, leftPos + 146, topPos + 61 - i, 192, 10 + (51 - i), 16, i);
		}

		this.drawInfoPanel(graphics, leftPos - 12, topPos + 25, 16, 16, 2);
	}
}
