package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerCraneExtractor;
import com.hbm.lib.RefStrings;
import com.hbm.module.ModulePatternMatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.network.TileEntityCraneExtractor;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class GUICraneExtractor extends GuiInfoContainer<ContainerCraneExtractor> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/storage/gui_crane_ejector.png");
	private final TileEntityCraneExtractor ejector;

	public GUICraneExtractor(ContainerCraneExtractor menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.ejector = menu.tile;

		this.imageWidth = 212;
		this.imageHeight = 185;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int x, int y) {

		if(this.menu.getCarried().isEmpty()) {
			for(int i = 0; i < 9; ++i) {
				Slot slot = this.menu.slots.get(i);

				if(this.isHovering(slot.x, slot.y, 16, 16, x, y) && ejector.matcher.modes[i] != null) {
					this.drawCustomInfoStat(graphics, x, y, x - 1, y - 1, 2, 2, x, y - 30, ChatFormatting.RED + "Right click to change", ModulePatternMatcher.getLabel(ejector.matcher.modes[i]));
				}
			}
		}

		this.drawCustomInfoStat(graphics, x, y, leftPos + 187, topPos + 34, 18, 18, x, y, "Only take maximum possible: " + (ejector.maxEject ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF"));
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {

		if(leftPos + 187 <= x && leftPos + 187 + 18 > x && topPos + 34 < y && topPos + 34 + 18 >= y) {
			click("maxEject");
			return true;
		}

		if(leftPos + 128 <= x && leftPos + 128 + 14 > x && topPos + 30 < y && topPos + 30 + 26 >= y) {
			click("whitelist");
			return true;
		}

		return super.mouseClicked(x, y, button);
	}

	private void click(String key) {
		this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		CompoundTag data = new CompoundTag();
		data.putBoolean(key, true);
		NBTControlPacket.send(data, ejector.getBlockPos());
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2, 6, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 26, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		if(ejector.maxEject) drawTexturedModalRect(graphics, texture, leftPos + 187, topPos + 34, 212, 0, 18, 18);

		if(ejector.isWhitelist) {
			drawTexturedModalRect(graphics, texture, leftPos + 139, topPos + 33, 212, 18, 3, 6);
		} else {
			drawTexturedModalRect(graphics, texture, leftPos + 139, topPos + 47, 212, 18, 3, 6);
		}
	}
}
