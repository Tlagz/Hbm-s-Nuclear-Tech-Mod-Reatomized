package com.hbm.inventory.gui;

import java.util.List;

import com.hbm.inventory.container.ContainerMachineWoodBurner;
import com.hbm.lib.RefStrings;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityMachineWoodBurner;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class GUIMachineWoodBurner extends GuiInfoContainer<ContainerMachineWoodBurner> {

	private final TileEntityMachineWoodBurner burner;
	private static final ResourceLocation texture = RefStrings.loc("textures/gui/generators/gui_wood_burner_alt.png");

	public GUIMachineWoodBurner(ContainerMachineWoodBurner menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		burner = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 186;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 143, topPos + 18, 16, 34, burner.power, TileEntityMachineWoodBurner.maxPower);

		if(this.menu.getCarried().isEmpty()) {
			Slot slot = this.menu.slots.get(0);
			if(this.isHovering(slot, mouseX, mouseY) && !slot.hasItem()) {
				List<String> bonuses = TileEntityMachineWoodBurner.burnModule.getDesc();
				if(!bonuses.isEmpty()) {
					graphics.renderComponentTooltip(font, bonuses.stream().map(s -> (Component) Component.literal(s)).toList(), mouseX, mouseY);
				}
			}
		}

		if(burner.liquidBurn) this.renderTankInfo(graphics, burner.tank, mouseX, mouseY, leftPos + 80, topPos + 18, 16, 52);

		if(!burner.liquidBurn && leftPos + 16 <= mouseX && leftPos + 16 + 8 > mouseX && topPos + 17 < mouseY && topPos + 17 + 54 >= mouseY) {
			this.drawInfo(graphics, new String[] { (burner.burnTime / 20) + "s" }, mouseX, mouseY);
		}

		if(leftPos + 53 <= mouseX && leftPos + 53 + 16 > mouseX && topPos + 17 < mouseY && topPos + 17 + 15 >= mouseY) {
			this.drawInfo(graphics, new String[] { burner.isOn ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF" }, mouseX, mouseY);
		}
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {

		if(leftPos + 53 <= x && leftPos + 53 + 16 > x && topPos + 17 < y && topPos + 17 + 15 >= y) {
			click();
			CompoundTag data = new CompoundTag();
			data.putBoolean("toggle", false);
			NBTControlPacket.send(data, burner.getBlockPos());
			return true;
		}

		if(leftPos + 46 <= x && leftPos + 46 + 30 > x && topPos + 37 < y && topPos + 37 + 14 >= y) {
			click();
			CompoundTag data = new CompoundTag();
			data.putBoolean("switch", false);
			NBTControlPacket.send(data, burner.getBlockPos());
			return true;
		}

		return super.mouseClicked(x, y, button);
	}

	private void click() {
		this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, 70 - font.width(name) / 2, 6, 0xffffff, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		if(burner.liquidBurn) {
			drawTexturedModalRect(graphics, texture, leftPos + 16, topPos + 17, 176, 52, 60, 54);
			drawTexturedModalRect(graphics, texture, leftPos + 79, topPos + 17, 176, 106, 36, 54);
		}

		if(burner.isOn) {
			drawTexturedModalRect(graphics, texture, leftPos + 53, topPos + 17, 196, 0, 16, 15);
		}

		int p = (int) (burner.power * 34 / TileEntityMachineWoodBurner.maxPower);
		drawTexturedModalRect(graphics, texture, leftPos + 143, topPos + 52 - p, 176, 52 - p, 16, p);

		if(burner.maxBurnTime > 0 && !burner.liquidBurn) {
			int b = burner.burnTime * 52 / burner.maxBurnTime;
			drawTexturedModalRect(graphics, texture, leftPos + 17, topPos + 70 - b, 192, 52 - b, 4, b);
		}

		if(burner.liquidBurn) this.renderTank(graphics, burner.tank, leftPos + 80, topPos + 70, 16, 52);
	}
}
