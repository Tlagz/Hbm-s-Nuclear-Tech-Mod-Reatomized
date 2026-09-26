package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineSolderingStation;
import com.hbm.lib.RefStrings;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityMachineSolderingStation;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachineSolderingStation extends GuiInfoContainer<ContainerMachineSolderingStation> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_soldering_station.png");
	private final TileEntityMachineSolderingStation solderer;

	public GUIMachineSolderingStation(ContainerMachineSolderingStation menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.solderer = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 204;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.renderTankInfo(graphics, solderer.tank, mouseX, mouseY, leftPos + 35, topPos + 63, 34, 16);
		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 152, topPos + 18, 16, 52, solderer.getPower(), solderer.getMaxPower());

		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 78, topPos + 67, 8, 8, leftPos + 78, topPos + 67, this.getUpgradeInfo(solderer));

		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 5, topPos + 66, 10, 10, mouseX, mouseY,
				"Recipe Collision Prevention: " + (solderer.collisionPrevention ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF"),
				"Prevents no-fluid recipes from being processed",
				"when fluid is present.");
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {

		if(leftPos + 5 <= x && leftPos + 5 + 10 > x && topPos + 66 < y && topPos + 66 + 10 >= y) {
			this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			CompoundTag data = new CompoundTag();
			data.putBoolean("collision", true);
			NBTControlPacket.send(data, solderer.getBlockPos());
			return true;
		}

		return super.mouseClicked(x, y, button);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2 - 18, 6, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		if(solderer.collisionPrevention) {
			drawTexturedModalRect(graphics, texture, leftPos + 5, topPos + 66, 192, 14, 10, 10);
		}

		int p = (int) (solderer.power * 52 / Math.max(solderer.maxPower, 1));
		drawTexturedModalRect(graphics, texture, leftPos + 152, topPos + 70 - p, 176, 52 - p, 16, p);

		int i = solderer.progress * 33 / Math.max(solderer.processTime, 1);
		drawTexturedModalRect(graphics, texture, leftPos + 72, topPos + 28, 192, 0, i, 14);

		if(solderer.power >= solderer.consumption) {
			drawTexturedModalRect(graphics, texture, leftPos + 156, topPos + 4, 176, 52, 9, 12);
		}

		this.drawInfoPanel(graphics, leftPos + 78, topPos + 67, 8, 8, 8);
		this.renderTankHorizontal(graphics, solderer.tank, leftPos + 35, topPos + 79, 34, 16);
	}
}
