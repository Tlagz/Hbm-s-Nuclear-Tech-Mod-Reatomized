package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineGasFlare;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.lib.RefStrings;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.oil.TileEntityMachineGasFlare;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

/** Flare stack: the valve and the ignition dial, the fuel tank and the power bar */
public class GUIMachineGasFlare extends GuiInfoContainer<ContainerMachineGasFlare> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/generators/gui_flare_stack.png");
	private final TileEntityMachineGasFlare flare;

	public GUIMachineGasFlare(ContainerMachineGasFlare menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		flare = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 203;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 79, topPos + 16, 35, 10, mouseX, mouseY, I18nUtil.resolveKeyArray("flare.valve"));
		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 79, topPos + 50, 35, 14, mouseX, mouseY, I18nUtil.resolveKeyArray("flare.ignition"));
		this.renderTankInfo(graphics, flare.tank, mouseX, mouseY, leftPos + 35, topPos + 69 - 52, 16, 52);
		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 143, topPos + 69 - 52, 16, 52, flare.power, TileEntityMachineGasFlare.maxPower);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {

		String key = null;
		if(leftPos + 89 <= x && leftPos + 89 + 16 > x && topPos + 16 < y && topPos + 16 + 10 >= y) key = "valve";
		else if(leftPos + 89 <= x && leftPos + 89 + 16 > x && topPos + 50 < y && topPos + 50 + 14 >= y) key = "dial";

		if(key != null) {
			this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			CompoundTag data = new CompoundTag();
			data.putBoolean(key, true);
			NBTControlPacket.send(data, flare.getBlockPos());
			return true;
		}

		return super.mouseClicked(x, y, button);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		int j = (int) flare.getPowerScaled(52);
		drawTexturedModalRect(graphics, texture, leftPos + 143, topPos + 69 - j, 176, 94 - j, 16, j);

		if(flare.isOn) drawTexturedModalRect(graphics, texture, leftPos + 79, topPos + 15, 176, 0, 35, 10);
		if(flare.doesBurn) drawTexturedModalRect(graphics, texture, leftPos + 79, topPos + 49, 176, 10, 35, 14);

		if(flare.isOn && flare.doesBurn && flare.tank.getFill() > 0 && flare.tank.getTankType().hasTrait(FT_Flammable.class))
			drawTexturedModalRect(graphics, texture, leftPos + 88, topPos + 29, 176, 24, 18, 18);

		this.renderTank(graphics, flare.tank, leftPos + 35, topPos + 69, 16, 52);
	}
}
