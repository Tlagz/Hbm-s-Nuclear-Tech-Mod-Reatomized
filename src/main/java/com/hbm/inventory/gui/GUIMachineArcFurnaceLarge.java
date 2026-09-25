package com.hbm.inventory.gui;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.container.ContainerMachineArcFurnaceLarge;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.lib.RefStrings;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityMachineArcFurnaceLarge;
import com.hbm.util.i18n.I18nUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachineArcFurnaceLarge extends GuiInfoContainer<ContainerMachineArcFurnaceLarge> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_arc_furnace.png");
	private final TileEntityMachineArcFurnaceLarge arc;

	public GUIMachineArcFurnaceLarge(ContainerMachineArcFurnaceLarge menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);
		this.arc = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 256;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int x, int y) {
		drawStackInfo(graphics, arc.liquids, x, y, 152, 36);
		this.drawElectricityInfo(graphics, x, y, leftPos + 8, topPos + 36, 7, 70, arc.getPower(), arc.getMaxPower());
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {

		// liquid mode toggle
		if(this.checkClick((int) x, (int) y, 151, 17, 18, 18)) {
			this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			CompoundTag data = new CompoundTag();
			data.putBoolean("liquid", true);
			NBTControlPacket.send(data, arc.getBlockPos());
			return true;
		}

		return super.mouseClicked(x, y, button);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, this.imageWidth / 2 - font.width(name) / 2, 6, 0xffffff, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float interp, int x, int y) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		if(arc.liquidMode) drawTexturedModalRect(graphics, texture, leftPos + 151, topPos + 17, 190, 18, 18, 18);
		if(arc.isProgressing) drawTexturedModalRect(graphics, texture, leftPos + 7, topPos + 17, 190, 0, 18, 18);

		int p = (int) (arc.power * 70 / TileEntityMachineArcFurnaceLarge.maxPower);
		drawTexturedModalRect(graphics, texture, leftPos + 8, topPos + 106 - p, 176, 70 - p, 7, p);

		int o = (int) (arc.progress * 70);
		drawTexturedModalRect(graphics, texture, leftPos + 17, topPos + 106 - o, 183, 70 - o, 7, o);

		drawStack(graphics, arc.liquids, TileEntityMachineArcFurnaceLarge.maxLiquid, 152, 106);
	}

	protected void drawStackInfo(GuiGraphics graphics, List<MaterialStack> stack, int mouseX, int mouseY, int x, int y) {
		List<String> list = new ArrayList<>();
		if(stack.isEmpty()) list.add(ChatFormatting.RED + "Empty");
		for(MaterialStack sta : stack) list.add(ChatFormatting.YELLOW + I18nUtil.resolveKey(sta.material.getUnlocalizedName()) + ": " + Mats.formatAmount(sta.amount, Screen.hasShiftDown()));
		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + x, topPos + y, 16, 70, mouseX, mouseY, list);
	}

	/** The molten materials stacked on top of each other in their colors, with a bright overlay like the original */
	protected void drawStack(GuiGraphics graphics, List<MaterialStack> stack, int capacity, int x, int y) {

		if(stack.isEmpty()) return;

		int lastHeight = 0;
		int lastQuant = 0;

		for(MaterialStack sta : stack) {

			int targetHeight = (lastQuant + sta.amount) * 70 / capacity;

			if(lastHeight == targetHeight) continue; //skip draw calls that would be 0 pixels high

			int hex = sta.material.moltenColor;
			RenderSystem.setShaderColor(((hex >> 16) & 0xFF) / 255F, ((hex >> 8) & 0xFF) / 255F, (hex & 0xFF) / 255F, 1F);
			drawTexturedModalRect(graphics, texture, leftPos + x, topPos + y - targetHeight, 208, 70 - targetHeight, 16, targetHeight - lastHeight);
			RenderSystem.enableBlend();
			RenderSystem.setShaderColor(1F, 1F, 1F, 0.3F);
			drawTexturedModalRect(graphics, texture, leftPos + x, topPos + y - targetHeight, 208, 70 - targetHeight, 16, targetHeight - lastHeight);
			RenderSystem.disableBlend();

			lastQuant += sta.amount;
			lastHeight = targetHeight;
		}

		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
	}
}
