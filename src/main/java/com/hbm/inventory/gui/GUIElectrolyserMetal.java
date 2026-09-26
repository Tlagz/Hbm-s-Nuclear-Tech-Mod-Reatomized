package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerElectrolyserMetal;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.lib.RefStrings;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityElectrolyser;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class GUIElectrolyserMetal extends GuiInfoContainer<ContainerElectrolyserMetal> {

	public static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_electrolyser_metal.png");
	private final TileEntityElectrolyser electrolyser;

	public GUIElectrolyserMetal(ContainerElectrolyserMetal menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.electrolyser = menu.tile;

		this.imageWidth = 210;
		this.imageHeight = 204;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.renderTankInfo(graphics, electrolyser.tanks[3], mouseX, mouseY, leftPos + 36, topPos + 18, 16, 52);
		metalInfo(graphics, electrolyser.leftStack, mouseX, mouseY, leftPos + 58);
		metalInfo(graphics, electrolyser.rightStack, mouseX, mouseY, leftPos + 96);
		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 186, topPos + 18, 16, 89, electrolyser.power, TileEntityElectrolyser.maxPower);
	}

	private void metalInfo(GuiGraphics graphics, MaterialStack stack, int mouseX, int mouseY, int x) {
		if(stack != null) {
			this.drawCustomInfoStat(graphics, mouseX, mouseY, x, topPos + 18, 34, 42, mouseX, mouseY, ChatFormatting.YELLOW + I18nUtil.resolveKey(stack.material.getUnlocalizedName()) + ": " + Mats.formatAmount(stack.amount, Screen.hasShiftDown()));
		} else {
			this.drawCustomInfoStat(graphics, mouseX, mouseY, x, topPos + 18, 34, 42, mouseX, mouseY, ChatFormatting.RED + "Empty");
		}
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {

		// switch to the fluid mode
		if(leftPos + 8 <= x && leftPos + 8 + 54 > x && topPos + 82 < y && topPos + 82 + 12 >= y) {
			this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			CompoundTag data = new CompoundTag();
			data.putBoolean("sgf", true);
			NBTControlPacket.send(data, electrolyser.getBlockPos());
			return true;
		}

		return super.mouseClicked(x, y, button);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, (imageWidth / 2 - font.width(name) / 2) - 16, 7, 0xffffff, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		metalBar(graphics, electrolyser.leftStack, leftPos + 58);
		metalBar(graphics, electrolyser.rightStack, leftPos + 96);

		int p = (int) (electrolyser.power * 89 / TileEntityElectrolyser.maxPower);
		drawTexturedModalRect(graphics, texture, leftPos + 186, topPos + 107 - p, 210, 89 - p, 16, p);

		if(electrolyser.power >= electrolyser.usageOre)
			drawTexturedModalRect(graphics, texture, leftPos + 190, topPos + 4, 226, 25, 9, 12);

		int o = electrolyser.processOreTime <= 0 ? 0 : electrolyser.progressOre * 26 / electrolyser.processOreTime;
		drawTexturedModalRect(graphics, texture, leftPos + 7, topPos + 71 - o, 226, 25 - o, 22, o);

		this.renderTank(graphics, electrolyser.tanks[3], leftPos + 36, topPos + 70, 16, 52);
	}

	/** The molten metal stored for one side, tinted in its molten color */
	private void metalBar(GuiGraphics graphics, MaterialStack stack, int x) {
		if(stack == null) return;
		int p = stack.amount * 42 / electrolyser.maxMaterial;
		int color = stack.material.moltenColor;
		graphics.setColor(((color >> 16) & 0xFF) / 255F, ((color >> 8) & 0xFF) / 255F, (color & 0xFF) / 255F, 1F);
		drawTexturedModalRect(graphics, texture, x, topPos + 60 - p, 210, 131 - p, 34, p);
		graphics.setColor(1F, 1F, 1F, 1F);
	}
}
