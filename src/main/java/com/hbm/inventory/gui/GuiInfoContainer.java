package com.hbm.inventory.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.lib.RefStrings;
import com.hbm.util.BobMathUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.world.inventory.Slot;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Base of NTM machine screens, with the original's helpers for power bars, info tooltips and info panels.
 * The GL based drawing of the original goes through GuiGraphics, textures are 256x256 sheets like before.
 *
 * TODO fluid tank rendering (FluidTank.renderTank), NEI/JEI integration
 */
public abstract class GuiInfoContainer<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

	public static final ResourceLocation guiUtil = RefStrings.loc("textures/gui/gui_utility.png");

	public GuiInfoContainer(T menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		this.drawTooltips(graphics, mouseX, mouseY);
		this.renderTooltip(graphics, mouseX, mouseY);
	}

	/** Machine specific hover info (power bars etc.), the original did this in drawScreen */
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) { }

	/** The original's drawTexturedModalRect for a 256x256 GUI sheet */
	public void drawTexturedModalRect(GuiGraphics graphics, ResourceLocation texture, int x, int y, int u, int v, int width, int height) {
		graphics.blit(texture, x, y, u, v, width, height);
	}

	public void drawElectricityInfo(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, int width, int height, long power, long maxPower) {
		if(x <= mouseX && x + width > mouseX && y < mouseY && y + height >= mouseY)
			drawInfo(graphics, new String[] { BobMathUtil.getShortNumber(power) + "/" + BobMathUtil.getShortNumber(maxPower) + "HE" }, mouseX, mouseY);
	}

	public void drawCustomInfoStat(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, int width, int height, int tPosX, int tPosY, String... text) {
		drawCustomInfoStat(graphics, mouseX, mouseY, x, y, width, height, tPosX, tPosY, Arrays.asList(text));
	}

	public void drawCustomInfoStat(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, int width, int height, int tPosX, int tPosY, List<String> text) {
		if(x <= mouseX && x + width > mouseX && y < mouseY && y + height >= mouseY)
			graphics.renderComponentTooltip(font, text.stream().map(s -> (Component) Component.literal(s)).toList(), tPosX, tPosY);
	}

	public void drawInfo(GuiGraphics graphics, String[] text, int x, int y) {
		graphics.renderComponentTooltip(font, Arrays.stream(text).map(s -> (Component) Component.literal(s)).toList(), x, y);
	}

	/** The small and large "i", "!" and "*" symbols */
	public void drawInfoPanel(GuiGraphics graphics, int x, int y, int width, int height, int type) {
		switch(type) {
		case 0: graphics.blit(guiUtil, x, y, 0, 0, 8, 8); break; //Small blue I
		case 1: graphics.blit(guiUtil, x, y, 0, 8, 8, 8); break; //Small green I
		case 2: graphics.blit(guiUtil, x, y, 8, 0, 16, 16); break; //Large blue I
		case 3: graphics.blit(guiUtil, x, y, 24, 0, 16, 16); break; //Large green I
		case 4: graphics.blit(guiUtil, x, y, 0, 16, 8, 8); break; //Small red !
		case 5: graphics.blit(guiUtil, x, y, 0, 24, 8, 8); break; //Small yellow !
		case 6: graphics.blit(guiUtil, x, y, 8, 16, 16, 16); break; //Large red !
		case 7: graphics.blit(guiUtil, x, y, 24, 16, 16, 16); break; //Large yellow !
		case 8: graphics.blit(guiUtil, x, y, 0, 32, 8, 8); break; //Small blue *
		case 9: graphics.blit(guiUtil, x, y, 0, 40, 8, 8); break; //Small grey *
		case 10: graphics.blit(guiUtil, x, y, 8, 32, 16, 16); break; //Large blue *
		case 11: graphics.blit(guiUtil, x, y, 24, 32, 16, 16); break; //Large grey *
		}
	}

	/**
	 * The original's FluidTank.renderTank: fills the tank area from the bottom with the fluid's 16x16 GUI texture,
	 * tinted with the fluid's GUI tint. y is the tank's bottom edge like in the original.
	 */
	public void renderTank(GuiGraphics graphics, FluidTank tank, int x, int y, int width, int height) {
		if(tank.getMaxFill() <= 0 || tank.getFill() <= 0) return;

		int filled = tank.getFill() * height / tank.getMaxFill();
		int color = tank.getTankType().getTint();
		graphics.setColor(((color >> 16) & 0xFF) / 255F, ((color >> 8) & 0xFF) / 255F, (color & 0xFF) / 255F, 1F);

		ResourceLocation texture = tank.getTankType().getTexture();
		int top = y - filled;
		for(int dy = 0; dy < filled; dy += 16) {
			int h = Math.min(16, filled - dy);
			for(int dx = 0; dx < width; dx += 16) {
				int w = Math.min(16, width - dx);
				graphics.blit(texture, x + dx, y - dy - h, 0, 16 - h, w, h, 16, 16);
			}
		}

		graphics.setColor(1F, 1F, 1F, 1F);
	}

	/** renderTank with the original's orientation 1: fills from the left edge to the right. y is the bottom edge. */
	public void renderTankHorizontal(GuiGraphics graphics, FluidTank tank, int x, int y, int width, int height) {
		if(tank.getMaxFill() <= 0 || tank.getFill() <= 0) return;

		int filled = tank.getFill() * width / tank.getMaxFill();
		int color = tank.getTankType().getTint();
		graphics.setColor(((color >> 16) & 0xFF) / 255F, ((color >> 8) & 0xFF) / 255F, (color & 0xFF) / 255F, 1F);

		ResourceLocation texture = tank.getTankType().getTexture();
		for(int dx = 0; dx < filled; dx += 16) {
			int w = Math.min(16, filled - dx);
			for(int dy = 0; dy < height; dy += 16) {
				int h = Math.min(16, height - dy);
				graphics.blit(texture, x + dx, y - height + dy, 0, 0, w, h, 16, 16);
			}
		}

		graphics.setColor(1F, 1F, 1F, 1F);
	}

	/** The original's FluidTank.renderTankInfo, the hover tooltip with type, fill and fluid traits */
	public void renderTankInfo(GuiGraphics graphics, FluidTank tank, int mouseX, int mouseY, int x, int y, int width, int height) {
		if(x <= mouseX && x + width > mouseX && y < mouseY && y + height >= mouseY) {

			List<String> list = new ArrayList<>();
			list.add(tank.getTankType().getLocalizedName());
			list.add(tank.getFill() + "/" + tank.getMaxFill() + "mB");

			if(tank.getPressure() != 0) {
				list.add(ChatFormatting.RED + "Pressure: " + tank.getPressure() + " PU");
				list.add((BobMathUtil.getBlink() ? ChatFormatting.RED : ChatFormatting.DARK_RED) + "Pressurized, use compressor!");
			}

			tank.getTankType().addInfo(list);
			graphics.renderComponentTooltip(font, list.stream().map(s -> (Component) Component.literal(s)).toList(), mouseX, mouseY);
		}
	}

	protected boolean isHovering(Slot slot, double mouseX, double mouseY) {
		return this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY);
	}

	protected boolean checkClick(int x, int y, int left, int top, int sizeX, int sizeY) {
		return leftPos + left <= x && leftPos + left + sizeX > x && topPos + top < y && topPos + top + sizeY >= y;
	}
}
