package com.hbm.inventory.gui;

import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

import com.hbm.inventory.container.ContainerHeaterHeatex;
import com.hbm.lib.RefStrings;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityHeaterHeatex;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Heat exchanger: hot and cold tanks, text fields for the amount per cycle and the cycle delay */
public class GUIHeaterHeatex extends GuiInfoContainer<ContainerHeaterHeatex> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/machine/gui_heatex.png");
	private final TileEntityHeaterHeatex heater;
	private EditBox fieldCycles;
	private EditBox fieldDelay;

	public GUIHeaterHeatex(ContainerHeaterHeatex menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		heater = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 204;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void init() {
		super.init();

		this.fieldCycles = new EditBox(this.font, leftPos + 73, topPos + 31, 30, 10, Component.empty());
		initText(this.fieldCycles);
		this.fieldCycles.setValue(String.valueOf(heater.amountToCool));
		this.fieldCycles.setResponder(text -> send("toCool", text));

		this.fieldDelay = new EditBox(this.font, leftPos + 73, topPos + 49, 30, 10, Component.empty());
		initText(this.fieldDelay);
		this.fieldDelay.setValue(String.valueOf(heater.tickDelay));
		this.fieldDelay.setResponder(text -> send("delay", text));
	}

	protected void initText(EditBox field) {
		field.setTextColor(0x00ff00);
		field.setTextColorUneditable(0x00ff00);
		field.setBordered(false);
		field.setMaxLength(5);
		this.addRenderableWidget(field);
	}

	private void send(String key, String text) {
		CompoundTag data = new CompoundTag();
		data.putInt(key, Math.max(NumberUtils.toInt(text), 1));
		NBTControlPacket.send(data, heater.getBlockPos());
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int x, int y) {
		this.renderTankInfo(graphics, heater.tanks[0], x, y, leftPos + 44, topPos + 36, 16, 52);
		this.renderTankInfo(graphics, heater.tanks[1], x, y, leftPos + 116, topPos + 36, 16, 52);

		if(leftPos + 70 <= x && leftPos + 70 + 36 > x && topPos + 26 < y && topPos + 26 + 18 >= y) {
			graphics.renderComponentTooltip(font, List.of(Component.literal("Amount per cycle")), x, y);
		}

		if(leftPos + 70 <= x && leftPos + 70 + 36 > x && topPos + 44 < y && topPos + 44 + 18 >= y) {
			graphics.renderComponentTooltip(font, List.of(Component.literal("Cycle tick delay")), x, y);
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		// typing into a field must not close the GUI (inventory key)
		for(EditBox field : new EditBox[] {fieldCycles, fieldDelay}) {
			if(field.isFocused() && keyCode != 256) {
				return field.keyPressed(keyCode, scanCode, modifiers) || field.canConsumeInput();
			}
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
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

		this.renderTank(graphics, heater.tanks[0], leftPos + 44, topPos + 88, 16, 52);
		this.renderTank(graphics, heater.tanks[1], leftPos + 116, topPos + 88, 16, 52);
	}
}
