package com.hbm.inventory.gui;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.container.ContainerMachineTurbineGas;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.inventory.fluid.trait.FT_Combustible.FuelGrade;
import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.lib.RefStrings;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityMachineTurbineGas;
import com.hbm.util.i18n.I18nUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class GUIMachineTurbineGas extends GuiInfoContainer<ContainerMachineTurbineGas> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/generators/gui_turbinegas.png");
	private final TileEntityMachineTurbineGas turbinegas;

	int yStart;
	int slidStart;

	public GUIMachineTurbineGas(ContainerMachineTurbineGas menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.turbinegas = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 223;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	private void click() {
		this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {

		int x = (int) mx;
		int y = (int) my;

		slidStart = turbinegas.powerSliderPos;
		yStart = y;

		if(Math.sqrt(Math.pow((x - leftPos - 88), 2) + Math.pow((y - topPos - 40), 2)) <= 8) { //start-stop circular button

			if(turbinegas.counter == 0 || turbinegas.counter == 579) {
				click();
				CompoundTag data = new CompoundTag();
				data.putInt("state", turbinegas.state - 1); //offline(0) to startup(-1), online(1) to offline(0)
				NBTControlPacket.send(data, turbinegas.getBlockPos());
			}
			return true;
		}

		if(turbinegas.state == 1 && x > leftPos + 74 && x <= leftPos + 74 + 29 && y >= topPos + 86 && y < topPos + 86 + 13) { //auto mode button
			click();
			CompoundTag data = new CompoundTag();
			data.putBoolean("autoMode", !turbinegas.autoMode);
			NBTControlPacket.send(data, turbinegas.getBlockPos());
			return true;
		}

		if(turbinegas.state == 1 && (topPos + 97 - slidStart) <= yStart && (topPos + 103 - slidStart) > yStart && leftPos + 36 < x && leftPos + 52 >= x) { //power slider
			CompoundTag data = new CompoundTag();
			data.putBoolean("autoMode", false); //if you click the slider with automode on, turns off automode
			NBTControlPacket.send(data, turbinegas.getBlockPos());
			click();
			return true;
		}

		return super.mouseClicked(mx, my, button);
	}

	@Override
	public boolean mouseDragged(double mx, double my, int button, double dragX, double dragY) {

		int x = (int) mx;
		int y = (int) my;

		if(!turbinegas.autoMode && turbinegas.state == 1 && leftPos + 36 < x && leftPos + 52 >= x && topPos + 37 < y && topPos + 103 >= y) { //area in which the slider can move

			if((topPos + 97 - slidStart) <= yStart && (topPos + 103 - slidStart) > yStart) {
				int slidPos = Math.max(0, Math.min(60, topPos + 100 - y));
				CompoundTag data = new CompoundTag();
				data.putDouble("slidPos", slidPos);
				NBTControlPacket.send(data, turbinegas.getBlockPos());
				return true;
			}
		}

		return super.mouseDragged(mx, my, button, dragX, dragY);
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {

		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 26, topPos + 108, 142, 16, turbinegas.power, TileEntityMachineTurbineGas.maxPower);

		if(turbinegas.state == 1) {
			double consumption = TileEntityMachineTurbineGas.getMaxConsumption(turbinegas.tanks[0].getTankType());
			this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 36, topPos + 36, 16, 66, mouseX, mouseY, "Fuel consumption: " + 20 * (consumption * 0.05D + consumption * turbinegas.throttle / 100) + " mb/s");
		} else {
			this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 36, topPos + 36, 16, 66, mouseX, mouseY, "Generator offline");
		}

		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 133, topPos + 23, 8, 72, mouseX, mouseY, "Temperature: " + Math.max(turbinegas.temp, 20) + "°C");

		this.renderTankInfo(graphics, turbinegas.tanks[0], mouseX, mouseY, leftPos + 8, topPos + 16, 16, 48);
		this.renderTankInfo(graphics, turbinegas.tanks[1], mouseX, mouseY, leftPos + 8, topPos + 70, 16, 32);
		this.renderTankInfo(graphics, turbinegas.tanks[2], mouseX, mouseY, leftPos + 147, topPos + 61, 16, 36);
		this.renderTankInfo(graphics, turbinegas.tanks[3], mouseX, mouseY, leftPos + 147, topPos + 21, 16, 36);

		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos - 16, topPos + 34, 16, 16, leftPos - 8, topPos + 44 + 16, I18nUtil.resolveKeyArray("desc.gui.turbinegas.automode"));

		List<String> fuels = new ArrayList<>();
		fuels.add(I18nUtil.resolveKey("desc.gui.turbinegas.fuels"));
		for(FluidType type : Fluids.getInNiceOrder()) {
			if(type.hasTrait(FT_Combustible.class) && type.getTrait(FT_Combustible.class).getGrade() == FuelGrade.GAS) {
				fuels.add("  " + type.getLocalizedName());
			}
		}
		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos - 16, topPos + 34 + 16, 16, 16, leftPos - 8, topPos + 44 + 16, fuels);

		if(turbinegas.tanks[0].getFill() < 5000 || turbinegas.tanks[1].getFill() < 1000)
			this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos - 16, topPos + 34 + 32, 16, 16, leftPos - 8, topPos + 44 + 16, I18nUtil.resolveKeyArray("desc.gui.turbinegas.warning"));
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2, 6, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight); //the main thing

		if(turbinegas.autoMode)
			drawTexturedModalRect(graphics, texture, leftPos + 74, topPos + 86, 194, 11, 29, 13); //auto mode button
		else
			drawTexturedModalRect(graphics, texture, leftPos + 74, topPos + 86, 194, 24, 29, 13);

		switch(turbinegas.state) {
		case 0:
			drawTexturedModalRect(graphics, texture, leftPos + 80, topPos + 32, 178, 38, 16, 16); //red button
			break;
		case -1:
			drawTexturedModalRect(graphics, texture, leftPos + 80, topPos + 32, 194, 38, 16, 16); //orange button
			displayStartup(graphics);
			break;
		case 1:
			drawTexturedModalRect(graphics, texture, leftPos + 80, topPos + 32, 210, 38, 16, 16); //green button
			drawPowerMeterDisplay(graphics, 20 * turbinegas.instantPowerOutput);
			break;
		default:
			break;
		}

		drawTexturedModalRect(graphics, texture, leftPos + 36, topPos + 97 - turbinegas.powerSliderPos, 178, 0, 16, 6); //power slider

		int power = (int) (turbinegas.power * 142 / TileEntityMachineTurbineGas.maxPower); //power storage
		drawTexturedModalRect(graphics, texture, leftPos + 26, topPos + 109, 0, 223, power, 16);

		GUIElements.drawSmoothTextureModalCircle(graphics, texture, leftPos + 64, topPos + 16, 0, 176, 64, 48, 48, (double) turbinegas.rpm / 100);
		drawThermometer(graphics, turbinegas.temp);

		this.drawInfoPanel(graphics, leftPos - 16, topPos + 34, 16, 16, 3); //info
		this.drawInfoPanel(graphics, leftPos - 16, topPos + 34 + 16, 16, 16, 2); //fuels
		if(turbinegas.tanks[0].getFill() < 5000 || turbinegas.tanks[1].getFill() < 1000)
			this.drawInfoPanel(graphics, leftPos - 16, topPos + 34 + 32, 16, 16, 7);
		if(turbinegas.tanks[0].getFill() == 0 || turbinegas.tanks[1].getFill() == 0)
			this.drawInfoPanel(graphics, leftPos - 16, topPos + 34 + 32, 16, 16, 6);

		this.renderTank(graphics, turbinegas.tanks[0], leftPos + 8, topPos + 65, 16, 48);
		this.renderTank(graphics, turbinegas.tanks[1], leftPos + 8, topPos + 103, 16, 32);
		this.renderTank(graphics, turbinegas.tanks[2], leftPos + 147, topPos + 98, 16, 36);
		this.renderTank(graphics, turbinegas.tanks[3], leftPos + 147, topPos + 58, 16, 36);
	}

	int numberToDisplay = 0; //for startup
	int digitNumber = 0;
	int exponent = 0;

	/** The display counts up through all the digits while starting */
	public void displayStartup(GuiGraphics graphics) {

		if(numberToDisplay < 8888888 && turbinegas.counter < 60) { //48 frames needed to complete
			digitNumber++;
			if(digitNumber == 9) {
				digitNumber = 1;
				exponent++;
			}
			numberToDisplay += Math.pow(10, exponent);
		}

		if(turbinegas.counter > 50)
			numberToDisplay = 0;

		drawPowerMeterDisplay(graphics, numberToDisplay);
	}

	/** 7 digit display, leading zeros turned off */
	protected void drawPowerMeterDisplay(GuiGraphics graphics, int number) {

		int firstDigitX = 65;
		int firstDigitY = 62;

		int[] digit = new int[7];

		for(int i = 6; i >= 0; i--) {
			digit[i] = number % 10;
			number = number / 10;
			drawTexturedModalRect(graphics, texture, leftPos + firstDigitX + i * 7, topPos + 9 + firstDigitY, 194 + digit[i] * 5, 0, 5, 11);
		}

		int uselessZeros = 0;

		for(int i = 0; i < 6; i++) {
			if(digit[i] == 0)
				uselessZeros++;
			else
				break;
		}

		for(int i = 0; i < uselessZeros; i++) {
			drawTexturedModalRect(graphics, texture, leftPos + firstDigitX + i * 7, topPos + 9 + firstDigitY, 244, 0, 5, 11);
		}
	}

	protected void drawThermometer(GuiGraphics graphics, int temp) {
		int maxTemp = 800;
		int h = Math.max(0, Math.min(64, 64 * temp / maxTemp));
		if(h <= 0) return;
		RenderSystem.enableBlend();
		drawTexturedModalRect(graphics, texture, leftPos + 136, topPos + 28 + 64 - h, 176, 64 - h, 2, h);
		RenderSystem.disableBlend();
	}
}
