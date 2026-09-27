package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerCraneGrabber;
import com.hbm.lib.RefStrings;
import com.hbm.module.ModulePatternMatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.network.TileEntityCraneGrabber;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class GUICraneGrabber extends GuiInfoContainer<ContainerCraneGrabber> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/storage/gui_crane_grabber.png");
	private final TileEntityCraneGrabber grabber;

	public GUICraneGrabber(ContainerCraneGrabber menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.grabber = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 185;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int x, int y) {

		if(this.menu.getCarried().isEmpty()) {
			for(int i = 0; i < 9; ++i) {
				Slot slot = this.menu.slots.get(i);

				if(this.isHovering(slot.x, slot.y, 16, 16, x, y) && grabber.matcher.modes[i] != null) {
					this.drawCustomInfoStat(graphics, x, y, x - 1, y - 1, 2, 2, x, y - 30, ChatFormatting.RED + "Right click to change", ModulePatternMatcher.getLabel(grabber.matcher.modes[i]));
				}
			}
		}
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {

		if(leftPos + 97 <= x && leftPos + 97 + 14 > x && topPos + 30 < y && topPos + 30 + 26 >= y) {
			this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			CompoundTag data = new CompoundTag();
			data.putBoolean("whitelist", true);
			NBTControlPacket.send(data, grabber.getBlockPos());
			return true;
		}

		return super.mouseClicked(x, y, button);
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

		if(grabber.isWhitelist) {
			drawTexturedModalRect(graphics, texture, leftPos + 108, topPos + 33, 176, 0, 3, 6);
		} else {
			drawTexturedModalRect(graphics, texture, leftPos + 108, topPos + 47, 176, 0, 3, 6);
		}
	}
}
