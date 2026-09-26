package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerCompressor;
import com.hbm.lib.RefStrings;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityMachineCompressorBase;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class GUICompressor extends GuiInfoContainer<ContainerCompressor> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_compressor.png");
	private final TileEntityMachineCompressorBase compressor;

	public GUICompressor(ContainerCompressor menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.compressor = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 204;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.renderTankInfo(graphics, compressor.tanks[0], mouseX, mouseY, leftPos + 17, topPos + 18, 16, 52);
		this.renderTankInfo(graphics, compressor.tanks[1], mouseX, mouseY, leftPos + 107, topPos + 18, 16, 52);
		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 152, topPos + 18, 16, 52, compressor.power, TileEntityMachineCompressorBase.maxPower);

		for(int j = 0; j < 5; j++) this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 43 + j * 11, topPos + 46, 8, 14, mouseX, mouseY, j + " PU -> " + (j + 1) + " PU");
	}

	/** The five buttons pick the input pressure */
	@Override
	public boolean mouseClicked(double x, double y, int button) {

		for(int j = 0; j < 5; j++) {
			if(leftPos + 43 + j * 11 <= x && leftPos + 43 + 8 + j * 11 > x && topPos + 46 < y && topPos + 46 + 14 >= y) {
				this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
				CompoundTag data = new CompoundTag();
				data.putInt("compression", j);
				NBTControlPacket.send(data, compressor.getBlockPos());
				return true;
			}
		}

		return super.mouseClicked(x, y, button);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, 70 - font.width(name) / 2, 6, 0xC7C1A3, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		if(compressor.power >= compressor.powerRequirement) {
			drawTexturedModalRect(graphics, texture, leftPos + 156, topPos + 4, 176, 52, 9, 12);
		}

		drawTexturedModalRect(graphics, texture, leftPos + 43 + compressor.tanks[0].getPressure() * 11, topPos + 46, 193, 18, 8, 14);

		int i = compressor.progress * 55 / Math.max(compressor.processTime, 1);
		drawTexturedModalRect(graphics, texture, leftPos + 42, topPos + 26, 192, 0, i, 17);

		int j = (int) (compressor.power * 52 / TileEntityMachineCompressorBase.maxPower);
		drawTexturedModalRect(graphics, texture, leftPos + 152, topPos + 70 - j, 176, 52 - j, 16, j);

		this.renderTank(graphics, compressor.tanks[0], leftPos + 17, topPos + 70, 16, 52);
		this.renderTank(graphics, compressor.tanks[1], leftPos + 107, topPos + 70, 16, 52);
	}
}
