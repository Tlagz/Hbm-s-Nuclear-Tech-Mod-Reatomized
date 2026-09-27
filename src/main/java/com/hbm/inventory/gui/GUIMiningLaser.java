package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMiningLaser;
import com.hbm.lib.RefStrings;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityMachineMiningLaser;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class GUIMiningLaser extends GuiInfoContainer<ContainerMiningLaser> {

	public static final ResourceLocation texture = RefStrings.loc("textures/gui/machine/gui_laser_miner.png");
	private final TileEntityMachineMiningLaser laser;

	public GUIMiningLaser(ContainerMiningLaser menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.laser = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 222;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 8, topPos + 106 - 88, 16, 88, laser.power, TileEntityMachineMiningLaser.maxPower);

		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 87, topPos + 31, 8, 8, leftPos + 141, topPos + 39 + 16,
				"Acceptable upgrades:",
				" -Speed (stacks to level 12)",
				" -Effectiveness (stacks to level 12)",
				" -Overdrive (stacks to level 3)",
				" -Fortune (stacks to level 3)",
				" -Smelter (exclusive)",
				" -Shredder (exclusive)",
				" -Centrifuge (exclusive)",
				" -Crystallizer (exclusive)",
				" -Nullifier");

		this.renderTankInfo(graphics, laser.tank, mouseX, mouseY, leftPos + 35, topPos + 124 - 52, 7, 52);
	}

	/** The on/off button */
	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if(leftPos + 61 <= x && leftPos + 61 + 18 > x && topPos + 17 < y && topPos + 17 + 18 >= y) {
			this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			CompoundTag data = new CompoundTag();
			data.putBoolean("toggle", true);
			NBTControlPacket.send(data, laser.getBlockPos());
			return true;
		}
		return super.mouseClicked(x, y, button);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2, 4, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);

		String width = "" + laser.getWidth();
		graphics.drawString(font, width, 43 - font.width(width) / 2, 26, 0xffffff, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		if(laser.isOn)
			drawTexturedModalRect(graphics, texture, leftPos + 61, topPos + 17, 200, 0, 18, 18);

		int i = laser.getPowerScaled(88);
		drawTexturedModalRect(graphics, texture, leftPos + 8, topPos + 106 - i, 176, 88 - i, 16, i);

		int j = laser.getProgressScaled(34);
		drawTexturedModalRect(graphics, texture, leftPos + 66, topPos + 36, 192, 0, 8, j);

		this.drawInfoPanel(graphics, leftPos + 87, topPos + 31, 8, 8, 8);

		this.renderTank(graphics, laser.tank, leftPos + 35, topPos + 124, 7, 52);
	}
}
