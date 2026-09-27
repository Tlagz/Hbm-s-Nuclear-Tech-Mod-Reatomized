package com.hbm.inventory.gui;

import java.util.List;

import com.hbm.inventory.container.ContainerMachineRotaryFurnace;
import com.hbm.inventory.material.Mats;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineRotaryFurnace;
import com.hbm.util.i18n.I18nUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class GUIMachineRotaryFurnace extends GuiInfoContainer<ContainerMachineRotaryFurnace> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_rotary_furnace.png");
	private final TileEntityMachineRotaryFurnace furnace;

	public GUIMachineRotaryFurnace(ContainerMachineRotaryFurnace menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.furnace = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 186;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int x, int y) {

		this.renderTankInfo(graphics, furnace.tanks[0], x, y, leftPos + 8, topPos + 36, 52, 16);
		this.renderTankInfo(graphics, furnace.tanks[1], x, y, leftPos + 134, topPos + 18, 16, 52);
		this.renderTankInfo(graphics, furnace.tanks[2], x, y, leftPos + 152, topPos + 18, 16, 52);

		Slot slot = this.menu.getSlot(4);
		if(this.menu.getCarried().isEmpty() && this.isHovering(slot, x, y) && !slot.hasItem()) {
			List<String> bonuses = TileEntityMachineRotaryFurnace.burnModule.getDesc();
			if(!bonuses.isEmpty()) drawInfo(graphics, bonuses.toArray(new String[0]), x, y);
		}

		if(furnace.output == null) {
			this.drawCustomInfoStat(graphics, x, y, leftPos + 98, topPos + 18, 16, 52, x, y, ChatFormatting.RED + "Empty");
		} else {
			this.drawCustomInfoStat(graphics, x, y, leftPos + 98, topPos + 18, 16, 52, x, y, ChatFormatting.YELLOW +
					I18nUtil.resolveKey(furnace.output.material.getUnlocalizedName()) + ": " + Mats.formatAmount(furnace.output.amount, Screen.hasShiftDown()));
		}
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, (imageWidth - 54) / 2 - font.width(name) / 2, 6, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		int p = (int) Math.ceil(furnace.progress * 33);
		drawTexturedModalRect(graphics, texture, leftPos + 63, topPos + 30, 176, 0, p, 10);

		if(furnace.maxBurnTime > 0) {
			int b = furnace.burnTime * 14 / furnace.maxBurnTime;
			drawTexturedModalRect(graphics, texture, leftPos + 26, topPos + 69 - b, 176, 24 - b, 14, b);
		}

		// the molten output in its color
		if(furnace.output != null) {
			int hex = furnace.output.material.moltenColor;
			int amount = furnace.output.amount * 52 / TileEntityMachineRotaryFurnace.maxOutput;
			graphics.setColor(((hex >> 16) & 0xFF) / 255F, ((hex >> 8) & 0xFF) / 255F, (hex & 0xFF) / 255F, 1F);
			drawTexturedModalRect(graphics, texture, leftPos + 98, topPos + 70 - amount, 176, 76 - amount, 16, amount);
			RenderSystem.enableBlend();
			graphics.setColor(1F, 1F, 1F, 0.3F);
			drawTexturedModalRect(graphics, texture, leftPos + 98, topPos + 70 - amount, 176, 76 - amount, 16, amount);
			RenderSystem.disableBlend();
			graphics.setColor(1F, 1F, 1F, 1F);
		}

		this.renderTankHorizontal(graphics, furnace.tanks[0], leftPos + 8, topPos + 52, 52, 16);
		this.renderTank(graphics, furnace.tanks[1], leftPos + 134, topPos + 70, 16, 52);
		this.renderTank(graphics, furnace.tanks[2], leftPos + 152, topPos + 70, 16, 52);
	}
}
