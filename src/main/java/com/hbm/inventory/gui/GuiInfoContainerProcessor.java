package com.hbm.inventory.gui;

import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBlueprints;
import com.hbm.module.machine.ModuleMachineBase;
import com.hbm.util.i18n.I18nUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * For things that use the GUIScreenRecipeSelector, mainly for the preview rendering
 * and certain other standardized GUI components.
 * @author hbm
 */
public abstract class GuiInfoContainerProcessor<T extends AbstractContainerMenu> extends GuiInfoContainer<T> {

	protected ModuleMachineBase[] processorModule;

	public GuiInfoContainerProcessor(T menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	/**
	 * Array of all recipe fields.
	 * Each recipe field is defined by an int array
	 * [ selector x / selector y / template slot index ]
	 */
	public abstract int[][] getSelectorPositions();

	/** Position of the tile the recipe selector sends the recipe change to (the original's IControlReceiver) */
	public abstract BlockPos getControlReceiver();

	public abstract ResourceLocation getTexture();

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		int[][] selectors = this.getSelectorPositions();

		// draw the tooltips for the recipe selectors
		for(int i = 0; i < selectors.length; i++) {
			int x = selectors[i][0];
			int y = selectors[i][1];

			if(leftPos + x <= mouseX && leftPos + x + 18 > mouseX && topPos + y < mouseY && topPos + y + 18 >= mouseY) {

				ModuleMachineBase module = this.processorModule[i];
				GenericRecipe recipe = module.getRecipe();

				if(recipe != null) {
					GUIElements.drawHoveringTextRecipe(graphics, recipe.print(Screen.hasShiftDown()), mouseX, mouseY, font, this.width, this.height);
				} else {
					graphics.renderTooltip(font, Component.literal(ChatFormatting.YELLOW + I18nUtil.resolveKey("gui.recipe.setRecipe")), mouseX, mouseY);
				}
			}
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int[][] selectors = this.getSelectorPositions();

		// standard recipe selector open
		for(int i = 0; i < selectors.length; i++) {
			int ix = selectors[i][0];
			int iy = selectors[i][1];
			int slot = selectors[i][2];

			if(this.checkClick((int) mouseX, (int) mouseY, ix, iy, 18, 18)) {
				this.click();

				ModuleMachineBase module = this.processorModule[i];
				GUIScreenRecipeSelector.openSelector(module.getRecipeSet(), this.getControlReceiver(),
						module.getRecipeName(), i, ItemBlueprints.grabPool(this.menu.getSlot(slot).getItem()), this);
				return true;
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	protected void click() {
		this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	/** Renders the standard double LEDs which are 3x6 pixels large and two pixels apart */
	protected void renderStandardLEDs(GuiGraphics graphics, boolean didProcess, GenericRecipe recipe, long power, int lX, int lY, int sX, int sY) {

		/// LEFT LED
		if(didProcess) {
			drawTexturedModalRect(graphics, getTexture(), leftPos + lX, topPos + lY, sX, sY, 3, 6);
		} else if(recipe != null) {
			drawTexturedModalRect(graphics, getTexture(), leftPos + lX, topPos + lY, sX - 3, sY, 3, 6);
		}

		/// RIGHT LED
		if(didProcess) {
			drawTexturedModalRect(graphics, getTexture(), leftPos + lX + 5, topPos + lY, sX, sY, 3, 6);
		} else if(recipe != null && power >= recipe.power) {
			drawTexturedModalRect(graphics, getTexture(), leftPos + lX + 5, topPos + lY, sX - 3, sY, 3, 6);
		}
	}

	/**
	 * Draws the half-opacity item icons over the slots as well as the recipe selector button
	 */
	protected void renderRecipeIcons(GuiGraphics graphics) {

		int[][] selectors = this.getSelectorPositions();

		// draws the icons for the recipe selectors as well as the ghost items in empty slots
		for(int i = 0; i < selectors.length; i++) {
			int ix = selectors[i][0] + 1;
			int iy = selectors[i][1] + 1;
			ModuleMachineBase module = this.processorModule[i];
			GenericRecipe recipe = module.getRecipe();

			graphics.renderItem(recipe != null ? recipe.getIcon() : new ItemStack(ModItems.template_folder.get()), leftPos + ix, topPos + iy);

			if(recipe != null && recipe.inputItem != null) {

				for(int j = 0; j < recipe.inputItem.length && j < module.inputSlots.length; j++) {
					Slot slot = this.menu.getSlot(module.inputSlots[j]);
					if(!slot.hasItem()) graphics.renderItem(recipe.inputItem[j].extractForCyclingDisplay(20), leftPos + slot.x, topPos + slot.y);
				}

				// the empty slot's background at half opacity over the item makes it look faded
				RenderSystem.enableBlend();
				graphics.setColor(1F, 1F, 1F, 0.5F);
				graphics.pose().pushPose();
				graphics.pose().translate(0, 0, 300);

				for(int j = 0; j < recipe.inputItem.length && j < module.inputSlots.length; j++) {
					Slot slot = this.menu.getSlot(module.inputSlots[j]);
					if(!slot.hasItem()) drawTexturedModalRect(graphics, getTexture(), leftPos + slot.x, topPos + slot.y, slot.x, slot.y, 16, 16);
				}

				graphics.pose().popPose();
				graphics.setColor(1F, 1F, 1F, 1F);
				RenderSystem.disableBlend();
			}
		}
	}
}
