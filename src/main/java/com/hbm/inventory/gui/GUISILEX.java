package com.hbm.inventory.gui;

import java.awt.Color;

import com.hbm.inventory.container.ContainerSILEX;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.SILEXRecipes;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.lib.RefStrings;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntitySILEX;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class GUISILEX extends GuiInfoContainer<ContainerSILEX> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_silex.png");
	private final TileEntitySILEX silex;

	public GUISILEX(ContainerSILEX menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.silex = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 222;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.renderTankInfo(graphics, silex.tank, mouseX, mouseY, leftPos + 8, topPos + 42, 52, 7);

		if(!silex.current.isEmpty()) {
			this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 27, topPos + 72, 16, 52, mouseX, mouseY, silex.currentFill + "/" + TileEntitySILEX.maxFill + "mB", silex.current.getHoverName().getString());
		}

		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 10, topPos + 92, 10, 10, mouseX, mouseY, "Void contents");
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if(leftPos + 10 <= x && leftPos + 10 + 12 > x && topPos + 92 < y && topPos + 92 + 12 >= y) {
			this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			CompoundTag data = new CompoundTag();
			data.putBoolean("void", true);
			NBTControlPacket.send(data, silex.getBlockPos());
			return true;
		}
		return super.mouseClicked(x, y, button);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, (imageWidth / 2 - font.width(name) / 2) - 54, 8, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);

		if(silex.mode != EnumWavelengths.NULL) {
			String mode = I18nUtil.resolveKey(silex.mode.name);
			graphics.drawString(font, silex.mode.textColor + mode, 100 + (32 - font.width(mode) / 2), 16, 0, false);
		}
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		if(silex.mode != EnumWavelengths.NULL) {
			float freq = 0.1F * (float) Math.pow(2, silex.mode.ordinal());
			int color = (silex.mode != EnumWavelengths.VISIBLE) ? silex.mode.guiColor : Color.HSBtoRGB(silex.getLevel().getGameTime() / 50.0F, 0.5F, 1F) & 16777215;
			drawWave(graphics, 81, 46, 16, 84, 0.5F, freq, color, 2);
		}

		if(silex.tank.getFill() > 0) {
			if(silex.tank.getTankType() == Fluids.PEROXIDE || TileEntitySILEX.isConvertedFluid(silex.tank.getTankType()) || SILEXRecipes.getOutput(ItemFluidIcon.make(silex.tank.getTankType(), 1)) != null) {
				drawTexturedModalRect(graphics, texture, leftPos + 7, topPos + 41, 176, 118, 54, 9);
			} else {
				drawTexturedModalRect(graphics, texture, leftPos + 7, topPos + 41, 176, 109, 54, 9);
			}
		}

		int p = silex.getProgressScaled(69);
		drawTexturedModalRect(graphics, texture, leftPos + 45, topPos + 82, 176, 0, p, 43);

		int f = silex.getFillScaled(52);
		drawTexturedModalRect(graphics, texture, leftPos + 26, topPos + 124 - f, 176, 109 - f, 16, f);

		int i = silex.getFluidScaled(52);
		drawTexturedModalRect(graphics, texture, leftPos + 8, topPos + 42, 176, silex.tank.getTankType() == Fluids.PEROXIDE ? 43 : 50, i, 7);
	}

	/** The scrolling sine wave of the laser's wavelength, drawn as small dots instead of the original's GL lines */
	private void drawWave(GuiGraphics graphics, int x, int y, int height, int width, float resolution, float freq, int color, int thickness) {
		float samples = width / resolution;
		float scale = height / 2F;
		float offset = (float) (silex.getLevel().getGameTime() % (4 * Math.PI / freq));

		for(int i = 1; i < samples; i++) {
			double currentX = offset + x + i * resolution;
			double currentY = y + scale * Math.sin(freq * currentX);
			int px = (int) (leftPos + currentX - offset);
			int py = (int) (topPos + currentY);
			graphics.fill(px, py - thickness / 2, px + 1, py + thickness - thickness / 2, 0xFF000000 | color);
		}
	}
}
