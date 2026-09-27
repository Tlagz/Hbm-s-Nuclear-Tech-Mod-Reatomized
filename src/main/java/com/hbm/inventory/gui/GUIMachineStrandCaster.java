package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineStrandCaster;
import com.hbm.inventory.material.Mats;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineStrandCaster;
import com.hbm.util.i18n.I18nUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachineStrandCaster extends GuiInfoContainer<ContainerMachineStrandCaster> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_strand_caster.png");
	private final TileEntityMachineStrandCaster caster;

	public GUIMachineStrandCaster(ContainerMachineStrandCaster menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.caster = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 214;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int x, int y) {
		String stack = caster.type == null ? ChatFormatting.RED + "Empty" :
				ChatFormatting.YELLOW + I18nUtil.resolveKey(caster.type.getUnlocalizedName()) + ": " + Mats.formatAmount(caster.amount, Screen.hasShiftDown());
		this.drawCustomInfoStat(graphics, x, y, leftPos + 16, topPos + 17, 36, 81, x, y, stack);

		this.renderTankInfo(graphics, caster.water, x, y, leftPos + 82, topPos + 14, 16, 24);
		this.renderTankInfo(graphics, caster.steam, x, y, leftPos + 82, topPos + 65, 16, 24);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2, 4, 0xffffff, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		// the molten metal in its color
		if(caster.amount != 0 && caster.type != null) {
			int targetHeight = Math.min(caster.amount * 79 / caster.getCapacity(), 92);
			int hex = caster.type.moltenColor;
			graphics.setColor(((hex >> 16) & 0xFF) / 255F, ((hex >> 8) & 0xFF) / 255F, (hex & 0xFF) / 255F, 1F);
			drawTexturedModalRect(graphics, texture, leftPos + 17, topPos + 93 - targetHeight, 176, 89 - targetHeight, 34, targetHeight);
			RenderSystem.enableBlend();
			graphics.setColor(1F, 1F, 1F, 0.3F);
			drawTexturedModalRect(graphics, texture, leftPos + 17, topPos + 93 - targetHeight, 176, 89 - targetHeight, 34, targetHeight);
			RenderSystem.disableBlend();
			graphics.setColor(1F, 1F, 1F, 1F);
		}

		this.renderTank(graphics, caster.water, leftPos + 82, topPos + 38, 16, 24);
		this.renderTank(graphics, caster.steam, leftPos + 82, topPos + 89, 16, 24);
	}
}
