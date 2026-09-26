package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerLiquefactor;
import com.hbm.inventory.container.ContainerSolidifier;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.oil.TileEntityMachineLiquefactor;
import com.hbm.tileentity.machine.oil.TileEntityMachineSolidifier;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/** The original's GUILiquefactor and GUISolidifier, identical except for the texture and the tank position */
public class GUILiquefactor<C extends AbstractContainerMenu> extends GuiInfoContainer<C> {

	private final ResourceLocation texture;
	private final int tankX;
	private final FluidTank tank;
	private final PowerInfo machine;

	private interface PowerInfo {
		long power();
		long maxPower();
		int progress();
		int processTime();
	}

	private GUILiquefactor(C menu, Inventory invPlayer, Component title, ResourceLocation texture, int tankX, FluidTank tank, PowerInfo machine) {
		super(menu, invPlayer, title);
		this.texture = texture;
		this.tankX = tankX;
		this.tank = tank;
		this.machine = machine;

		this.imageWidth = 176;
		this.imageHeight = 204;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	public static GUILiquefactor<ContainerLiquefactor> liquefactor(ContainerLiquefactor menu, Inventory invPlayer, Component title) {
		TileEntityMachineLiquefactor tile = menu.tile;
		return new GUILiquefactor<>(menu, invPlayer, title, RefStrings.loc("textures/gui/processing/gui_liquefactor.png"), 71, tile.tank, new PowerInfo() {
			@Override public long power() { return tile.power; }
			@Override public long maxPower() { return TileEntityMachineLiquefactor.maxPower; }
			@Override public int progress() { return tile.progress; }
			@Override public int processTime() { return tile.processTime; }
		});
	}

	public static GUILiquefactor<ContainerSolidifier> solidifier(ContainerSolidifier menu, Inventory invPlayer, Component title) {
		TileEntityMachineSolidifier tile = menu.tile;
		return new GUILiquefactor<>(menu, invPlayer, title, RefStrings.loc("textures/gui/processing/gui_solidifier.png"), 35, tile.tank, new PowerInfo() {
			@Override public long power() { return tile.power; }
			@Override public long maxPower() { return TileEntityMachineSolidifier.maxPower; }
			@Override public int progress() { return tile.progress; }
			@Override public int processTime() { return tile.processTime; }
		});
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		this.renderTankInfo(graphics, tank, mouseX, mouseY, leftPos + tankX, topPos + 36, 16, 52);
		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 134, topPos + 18, 16, 52, machine.power(), machine.maxPower());
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, 70 - font.width(name) / 2, 6, 0xC7C1A3, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		int i = (int) (machine.power() * 52 / machine.maxPower());
		drawTexturedModalRect(graphics, texture, leftPos + 134, topPos + 70 - i, 176, 52 - i, 16, i);

		int j = machine.processTime() <= 0 ? 0 : machine.progress() * 42 / machine.processTime();
		drawTexturedModalRect(graphics, texture, leftPos + 42, topPos + 17, 192, 0, j, 35);

		if(i > 0)
			drawTexturedModalRect(graphics, texture, leftPos + 138, topPos + 4, 176, 52, 9, 12);

		this.renderTank(graphics, tank, leftPos + tankX, topPos + 88, 16, 52);
	}
}
