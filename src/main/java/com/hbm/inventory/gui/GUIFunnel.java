package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerFunnel;
import com.hbm.lib.RefStrings;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityMachineFunnel;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class GUIFunnel extends GuiInfoContainer<ContainerFunnel> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_funnel.png");
	private final TileEntityMachineFunnel funnel;

	public GUIFunnel(ContainerFunnel menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.funnel = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 168;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		String mode = funnel.mode == TileEntityMachineFunnel.MODE_3x3 ? "3x3 only" : funnel.mode == TileEntityMachineFunnel.MODE_2x2 ? "2x2 only" : "3x3 then 2x2";
		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 159, topPos + 73, 10, 10, mouseX, mouseY, "Mode: " + mode);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if(this.checkClick((int) x, (int) y, 159, 73, 10, 10)) {
			this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			CompoundTag data = new CompoundTag();
			data.putBoolean("toggle", true);
			NBTControlPacket.send(data, funnel.getBlockPos());
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
		drawTexturedModalRect(graphics, texture, leftPos + 159, topPos + 73, 176, funnel.mode * 10, 10, 10);
	}
}
