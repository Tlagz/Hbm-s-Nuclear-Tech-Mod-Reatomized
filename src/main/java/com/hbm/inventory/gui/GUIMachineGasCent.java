package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineGasCent;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineGasCent;
import com.hbm.tileentity.machine.TileEntityMachineGasCent.PseudoFluidTank;
import com.hbm.util.i18n.I18nUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachineGasCent extends GuiInfoContainer<ContainerMachineGasCent> {

	public static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_centrifuge_gas.png");
	private final TileEntityMachineGasCent gasCent;

	public GUIMachineGasCent(ContainerMachineGasCent menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.gasCent = menu.tile;

		this.imageWidth = 206;
		this.imageHeight = 204;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {

		String[] inTankInfo = new String[] {gasCent.inputTank.getTankType().getName(), gasCent.inputTank.getFill() + " / " + gasCent.inputTank.getMaxFill() + " mB"};
		if(gasCent.inputTank.getTankType().getIfHighSpeed()) {
			if(gasCent.getProcessingSpeed() > TileEntityMachineGasCent.processingSpeed - 70)
				inTankInfo[0] = ChatFormatting.DARK_RED + inTankInfo[0];
			else
				inTankInfo[0] = ChatFormatting.GOLD + inTankInfo[0];
		}

		String[] outTankInfo = new String[] {gasCent.outputTank.getTankType().getName(), gasCent.outputTank.getFill() + " / " + gasCent.outputTank.getMaxFill() + " mB"};
		if(gasCent.outputTank.getTankType().getIfHighSpeed())
			outTankInfo[0] = ChatFormatting.GOLD + outTankInfo[0];

		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 15, topPos + 15, 24, 55, mouseX, mouseY, inTankInfo);
		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 137, topPos + 15, 25, 55, mouseX, mouseY, outTankInfo);

		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 182, topPos + 69 - 52, 16, 52, gasCent.power, TileEntityMachineGasCent.maxPower);

		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos - 12, topPos + 16, 16, 16, leftPos - 8, topPos + 16 + 16, I18nUtil.resolveKeyArray("desc.gui.gasCent.enrichment"));
		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos - 12, topPos + 32, 16, 16, leftPos - 8, topPos + 32 + 16, I18nUtil.resolveKeyArray("desc.gui.gasCent.output"));
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		int i = (int) gasCent.getPowerRemainingScaled(52);
		drawTexturedModalRect(graphics, texture, leftPos + 182, topPos + 69 - i, 206, 52 - i, 16, i);

		int j = gasCent.getCentrifugeProgressScaled(36);
		drawTexturedModalRect(graphics, texture, leftPos + 70, topPos + 35, 206, 52, j, 13);

		renderPseudoTank(graphics, gasCent.inputTank, leftPos + 16, topPos + 16);
		renderPseudoTank(graphics, gasCent.inputTank, leftPos + 32, topPos + 16);
		renderPseudoTank(graphics, gasCent.outputTank, leftPos + 138, topPos + 16);
		renderPseudoTank(graphics, gasCent.outputTank, leftPos + 154, topPos + 16);

		this.drawInfoPanel(graphics, leftPos - 12, topPos + 16, 16, 16, 3);
		this.drawInfoPanel(graphics, leftPos - 12, topPos + 32, 16, 16, 2);
	}

	/** A 6 wide, 52 high column of the real fluid's texture, filled by the pseudo fluid's amount */
	private void renderPseudoTank(GuiGraphics graphics, PseudoFluidTank tank, int x, int y) {
		ResourceLocation fluidTex = gasCent.tank.getTankType().getTexture();
		if(fluidTex == null || tank.getMaxFill() <= 0) return;

		int height = 52;
		int i = tank.getFill() * height / tank.getMaxFill();
		if(i <= 0) return;

		RenderSystem.enableBlend();
		graphics.blit(fluidTex, x, y + height - i, 0, 0, 6, i, 16, 16);
		RenderSystem.disableBlend();
	}
}
