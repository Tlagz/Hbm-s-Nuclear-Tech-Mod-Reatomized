package com.hbm.inventory.gui;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.container.ContainerBatterySocket;
import com.hbm.lib.RefStrings;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.storage.TileEntityBatterySocket;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GUIBatterySocket extends GuiInfoContainer<ContainerBatterySocket> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/storage/gui_battery_socket.png");
	private final TileEntityBatterySocket battery;

	public GUIBatterySocket(ContainerBatterySocket menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.battery = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 181;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {

		ItemStack stack = battery.slots.get(0);
		if(stack.getItem() instanceof IBatteryItem item) {

			String deltaText = BobMathUtil.getShortNumber(Math.abs(battery.delta)) + "HE/s";

			if(battery.delta > 0) deltaText = ChatFormatting.GREEN + "+" + deltaText;
			else if(battery.delta < 0) deltaText = ChatFormatting.RED + "-" + deltaText;
			else deltaText = ChatFormatting.YELLOW + "+" + deltaText;

			this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 62, topPos + 69 - 52, 34, 52, mouseX, mouseY,
					BobMathUtil.getShortNumber(item.getCharge(stack)) + "/" + BobMathUtil.getShortNumber(item.getMaxCharge(stack)) + "HE", deltaText);
		}

		String lang = switch(battery.priority) {
		case LOW -> "low";
		case HIGH -> "high";
		default -> "normal";
		};

		List<String> priority = new ArrayList<>();
		priority.add(I18nUtil.resolveKey("battery.priority." + lang));
		priority.add(I18nUtil.resolveKey("battery.priority.recommended"));
		for(String s : I18nUtil.resolveKeyArray("battery.priority." + lang + ".desc")) priority.add(s);

		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 125, topPos + 35, 16, 16, mouseX, mouseY, priority);
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {

		int x = (int) mx;
		int y = (int) my;
		CompoundTag data = new CompoundTag();

		if(this.checkClick(x, y, 106, 16, 18, 18)) data.putBoolean("low", true);
		if(this.checkClick(x, y, 106, 52, 18, 18)) data.putBoolean("high", true);
		if(this.checkClick(x, y, 125, 35, 16, 16)) data.putBoolean("priority", true);

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
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		ItemStack stack = battery.slots.get(0);
		if(stack.getItem() instanceof IBatteryItem item) {
			long power = item.getCharge(stack);
			long maxPower = item.getMaxCharge(stack);

			if(power > Long.MAX_VALUE / 100) {
				power /= 100;
				maxPower /= 100;
			}
			if(maxPower <= 1) maxPower = 1;
			int p = (int) (power * 52 / maxPower);
			drawTexturedModalRect(graphics, texture, leftPos + 62, topPos + 69 - p, 176, 52 - p, 34, p);
		}

		drawTexturedModalRect(graphics, texture, leftPos + 106, topPos + 16, 176, 52 + battery.redLow * 18, 18, 18);
		drawTexturedModalRect(graphics, texture, leftPos + 106, topPos + 52, 176, 52 + battery.redHigh * 18, 18, 18);
		drawTexturedModalRect(graphics, texture, leftPos + 125, topPos + 35, 194, 52 + battery.priority.ordinal() * 16 - 16, 16, 16);
	}
}
