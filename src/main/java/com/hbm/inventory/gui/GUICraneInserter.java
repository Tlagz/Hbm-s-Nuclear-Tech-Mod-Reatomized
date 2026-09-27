package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerCraneInserter;
import com.hbm.lib.RefStrings;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.network.TileEntityCraneInserter;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class GUICraneInserter extends GuiInfoContainer<ContainerCraneInserter> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/storage/gui_crane_inserter.png");
	private final TileEntityCraneInserter inserter;

	public GUICraneInserter(ContainerCraneInserter menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.inserter = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 185;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int x, int y) {
		this.drawCustomInfoStat(graphics, x, y, leftPos + 151, topPos + 34, 18, 18, x, y, "Destroy overflow: " + (inserter.destroyer ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF"));
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if(leftPos + 151 <= x && leftPos + 151 + 18 > x && topPos + 34 < y && topPos + 34 + 18 >= y) {
			this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			CompoundTag data = new CompoundTag();
			data.putBoolean("destroyer", true);
			NBTControlPacket.send(data, inserter.getBlockPos());
			return true;
		}
		return super.mouseClicked(x, y, button);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2 - 18, 5, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		if(inserter.destroyer)
			drawTexturedModalRect(graphics, texture, leftPos + 151, topPos + 34, 176, 0, 18, 18);
	}
}
