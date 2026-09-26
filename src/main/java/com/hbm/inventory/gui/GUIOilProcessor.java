package com.hbm.inventory.gui;

import java.util.List;

import com.hbm.inventory.container.ContainerOilProcessor;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.oil.TileEntityOilProcessorBase;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * The original's GUIMachineVacuumDistill, GUIMachineCatalyticReformer and GUIMachineHydrotreater: a power bar and
 * a row of 16x52 tanks, the reformer and hydrotreater show which item goes into the empty catalyst slot.
 */
public class GUIOilProcessor extends GuiInfoContainer<ContainerOilProcessor> {

	private final ResourceLocation texture;
	private final TileEntityOilProcessorBase machine;
	private final int powerX;
	private final int[] tankX;
	private final int converterSlot;

	public GUIOilProcessor(ContainerOilProcessor menu, Inventory invPlayer, Component title, String texture, int powerX, int[] tankX, int converterSlot) {
		super(menu, invPlayer, title);
		this.machine = menu.tile;
		this.texture = RefStrings.loc("textures/gui/processing/" + texture + ".png");
		this.powerX = powerX;
		this.tankX = tankX;
		this.converterSlot = converterSlot;

		this.imageWidth = 176;
		this.imageHeight = 238;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	public static GUIOilProcessor vacuumDistill(ContainerOilProcessor menu, Inventory inv, Component title) {
		return new GUIOilProcessor(menu, inv, title, "gui_vacuum_distill", 26, new int[] {44, 80, 98, 116, 134}, -1);
	}

	public static GUIOilProcessor catalyticReformer(ContainerOilProcessor menu, Inventory inv, Component title) {
		return new GUIOilProcessor(menu, inv, title, "gui_catalytic_reformer", 17, new int[] {35, 107, 125, 143}, 10);
	}

	public static GUIOilProcessor hydrotreater(ContainerOilProcessor menu, Inventory inv, Component title) {
		return new GUIOilProcessor(menu, inv, title, "gui_hydrotreater", 17, new int[] {35, 53, 125, 143}, 10);
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		for(int i = 0; i < tankX.length; i++) this.renderTankInfo(graphics, machine.tanks[i], mouseX, mouseY, leftPos + tankX[i], topPos + 70 - 52, 16, 52);
		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + powerX, topPos + 70 - 52, 16, 52, machine.power, TileEntityOilProcessorBase.maxPower);

		if(converterSlot >= 0 && this.menu.getCarried().isEmpty()) {
			Slot slot = this.menu.slots.get(converterSlot);
			if(this.isHovering(slot, mouseX, mouseY) && !slot.hasItem()) {
				graphics.renderComponentTooltip(font, List.of(ModItems.catalytic_converter.get().getDescription()), mouseX, mouseY);
			}
		}
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, this.imageWidth / 2 - font.width(name) / 2, 5, 0xffffff, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		int j = (int) (machine.power * 54 / TileEntityOilProcessorBase.maxPower);
		drawTexturedModalRect(graphics, texture, leftPos + powerX, topPos + 70 - j, 176, 52 - j, 16, j);

		for(int i = 0; i < tankX.length; i++) this.renderTank(graphics, machine.tanks[i], leftPos + tankX[i], topPos + 70, 16, 52);
	}
}
