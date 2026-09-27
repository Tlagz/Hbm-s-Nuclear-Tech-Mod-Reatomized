package com.hbm.inventory.gui;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.container.ContainerRadiolysis;
import com.hbm.items.machine.ItemRTGPellet;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineRadiolysis;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GUIRadiolysis extends GuiInfoContainer<ContainerRadiolysis> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/gui_radiolysis.png");
	private final TileEntityMachineRadiolysis radiolysis;

	public GUIRadiolysis(ContainerRadiolysis menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.radiolysis = menu.tile;

		this.imageWidth = 230;
		this.imageHeight = 166;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.renderTankInfo(graphics, radiolysis.tanks[0], mouseX, mouseY, leftPos + 61, topPos + 17, 8, 52);
		this.renderTankInfo(graphics, radiolysis.tanks[1], mouseX, mouseY, leftPos + 87, topPos + 17, 12, 16);
		this.renderTankInfo(graphics, radiolysis.tanks[2], mouseX, mouseY, leftPos + 87, topPos + 53, 12, 16);

		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 8, topPos + 17, 16, 34, radiolysis.power, TileEntityMachineRadiolysis.maxPower);

		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos - 16, topPos + 16, 16, 16, leftPos - 8, topPos + 16 + 16, I18nUtil.resolveKeyArray("desc.gui.radiolysis.desc"));
		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos - 16, topPos + 16 + 18, 16, 16, leftPos - 8, topPos + 16 + 18 + 16, I18nUtil.resolveKeyArray("desc.gui.rtg.heat", radiolysis.heat));

		List<String> pelletText = new ArrayList<>();
		pelletText.add(I18nUtil.resolveKey("desc.gui.rtg.pellets"));
		for(ItemRTGPellet pellet : ItemRTGPellet.pelletList) {
			pelletText.add(I18nUtil.resolveKey("desc.gui.rtg.pelletPower", new ItemStack(pellet).getHoverName().getString(), pellet.getHeat() * 10));
		}
		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos - 16, topPos + 16 + 36, 16, 16, leftPos - 8, topPos + 16 + 36 + 16, pelletText);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, 88 - font.width(name) / 2, 6, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		int i = (int) (radiolysis.getPower() * 34 / radiolysis.getMaxPower());
		drawTexturedModalRect(graphics, texture, leftPos + 8, topPos + 51 - i, 240, 34 - i, 16, i);

		this.renderTank(graphics, radiolysis.tanks[0], leftPos + 61, topPos + 69, 8, 52);
		for(int j = 0; j < 2; j++) {
			this.renderTank(graphics, radiolysis.tanks[j + 1], leftPos + 87, topPos + 33 + j * 36, 12, 16);
		}

		this.drawInfoPanel(graphics, leftPos - 16, topPos + 16, 16, 16, 10);
		this.drawInfoPanel(graphics, leftPos - 16, topPos + 16 + 18, 16, 16, 2);
		this.drawInfoPanel(graphics, leftPos - 16, topPos + 16 + 36, 16, 16, 3);
	}
}
