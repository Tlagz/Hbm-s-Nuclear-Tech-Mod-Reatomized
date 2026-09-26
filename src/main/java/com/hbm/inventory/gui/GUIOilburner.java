package com.hbm.inventory.gui;

import java.util.Locale;

import com.hbm.inventory.container.ContainerOilburner;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.lib.RefStrings;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityHeaterOilburner;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class GUIOilburner extends GuiInfoContainer<ContainerOilburner> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/machine/gui_oilburner.png");
	private final TileEntityHeaterOilburner heater;

	public GUIOilburner(ContainerOilburner menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		heater = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 203;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int x, int y) {
		this.drawCustomInfoStat(graphics, x, y, leftPos + 116, topPos + 17, 16, 52, x, y, String.format(Locale.US, "%,d", Math.min(heater.heatEnergy, TileEntityHeaterOilburner.maxHeatEnergy)) + " / " + String.format(Locale.US, "%,d", TileEntityHeaterOilburner.maxHeatEnergy) + " TU");

		if(heater.tank.getTankType().hasTrait(FT_Flammable.class)) {
			this.drawCustomInfoStat(graphics, x, y, leftPos + 79, topPos + 34, 18, 18, x, y, heater.setting + " mB/t", String.format(Locale.US, "%,d", (int) (heater.tank.getTankType().getTrait(FT_Flammable.class).getHeatEnergy() / 1000) * heater.setting) + " TU/t");
		}

		this.renderTankInfo(graphics, heater.tank, x, y, leftPos + 44, topPos + 17, 16, 52);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {

		if(leftPos + 80 <= x && leftPos + 80 + 16 > x && topPos + 54 < y && topPos + 54 + 14 >= y) {
			this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			CompoundTag data = new CompoundTag();
			data.putBoolean("toggle", true);
			NBTControlPacket.send(data, heater.getBlockPos());
			return true;
		}

		return super.mouseClicked(x, y, button);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, this.imageWidth / 2 - font.width(name) / 2, 6, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		int i = heater.heatEnergy * 52 / TileEntityHeaterOilburner.maxHeatEnergy;
		drawTexturedModalRect(graphics, texture, leftPos + 116, topPos + 69 - i, 194, 52 - i, 16, i);

		if(heater.isOn) {
			drawTexturedModalRect(graphics, texture, leftPos + 70, topPos + 54, 210, 0, 35, 14);

			if(heater.tank.getFill() > 0 && heater.tank.getTankType().hasTrait(FT_Flammable.class)) {
				drawTexturedModalRect(graphics, texture, leftPos + 79, topPos + 34, 176, 0, 18, 18);
			}
		}

		this.renderTank(graphics, heater.tank, leftPos + 44, topPos + 69, 16, 52);
	}
}
