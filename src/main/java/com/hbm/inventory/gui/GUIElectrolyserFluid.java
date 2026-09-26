package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerElectrolyserFluid;
import com.hbm.lib.RefStrings;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityElectrolyser;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class GUIElectrolyserFluid extends GuiInfoContainer<ContainerElectrolyserFluid> {

	public static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_electrolyser_fluid.png");
	private final TileEntityElectrolyser electrolyser;

	public GUIElectrolyserFluid(ContainerElectrolyserFluid menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.electrolyser = menu.tile;

		this.imageWidth = 210;
		this.imageHeight = 204;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.renderTankInfo(graphics, electrolyser.tanks[0], mouseX, mouseY, leftPos + 42, topPos + 18, 16, 52);
		this.renderTankInfo(graphics, electrolyser.tanks[1], mouseX, mouseY, leftPos + 96, topPos + 18, 16, 52);
		this.renderTankInfo(graphics, electrolyser.tanks[2], mouseX, mouseY, leftPos + 116, topPos + 18, 16, 52);
		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 186, topPos + 18, 16, 89, electrolyser.power, TileEntityElectrolyser.maxPower);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {

		// switch to the metal mode
		if(leftPos + 8 <= x && leftPos + 8 + 54 > x && topPos + 82 < y && topPos + 82 + 12 >= y) {
			this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			CompoundTag data = new CompoundTag();
			data.putBoolean("sgm", true);
			NBTControlPacket.send(data, electrolyser.getBlockPos());
			return true;
		}

		return super.mouseClicked(x, y, button);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, (imageWidth / 2 - font.width(name) / 2) - 16, 7, 0xffffff, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		int p = (int) (electrolyser.power * 89 / TileEntityElectrolyser.maxPower);
		drawTexturedModalRect(graphics, texture, leftPos + 186, topPos + 107 - p, 210, 89 - p, 16, p);

		if(electrolyser.power >= electrolyser.usageFluid)
			drawTexturedModalRect(graphics, texture, leftPos + 190, topPos + 4, 226, 40, 9, 12);

		int e = electrolyser.processFluidTime <= 0 ? 0 : electrolyser.progressFluid * 41 / electrolyser.processFluidTime;
		drawTexturedModalRect(graphics, texture, leftPos + 62, topPos + 26, 226, 0, 12, e);

		this.renderTank(graphics, electrolyser.tanks[0], leftPos + 42, topPos + 70, 16, 52);
		this.renderTank(graphics, electrolyser.tanks[1], leftPos + 96, topPos + 70, 16, 52);
		this.renderTank(graphics, electrolyser.tanks[2], leftPos + 116, topPos + 70, 16, 52);
	}
}
