package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineShredder;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineShredder;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachineShredder extends GuiInfoContainer<ContainerMachineShredder> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_shredder.png");
	private final TileEntityMachineShredder shredder;

	public GUIMachineShredder(ContainerMachineShredder menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		shredder = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 233;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	private boolean bladesBroken() {
		return shredder.getGearLeft() == 0 || shredder.getGearLeft() == 3 || shredder.getGearRight() == 0 || shredder.getGearRight() == 3;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 8, topPos + 106 - 88, 16, 88, shredder.power, TileEntityMachineShredder.maxPower);

		if(bladesBroken()) {
			this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos - 16, topPos + 36, 16, 16, leftPos - 8, topPos + 36 + 16, "Error: Shredder blades are broken or missing!");
		}
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, 106 - font.width(name) / 2, 6, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		if(shredder.power > 0) {
			int i = (int) shredder.getPowerScaled(88);
			drawTexturedModalRect(graphics, texture, leftPos + 8, topPos + 106 - i, 176, 160 - i, 16, i);
		}

		int j1 = shredder.getDiFurnaceProgressScaled(34);
		drawTexturedModalRect(graphics, texture, leftPos + 63, topPos + 89, 176, 54, j1 + 1, 18);

		// blade states: fine, worn, broken
		int left = shredder.getGearLeft();
		if(left != 0) drawTexturedModalRect(graphics, texture, leftPos + 43, topPos + 71, 176, (left - 1) * 18, 18, 18);
		int right = shredder.getGearRight();
		if(right != 0) drawTexturedModalRect(graphics, texture, leftPos + 79, topPos + 71, 194, (right - 1) * 18, 18, 18);

		if(bladesBroken())
			this.drawInfoPanel(graphics, leftPos - 16, topPos + 36, 16, 16, 6);
	}
}
