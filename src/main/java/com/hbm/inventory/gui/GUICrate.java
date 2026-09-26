package com.hbm.inventory.gui;

import com.hbm.blocks.generic.BlockStorageCrate.CrateType;
import com.hbm.inventory.container.ContainerCrate;
import com.hbm.lib.RefStrings;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** The original's GUICrateIron/Steel/Desh/Tungsten and GUISafe, the texture and size come from the crate type */
public class GUICrate extends GuiInfoContainer<ContainerCrate> {

	private final CrateType type;
	private final ResourceLocation texture;

	public GUICrate(ContainerCrate menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.type = menu.type;
		this.texture = RefStrings.loc("textures/gui/storage/" + type.texture + ".png");

		this.imageWidth = type.width;
		this.imageHeight = type.height;
		this.inventoryLabelX = type.invX;
		this.inventoryLabelY = this.imageHeight - 96 + (type == CrateType.DESH ? 3 : 2);
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) { }

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		// the tungsten crate has a dark GUI
		int color = type == CrateType.TUNGSTEN ? 0xffffff : 4210752;
		String name = this.title.getString();
		graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2, 6, color, false);
		graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, color, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		// the desh crate's texture is 256x256 with the GUI filling it
		graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
	}
}
