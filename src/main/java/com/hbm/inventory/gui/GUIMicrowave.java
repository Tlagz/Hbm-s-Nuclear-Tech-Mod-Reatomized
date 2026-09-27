package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMicrowave;
import com.hbm.lib.RefStrings;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityMicrowave;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class GUIMicrowave extends GuiInfoContainer<ContainerMicrowave> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_microwave.png");
	private final TileEntityMicrowave microwave;

	public GUIMicrowave(ContainerMicrowave menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.microwave = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 168;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 8, topPos + 51 - 34, 16, 34, microwave.power, TileEntityMicrowave.maxPower);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		String key = null;
		if(leftPos + 43 <= x && leftPos + 43 + 18 > x && topPos + 25 < y && topPos + 25 + 18 >= y) key = "up";
		if(leftPos + 43 <= x && leftPos + 43 + 18 > x && topPos + 43 < y && topPos + 43 + 18 >= y) key = "down";

		if(key != null) {
			this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			CompoundTag data = new CompoundTag();
			data.putBoolean(key, true);
			NBTControlPacket.send(data, microwave.getBlockPos());
			return true;
		}
		return super.mouseClicked(x, y, button);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2, 6, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		int i = (int) microwave.getPowerScaled(34);
		drawTexturedModalRect(graphics, texture, leftPos + 8, topPos + 51 - i, 176, 34 - i, 16, i);

		int j = Math.min(microwave.getProgressScaled(23), 22);
		drawTexturedModalRect(graphics, texture, leftPos + 104, topPos + 34, 192, 0, j, 16);

		int k = microwave.getSpeedScaled(34);
		drawTexturedModalRect(graphics, texture, leftPos + 62, topPos + 60 - k, 214, 34 - k, 4, k);
	}
}
