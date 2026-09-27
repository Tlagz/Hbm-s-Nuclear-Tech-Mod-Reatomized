package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerDiFurnace;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityDiFurnace;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class GUIDiFurnace extends GuiInfoContainer<ContainerDiFurnace> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/guidifurnace.png");
	private final TileEntityDiFurnace diFurnace;

	public GUIDiFurnace(ContainerDiFurnace menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.diFurnace = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 166;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	/** Which side each input and the fuel slot take automation from, right click cycles it */
	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		if(!this.menu.getCarried().isEmpty()) return;

		for(int i = 0; i < 3; i++) {
			Slot slot = this.menu.getSlot(i);

			if(this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
				byte dir = i == 0 ? diFurnace.sideUpper : i == 1 ? diFurnace.sideLower : diFurnace.sideFuel;
				String label = ChatFormatting.YELLOW + "Accepts items from: " + Direction.from3DDataValue(dir).name();
				graphics.renderTooltip(font, Component.literal(label), mouseX, mouseY - (slot.hasItem() ? 15 : 0));
				return;
			}
		}
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

		if(diFurnace.hasPower()) {
			int i1 = diFurnace.getPowerRemainingScaled(52);
			drawTexturedModalRect(graphics, texture, leftPos + 44, topPos + 70 - i1, 201, 53 - i1, 16, i1);
		}

		int j1 = diFurnace.getDiFurnaceProgressScaled(24);
		drawTexturedModalRect(graphics, texture, leftPos + 101, topPos + 35, 176, 14, j1 + 1, 17);

		if(diFurnace.hasPower() && (diFurnace.canProcess() || j1 > 0)) {
			drawTexturedModalRect(graphics, texture, leftPos + 63, topPos + 37, 176, 0, 14, 14);
		}
	}
}
