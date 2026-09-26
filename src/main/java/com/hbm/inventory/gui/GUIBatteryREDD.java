package com.hbm.inventory.gui;

import java.math.BigInteger;
import java.util.Locale;

import com.hbm.inventory.container.ContainerBatteryREDD;
import com.hbm.lib.RefStrings;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.storage.TileEntityBatteryREDD;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class GUIBatteryREDD extends GuiInfoContainer<ContainerBatteryREDD> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/storage/gui_battery_redd.png");
	private final TileEntityBatteryREDD battery;

	public GUIBatteryREDD(ContainerBatteryREDD menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.battery = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 181;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) { }

	@Override
	public boolean mouseClicked(double mx, double my, int button) {

		int x = (int) mx;
		int y = (int) my;
		CompoundTag data = new CompoundTag();

		if(this.checkClick(x, y, 133, 16, 18, 18)) data.putBoolean("low", true);
		if(this.checkClick(x, y, 133, 52, 18, 18)) data.putBoolean("high", true);
		if(this.checkClick(x, y, 152, 35, 16, 16)) data.putBoolean("priority", true);

		if(!data.isEmpty()) {
			this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			NBTControlPacket.send(data, battery.getBlockPos());
			return true;
		}

		return super.mouseClicked(mx, my, button);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2, 6, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);

		// the stored energy and the change, half size on the display
		graphics.pose().pushPose();
		graphics.pose().scale(0.5F, 0.5F, 1F);

		String label = String.format(Locale.US, "%,d", battery.power) + " HE";
		graphics.drawString(font, label, 242 - font.width(label), 45, 0x00ff00, false);

		String deltaText = String.format(Locale.US, "%,d", battery.delta) + " HE/s";

		int comp = battery.delta.compareTo(BigInteger.ZERO);
		if(comp > 0) deltaText = ChatFormatting.GREEN + "+" + deltaText;
		else if(comp < 0) deltaText = ChatFormatting.RED + deltaText;
		else deltaText = ChatFormatting.YELLOW + "+" + deltaText;

		graphics.drawString(font, deltaText, 242 - font.width(deltaText), 65, 0x00ff00, false);

		graphics.pose().popPose();
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		drawTexturedModalRect(graphics, texture, leftPos + 133, topPos + 16, 176, 52 + battery.redLow * 18, 18, 18);
		drawTexturedModalRect(graphics, texture, leftPos + 133, topPos + 52, 176, 52 + battery.redHigh * 18, 18, 18);
		drawTexturedModalRect(graphics, texture, leftPos + 152, topPos + 35, 194, 52 + battery.priority.ordinal() * 16 - 16, 16, 16);
	}
}
