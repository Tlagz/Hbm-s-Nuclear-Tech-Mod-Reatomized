package com.hbm.inventory.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.hbm.inventory.container.ContainerCrucible;
import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial.SmeltingBehavior;
import com.hbm.inventory.recipes.CrucibleRecipe;
import com.hbm.inventory.recipes.CrucibleRecipes;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityCrucible;
import com.hbm.util.i18n.I18nUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GUICrucible extends GuiInfoContainer<ContainerCrucible> {

	private static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_crucible.png");
	private final TileEntityCrucible crucible;

	public GUICrucible(ContainerCrucible menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);
		this.crucible = menu.tile;

		this.imageWidth = 176;
		this.imageHeight = 214;
		this.inventoryLabelY = this.imageHeight - 96 + 2;
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int x, int y) {
		drawStackInfo(graphics, crucible.wasteStack, x, y, 16, 17);
		drawStackInfo(graphics, crucible.recipeStack, x, y, 61, 17);

		this.drawCustomInfoStat(graphics, x, y, leftPos + 125, topPos + 81, 34, 7, x, y, String.format(Locale.US, "%,d", crucible.progress) + " / " + String.format(Locale.US, "%,d", TileEntityCrucible.processTime) + "TU");
		this.drawCustomInfoStat(graphics, x, y, leftPos + 125, topPos + 90, 34, 7, x, y, String.format(Locale.US, "%,d", crucible.heat) + " / " + String.format(Locale.US, "%,d", TileEntityCrucible.maxHeat) + "TU");

		if(this.checkClick(x, y, 106, 80, 18, 18)) {
			CrucibleRecipe recipe = CrucibleRecipes.INSTANCE.recipeNameMap.get(this.crucible.recipe);
			if(recipe != null) {
				GUIElements.drawHoveringTextRecipe(graphics, recipe.print(Screen.hasShiftDown()), x, y, font, this.width, this.height);
			} else {
				graphics.renderTooltip(font, Component.literal(ChatFormatting.YELLOW + I18nUtil.resolveKey("gui.recipe.setRecipe")), x, y);
			}
		}
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if(this.checkClick((int) x, (int) y, 106, 80, 18, 18)) {
			GUIScreenRecipeSelector.openSelector(CrucibleRecipes.INSTANCE, crucible.getBlockPos(), crucible.recipe, 0, null, this);
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

		int pGauge = crucible.progress * 33 / TileEntityCrucible.processTime;
		if(pGauge > 0) drawTexturedModalRect(graphics, texture, leftPos + 126, topPos + 82, 176, 0, pGauge, 5);
		int hGauge = crucible.heat * 33 / TileEntityCrucible.maxHeat;
		if(hGauge > 0) drawTexturedModalRect(graphics, texture, leftPos + 126, topPos + 91, 176, 5, hGauge, 5);

		CrucibleRecipe recipe = CrucibleRecipes.INSTANCE.recipeNameMap.get(crucible.recipe);
		graphics.renderItem(recipe != null ? recipe.getIcon() : new ItemStack(ModItems.template_folder.get()), leftPos + 107, topPos + 81);

		if(!crucible.recipeStack.isEmpty()) drawStack(graphics, crucible.recipeStack, TileEntityCrucible.recipeZCapacity, 62, 97);
		if(!crucible.wasteStack.isEmpty()) drawStack(graphics, crucible.wasteStack, TileEntityCrucible.wasteZCapacity, 17, 97);
	}

	protected void drawStackInfo(GuiGraphics graphics, List<MaterialStack> stack, int mouseX, int mouseY, int x, int y) {
		List<String> list = new ArrayList<>();

		if(stack.isEmpty())
			list.add(ChatFormatting.RED + "Empty");

		for(MaterialStack sta : stack) {
			list.add(ChatFormatting.YELLOW + I18nUtil.resolveKey(sta.material.getUnlocalizedName()) + ": " + Mats.formatAmount(sta.amount, Screen.hasShiftDown()));
		}

		this.drawCustomInfoStat(graphics, mouseX, mouseY, leftPos + x, topPos + y, 36, 81, mouseX, mouseY, list);
	}

	/** The molten materials stacked in their colors, additives with their own texture */
	protected void drawStack(GuiGraphics graphics, List<MaterialStack> stack, int capacity, int x, int y) {

		if(stack.isEmpty()) return;

		int lastHeight = 0;
		int lastQuant = 0;

		for(MaterialStack sta : stack) {

			int targetHeight = (lastQuant + sta.amount) * 79 / capacity;

			if(lastHeight == targetHeight) continue; //skip draw calls that would be 0 pixels high

			int offset = sta.material.smeltable == SmeltingBehavior.ADDITIVE ? 34 : 0; //additives use a differnt texture

			int hex = sta.material.moltenColor;
			RenderSystem.setShaderColor(((hex >> 16) & 0xFF) / 255F, ((hex >> 8) & 0xFF) / 255F, (hex & 0xFF) / 255F, 1F);
			drawTexturedModalRect(graphics, texture, leftPos + x, topPos + y - targetHeight, 176 + offset, 89 - targetHeight, 34, targetHeight - lastHeight);
			RenderSystem.enableBlend();
			RenderSystem.setShaderColor(1F, 1F, 1F, 0.3F);
			drawTexturedModalRect(graphics, texture, leftPos + x, topPos + y - targetHeight, 176 + offset, 89 - targetHeight, 34, targetHeight - lastHeight);
			RenderSystem.disableBlend();

			lastQuant += sta.amount;
			lastHeight = targetHeight;
		}

		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
	}
}
