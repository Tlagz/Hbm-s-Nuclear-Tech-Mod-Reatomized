package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineSiren;
import com.hbm.items.machine.ItemCassette.TrackType;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineSiren;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachineSiren extends GuiInfoContainer<ContainerMachineSiren> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/gui_siren.png");
	private final TileEntityMachineSiren siren;

	public GUIMachineSiren(ContainerMachineSiren menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.siren = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 166;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2, 6, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);

		TrackType type = siren.getCurrentType();
		if(type != null) {
			int color = type.getColor();
			graphics.drawString(font, type.getTrackTitle(), 46, 28, color, false);
			graphics.drawString(font, "Type: " + type.getType().name(), 46, 40, color, false);
			graphics.drawString(font, "Volume: " + type.getVolume(), 46, 52, color, false);
		}
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
	}
}
