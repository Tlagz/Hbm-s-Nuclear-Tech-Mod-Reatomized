package com.hbm.inventory.gui;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.container.ContainerMixer;
import com.hbm.inventory.recipes.MixerRecipes;
import com.hbm.inventory.recipes.MixerRecipes.MixerRecipe;
import com.hbm.lib.RefStrings;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityMachineMixer;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GUIMixer extends GuiInfoContainer<ContainerMixer> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_mixer.png");
	private final TileEntityMachineMixer mixer;

	public GUIMixer(ContainerMixer menu, Inventory invPlayer, Component title) {
		super(menu, invPlayer, title);
		this.mixer = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 204;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {

		this.drawElectricityInfo(graphics, mouseX, mouseY, leftPos + 12, topPos + 18, 16, 52, mixer.getPower(), mixer.getMaxPower());

		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 152, topPos + 55, 8, 8, mouseX, mouseY,
				I18nUtil.resolveKey("desc.gui.upgrade"), I18nUtil.resolveKey("desc.gui.upgrade.speed"), I18nUtil.resolveKey("desc.gui.upgrade.power"), I18nUtil.resolveKey("desc.gui.upgrade.overdrive"));

		MixerRecipe[] recipes = MixerRecipes.getOutput(mixer.tanks[2].getTankType());

		if(recipes != null && recipes.length > 1) {
			List<String> label = new ArrayList<>();
			label.add(ChatFormatting.YELLOW + "Current recipe (" + (mixer.recipeIndex + 1) + "/" + recipes.length + "):");
			MixerRecipe recipe = recipes[mixer.recipeIndex % recipes.length];
			if(recipe.input1 != null) label.add("-" + recipe.input1.type.getLocalizedName());
			if(recipe.input2 != null) label.add("-" + recipe.input2.type.getLocalizedName());
			if(recipe.solidInput != null) {
				List<ItemStack> options = recipe.solidInput.extractForNEI();
				if(!options.isEmpty()) label.add("-" + options.get((int) (System.currentTimeMillis() / 1000 % options.size())).getHoverName().getString());
			}
			label.add(ChatFormatting.RED + "Click to change!");
			this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + 71, topPos + 17, 12, 12, mouseX, mouseY, label);
		}

		this.renderTankInfo(graphics, mixer.tanks[0], mouseX, mouseY, leftPos + 52, topPos + 18, 7, 52);
		this.renderTankInfo(graphics, mixer.tanks[1], mouseX, mouseY, leftPos + 61, topPos + 18, 7, 52);
		this.renderTankInfo(graphics, mixer.tanks[2], mouseX, mouseY, leftPos + 126, topPos + 18, 16, 52);
	}

	/** Cycles through the output's recipes */
	@Override
	public boolean mouseClicked(double x, double y, int button) {

		if(leftPos + 71 <= x && leftPos + 71 + 12 > x && topPos + 17 < y && topPos + 17 + 12 >= y) {
			this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			CompoundTag data = new CompoundTag();
			data.putBoolean("toggle", true);
			NBTControlPacket.send(data, mixer.getBlockPos());
			return true;
		}

		return super.mouseClicked(x, y, button);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String name = this.title.getString();
		graphics.drawString(font, name, imageWidth / 2 + 40 / 2 - font.width(name) / 2, 6, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		int i = (int) (mixer.getPower() * 52 / mixer.getMaxPower());
		drawTexturedModalRect(graphics, texture, leftPos + 12, topPos + 70 - i, 176, 52 - i, 16, i);

		if(mixer.processTime > 0 && mixer.progress > 0) {
			int j = mixer.progress * 52 / mixer.processTime;
			drawTexturedModalRect(graphics, texture, leftPos + 71, topPos + 31, 192, 0, j, 44);
		}

		this.renderTank(graphics, mixer.tanks[0], leftPos + 52, topPos + 70, 7, 52);
		this.renderTank(graphics, mixer.tanks[1], leftPos + 61, topPos + 70, 7, 52);
		this.renderTank(graphics, mixer.tanks[2], leftPos + 126, topPos + 70, 16, 52);

		this.drawInfoPanel(graphics, leftPos + 152, topPos + 55, 8, 8, 8);
	}
}
