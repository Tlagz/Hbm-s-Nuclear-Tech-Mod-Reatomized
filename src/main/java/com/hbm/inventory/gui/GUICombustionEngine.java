package com.hbm.inventory.gui;

import java.util.List;
import java.util.Locale;

import com.hbm.inventory.container.ContainerCombustionEngine;
import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.items.machine.ItemPistons;
import com.hbm.items.machine.ItemPistons.EnumPistonType;
import com.hbm.lib.RefStrings;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityMachineCombustionEngine;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

/** Combustion engine: ignition switch, the throttle slider (drag), power and fuel bars */
public class GUICombustionEngine extends GuiInfoContainer<ContainerCombustionEngine> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/generators/gui_combustion.png");
	private final TileEntityMachineCombustionEngine engine;
	private int setting = 0;
	private boolean isMouseLocked = false;

	public GUICombustionEngine(ContainerCombustionEngine menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		engine = menu.tile;
		this.setting = engine.setting;

		this.imageWidth = 176;
		this.imageHeight = 203;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int x, int y) {

		if(!isMouseLocked) {
			this.drawElectricityInfo(graphics, x, y, leftPos + 143, topPos + 17, 16, 52, engine.getPower(), TileEntityMachineCombustionEngine.maxPower);
			this.renderTankInfo(graphics, engine.tank, x, y, leftPos + 35, topPos + 17, 16, 52);
		}

		if(isMouseLocked || (leftPos + 80 <= x && leftPos + 80 + 34 > x && topPos + 38 < y && topPos + 38 + 8 >= y)) {
			graphics.renderComponentTooltip(font, List.of(Component.literal(((setting * 2) / 10D) + "mB/t")), Mth.clamp(x, leftPos + 80, leftPos + 114), Mth.clamp(y, topPos + 38, topPos + 46));
		}

		EnumPistonType piston = ItemPistons.getType(engine.getItem(2));
		if(piston != null) {
			double power = 0;
			if(engine.tank.getTankType().hasTrait(FT_Combustible.class)) {
				FT_Combustible trait = engine.tank.getTankType().getTrait(FT_Combustible.class);
				power = setting * 0.2 * trait.getCombustionEnergy() / 1_000D * piston.eff[trait.getGrade().ordinal()];
			}
			String c = ChatFormatting.YELLOW + "";
			this.drawCustomInfoStat(graphics, x, y, leftPos + 79, topPos + 50, 35, 14, x, y, c + String.format(Locale.US, "%,d", (int) (power)) + " HE/t", c + String.format(Locale.US, "%,d", (int) (power * 20)) + " HE/s");
		}

		this.drawCustomInfoStat(graphics, x, y, leftPos + 79, topPos + 13, 35, 15, x, y, "Ignition");
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {

		if(leftPos + 89 <= x && leftPos + 89 + 16 > x && topPos + 13 < y && topPos + 13 + 14 >= y) {
			click();
			CompoundTag data = new CompoundTag();
			data.putBoolean("turnOn", true);
			NBTControlPacket.send(data, engine.getBlockPos());
			return true;
		}

		if(leftPos + 79 <= x && leftPos + 79 + 36 > x && topPos + 38 < y && topPos + 38 + 8 >= y) {
			click();
			isMouseLocked = true;
			updateSetting(x);
			return true;
		}

		return super.mouseClicked(x, y, button);
	}

	@Override
	public boolean mouseDragged(double x, double y, int button, double dragX, double dragY) {
		if(isMouseLocked) {
			updateSetting(x);
			return true;
		}
		return super.mouseDragged(x, y, button, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(double x, double y, int button) {
		if(isMouseLocked && (button == 0 || button == 1)) {
			isMouseLocked = false;
			return true;
		}
		return super.mouseReleased(x, y, button);
	}

	private void updateSetting(double x) {
		int setting = Mth.clamp((int) ((x - leftPos - 81) * 30 / 32), 0, 30);
		if(this.setting != setting) {
			this.setting = setting;
			CompoundTag data = new CompoundTag();
			data.putInt("setting", setting);
			NBTControlPacket.send(data, engine.getBlockPos());
		}
	}

	private void click() {
		this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		EnumPistonType piston = ItemPistons.getType(engine.getItem(2));
		if(piston != null) {
			drawTexturedModalRect(graphics, texture, leftPos + 80, topPos + 51, 176, 52 + piston.ordinal() * 12, 25, 12);
		}

		drawTexturedModalRect(graphics, texture, leftPos + 79 + (setting * 32 / 30), topPos + 38, 192, 15, 4, 8);

		if(engine.isOn) {
			drawTexturedModalRect(graphics, texture, leftPos + 79, topPos + 13, 192, 0, 35, 15);
		}

		int i = (int) (engine.power * 53 / TileEntityMachineCombustionEngine.maxPower);
		drawTexturedModalRect(graphics, texture, leftPos + 143, topPos + 69 - i, 176, 52 - i, 16, i);

		this.renderTank(graphics, engine.tank, leftPos + 35, topPos + 69, 16, 52);
	}
}
