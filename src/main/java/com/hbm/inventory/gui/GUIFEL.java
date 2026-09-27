package com.hbm.inventory.gui;

import java.awt.Color;

import com.hbm.inventory.container.ContainerFEL;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import com.hbm.lib.RefStrings;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityFEL;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class GUIFEL extends GuiInfoContainer<ContainerFEL> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/machine/gui_fel.png");
	private final TileEntityFEL fel;

	public GUIFEL(ContainerFEL menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.fel = menu.tile;

		this.imageWidth = 203;
		this.imageHeight = 169;
		this.inventoryLabelY = this.imageHeight - 98;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 182, topPos + 27, 16, 113, fel.power, TileEntityFEL.maxPower);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if(leftPos + 142 <= x && leftPos + 142 + 29 > x && topPos + 41 < y && topPos + 41 + 17 >= y) {
			this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			CompoundTag data = new CompoundTag();
			data.putBoolean("toggle", true);
			NBTControlPacket.send(data, fel.getBlockPos());
			return true;
		}
		return super.mouseClicked(x, y, button);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, 90 + imageWidth / 2 - font.width(name) / 2, 7, 0xffffff, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);

		if(fel.missingValidSilex && fel.isOn) {
			graphics.drawString(font, "ERR.", 55 + imageWidth / 2 - font.width(name) / 2, 9, 0xFF0000, false);
		} else if(fel.isOn) {
			graphics.drawString(font, "LIVE", 54 + imageWidth / 2 - font.width(name) / 2, 9, 0x00FF00, false);
		}
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		if(fel.isOn) drawTexturedModalRect(graphics, texture, leftPos + 142, topPos + 41, 203, 0, 29, 17);

		int k = (int) fel.getPowerScaled(114);
		drawTexturedModalRect(graphics, texture, leftPos + 182, topPos + 27 + 113 - k, 203, 17 + 113 - k, 16, k);

		// the beam leaving the laser, and coming out on the left edge of the screen
		if(fel.power > TileEntityFEL.powerReq * Math.pow(2, fel.mode.ordinal()) && fel.isOn && fel.mode != EnumWavelengths.NULL && fel.distance > 0) {
			int color = fel.mode != EnumWavelengths.VISIBLE ? fel.mode.guiColor : Color.HSBtoRGB(fel.getLevel().getGameTime() / 50.0F, 0.5F, 1F) & 16777215;
			graphics.fill(leftPos + 113, topPos + 29, leftPos + 135, topPos + 34, 0xFF000000 | color);
			graphics.fill(0, topPos + 29, leftPos + 4, topPos + 34, 0xFF000000 | color);
		}
	}
}
