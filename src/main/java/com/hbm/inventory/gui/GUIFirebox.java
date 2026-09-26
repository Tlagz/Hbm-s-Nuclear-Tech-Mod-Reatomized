package com.hbm.inventory.gui;

import java.util.List;
import java.util.Locale;

import com.hbm.inventory.container.ContainerFirebox;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityFireboxBase;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class GUIFirebox extends GuiInfoContainer<ContainerFirebox> {

	private final TileEntityFireboxBase firebox;
	private final ResourceLocation texture;

	public GUIFirebox(ContainerFirebox menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		firebox = menu.tile;
		// the heating oven shares the firebox's GUI with its own texture and a white title
		texture = RefStrings.loc(firebox instanceof com.hbm.tileentity.machine.TileEntityHeaterOven ? "textures/gui/machine/gui_heating_oven.png" : "textures/gui/machine/gui_firebox.png");

		this.imageWidth = 176;
		this.imageHeight = 168;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int x, int y) {

		if(this.menu.getCarried().isEmpty()) {

			for(int i = 0; i < 2; ++i) {
				Slot slot = this.menu.slots.get(i);

				if(this.isHovering(slot, x, y) && !slot.hasItem()) {

					List<String> bonuses = this.firebox.getModule().getDesc();

					if(!bonuses.isEmpty()) {
						graphics.renderComponentTooltip(font, bonuses.stream().map(s -> (Component) Component.literal(s)).toList(), x, y);
					}
				}
			}
		}

		this.drawCustomInfoStat(graphics, x, y, leftPos + 80, topPos + 27, 71, 7, x, y, new String[] { String.format(Locale.US, "%,d", firebox.heatEnergy) + " / " + String.format(Locale.US, "%,d", firebox.getMaxHeat()) + "TU" });
		this.drawCustomInfoStat(graphics, x, y, leftPos + 80, topPos + 36, 71, 7, x, y, new String[] { firebox.burnHeat + "TU/t", (firebox.burnTime / 20) + "s" });
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, this.imageWidth / 2 - font.width(name) / 2, 6, firebox instanceof com.hbm.tileentity.machine.TileEntityHeaterOven ? 0xffffff : 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		int i = firebox.heatEnergy * 69 / firebox.getMaxHeat();
		drawTexturedModalRect(graphics, texture, leftPos + 81, topPos + 28, 176, 0, i, 5);

		int j = firebox.burnTime * 70 / Math.max(firebox.maxBurnTime, 1);
		drawTexturedModalRect(graphics, texture, leftPos + 81, topPos + 37, 176, 5, j, 5);

		if(firebox.wasOn) {
			drawTexturedModalRect(graphics, texture, leftPos + 25, topPos + 26, 176, 10, 18, 18);
		}
	}
}
