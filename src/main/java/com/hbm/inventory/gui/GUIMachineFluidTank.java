package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineFluidTank;
import com.hbm.lib.RefStrings;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.storage.TileEntityMachineFluidTank;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachineFluidTank extends GuiInfoContainer<ContainerMachineFluidTank> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/storage/gui_tank.png");
	private final TileEntityMachineFluidTank tank;

	public GUIMachineFluidTank(ContainerMachineFluidTank menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		tank = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 166;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.renderTankInfo(graphics, tank.tank, mouseX, mouseY, leftPos + 71, topPos + 69 - 52, 34, 52);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {

		// mode button, the original's AuxButtonPacket
		if(leftPos + 151 <= x && leftPos + 151 + 18 > x && topPos + 35 < y && topPos + 35 + 18 >= y) {
			this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			CompoundTag data = new CompoundTag();
			data.putBoolean("mode", true);
			NBTControlPacket.send(data, tank.getBlockPos());
			return true;
		}

		return super.mouseClicked(x, y, button);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, this.imageWidth / 2 - font.width(name) / 2, 6, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		int i = tank.mode;
		drawTexturedModalRect(graphics, texture, leftPos + 151, topPos + 34, 176, i * 18, 18, 18);

		this.renderTank(graphics, tank.tank, leftPos + 71, topPos + 69, 34, 52);
	}
}
