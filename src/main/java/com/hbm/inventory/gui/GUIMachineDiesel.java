package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineDiesel;
import com.hbm.lib.RefStrings;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityMachineDiesel;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachineDiesel extends GuiInfoContainer<ContainerMachineDiesel> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/generators/gui_diesel.png");
	private final TileEntityMachineDiesel diesel;

	public GUIMachineDiesel(ContainerMachineDiesel menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		diesel = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 203;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {

		this.renderTankInfo(graphics, diesel.tank, mouseX, mouseY, leftPos + 35, topPos + 69 - 52, 16, 52);
		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 141, topPos + 69 - 52, 16, 52, diesel.power, diesel.powerCap);

		String[] text = new String[] { "Fuel consumption rate:",
				"  1 mB/t",
				"  20 mB/s",
				"(Consumption rate is constant)" };
		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos - 8, topPos + 36, 16, 16, leftPos, topPos + 36 + 16, text);

		if(!diesel.hasAcceptableFuel()) {
			String[] text2 = new String[] { "Error: The currently set fuel type",
					"is not supported by this engine!" };
			this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos - 8, topPos + 36 + 32, 16, 16, leftPos, topPos + 36 + 16 + 32, text2);
		}
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {

		if(leftPos + 89 <= x && leftPos + 89 + 16 > x && topPos + 61 < y && topPos + 61 + 14 >= y) {
			this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			CompoundTag data = new CompoundTag();
			data.putBoolean("turnOn", true);
			NBTControlPacket.send(data, diesel.getBlockPos());
			return true;
		}

		return super.mouseClicked(x, y, button);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		if(diesel.power > 0) {
			int i = (int) diesel.getPowerScaled(52);
			drawTexturedModalRect(graphics, texture, leftPos + 141, topPos + 69 - i, 176, 52 - i, 16, i);
		}

		if(diesel.isOn) drawTexturedModalRect(graphics, texture, leftPos + 79, topPos + 61, 192, 16, 35, 14);
		if(diesel.wasOn) drawTexturedModalRect(graphics, texture, leftPos + 89, topPos + 42, 192, 0, 16, 16);

		this.drawInfoPanel(graphics, leftPos - 8, topPos + 36, 16, 16, 2);

		if(!diesel.hasAcceptableFuel())
			this.drawInfoPanel(graphics, leftPos - 8, topPos + 36 + 32, 16, 16, 6);

		this.renderTank(graphics, diesel.tank, leftPos + 35, topPos + 69, 16, 52);
	}
}
