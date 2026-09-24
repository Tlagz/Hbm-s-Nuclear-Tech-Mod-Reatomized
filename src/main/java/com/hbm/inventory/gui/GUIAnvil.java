package com.hbm.inventory.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.container.ContainerAnvil;
import com.hbm.inventory.recipes.anvil.AnvilRecipes;
import com.hbm.inventory.recipes.anvil.AnvilRecipes.AnvilConstructionRecipe;
import com.hbm.inventory.recipes.anvil.AnvilRecipes.AnvilOutput;
import com.hbm.lib.RefStrings;
import com.hbm.packet.toserver.AnvilCraftPacket;
import com.hbm.util.InventoryUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * NTM anvil: smithing slots on top, the construction recipe list below (10 visible, paged by 2 columns),
 * search field, overlay filter, and the selected recipe's ingredients on the side panel.
 */
public class GUIAnvil extends GuiInfoContainer<ContainerAnvil> {

	public static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_anvil.png");

	private int tier;
	private List<AnvilConstructionRecipe> originList = new ArrayList<>();
	private List<AnvilConstructionRecipe> recipes = new ArrayList<>();
	int index;
	int size;
	int selection;
	private EditBox search;
	private Inventory playerInventory;
	private AnvilRecipes.OverlayType state = AnvilRecipes.OverlayType.NONE;
	int lastSize = 1;

	public GUIAnvil(ContainerAnvil menu, Inventory player, Component title) {
		super(menu, player, title);

		this.tier = menu.tier;
		this.imageWidth = 176;
		this.imageHeight = 222;
		this.inventoryLabelY = this.imageHeight - 96 + 2;

		this.playerInventory = player;
		for(AnvilConstructionRecipe recipe : AnvilRecipes.getConstruction()) {
			if(recipe.isTierValid(this.tier))
				this.originList.add(recipe);
		}

		regenerateRecipes();
	}

	@Override
	protected void init() {
		super.init();

		this.search = new EditBox(this.font, leftPos + 10, topPos + 111, 84, 12, Component.empty());
		this.search.setTextColor(-1);
		this.search.setTextColorUneditable(-1);
		this.search.setBordered(false);
		this.search.setMaxLength(25);
		this.search.setResponder(this::search);
		this.addRenderableWidget(this.search);
	}

	private boolean matchState(AnvilConstructionRecipe recipe) {
		return ((this.state == AnvilRecipes.OverlayType.NONE) || (recipe.getOverlay() == this.state));
	}

	private void regenerateRecipes() {

		this.recipes.clear();
		for(AnvilConstructionRecipe recipe : this.originList) {
			if(matchState(recipe))
				this.recipes.add(recipe);
		}
		resetPaging();
	}

	private void search(String search) {

		search = search.toLowerCase(Locale.US);

		this.recipes.clear();

		if(search.isEmpty()) {
			for(AnvilConstructionRecipe recipe : this.originList) {
				if(matchState(recipe)) {
					this.recipes.add(recipe);
				}
			}
		} else {
			for(AnvilConstructionRecipe recipe : this.originList) {
				for(String s : recipeToSearchList(recipe)) {
					if(s.contains(search)) {
						this.recipes.add(recipe);
						break;
					}
				}
			}
		}

		resetPaging();
	}

	private void resetPaging() {

		this.index = 0;
		this.selection = -1;
		this.size = Math.max(0, (int) Math.ceil((this.recipes.size() - 10) / 2D));
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		if(leftPos <= x && leftPos + imageWidth > x && topPos < y && topPos + imageHeight >= y && this.getSlotUnderMouse() == null) {
			if(scrollY > 0 && this.index > 0) this.index--;
			if(scrollY < 0 && this.index < this.size) this.index++;
			return true;
		}
		return super.mouseScrolled(x, y, scrollX, scrollY);
	}

	private void click() {
		this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	@Override
	public boolean mouseClicked(double x, double y, int k) {

		if(leftPos + 7 <= x && leftPos + 7 + 9 > x && topPos + 71 < y && topPos + 71 + 36 >= y) {
			click();
			if(this.index > 0)
				this.index--;
			return true;
		}

		if(leftPos + 106 <= x && leftPos + 106 + 9 > x && topPos + 71 < y && topPos + 71 + 36 >= y) {
			click();
			if(this.index < this.size)
				this.index++;
			return true;
		}

		if(leftPos + 52 <= x && leftPos + 52 + 18 > x && topPos + 53 < y && topPos + 53 + 18 >= y) {

			if(this.selection == -1)
				return true;

			click();
			AnvilCraftPacket.send(this.recipes.get(this.selection), Screen.hasShiftDown() ? 1 : 0);
			return true;
		}

		if(leftPos + 88 <= x && leftPos + 88 + 18 > x && topPos + 53 < y && topPos + 53 + 18 >= y) {
			click();
			AnvilRecipes.OverlayType[] values = AnvilRecipes.OverlayType.values();
			this.state = values[(this.state.ordinal() + 1) % values.length];
			regenerateRecipes();
			return true;
		}

		for(int i = index * 2; i < index * 2 + 10; i++) {

			if(i >= this.recipes.size())
				break;

			int ind = i - index * 2;

			int ix = 16 + 18 * (ind / 2);
			int iy = 71 + 18 * (ind % 2);
			if(leftPos + ix <= x && leftPos + ix + 18 > x && topPos + iy < y && topPos + iy + 18 >= y) {

				if(this.selection != i)
					this.selection = i;
				else
					this.selection = -1;

				click();
				return true;
			}
		}

		return super.mouseClicked(x, y, k);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		// typing into the search field must not close the GUI (inventory key)
		if(this.search.isFocused() && keyCode != 256) {
			return this.search.keyPressed(keyCode, scanCode, modifiers) || this.search.canConsumeInput();
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mX, int mY) {
		String name = this.title.getString();
		graphics.drawString(font, name, 61 - font.width(name) / 2, 8, 4210752, false);
		graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 4210752, false);

		if(this.selection >= 0) {

			AnvilConstructionRecipe recipe = recipes.get(this.selection);
			List<Component> list = recipeToList(recipe);
			int longest = 0;

			for(Component s : list) {
				int length = font.width(s);
				if(length > longest) longest = length;
			}

			float scale = 0.5F;
			graphics.pose().pushPose();
			graphics.pose().scale(scale, scale, scale);
			int offset = 0;
			for(Component s : list) {
				graphics.drawString(font, s, 260, 50 + offset, 0xffffff, false);
				offset += 9;
			}
			graphics.pose().popPose();

			this.lastSize = (int) (longest * scale);

		} else {
			this.lastSize = 0;
		}
	}

	@Override
	protected void drawTooltips(GuiGraphics graphics, int mX, int mY) {
		this.drawCustomInfoStat(graphics, mX, mY, leftPos + 88, topPos + 53, 18, 18, mX, mY,
				this.state == AnvilRecipes.OverlayType.NONE ? "All recipes" :
				this.state == AnvilRecipes.OverlayType.SMITHING ? "Smithing" :
				this.state == AnvilRecipes.OverlayType.CONSTRUCTION ? "Construction" :
				"Recycling");

		// output tooltips for the recipe icons
		for(int i = index * 2; i < index * 2 + 10 && i < recipes.size(); i++) {
			int ind = i - index * 2;
			int ix = leftPos + 16 + 18 * (ind / 2);
			int iy = topPos + 71 + 18 * (ind % 2);
			if(ix <= mX && ix + 18 > mX && iy < mY && iy + 18 >= mY) {
				graphics.renderTooltip(font, recipes.get(i).getDisplay(), mX, mY);
			}
		}
	}

	/** Ingredients (red if missing) and outputs of the selected recipe */
	public List<Component> recipeToList(AnvilConstructionRecipe recipe) {

		List<Component> list = new ArrayList<>();

		list.add(Component.literal("Inputs:").withStyle(ChatFormatting.YELLOW));

		for(AStack stack : recipe.input) {
			boolean enough = InventoryUtil.countMatches(playerInventory.player, stack) >= stack.stacksize;
			ItemStack display;

			if(stack instanceof ComparableStack comp) {
				display = comp.toStack();
			} else {
				List<ItemStack> ores = ((OreDictStack) stack).toStacks();
				if(ores.isEmpty()) {
					list.add(Component.literal("I AM ERROR"));
					continue;
				}
				display = ores.get((int) (Math.abs(System.currentTimeMillis() / 1000) % ores.size()));
			}

			Component line = Component.literal(">" + stack.stacksize + "x ").append(display.getHoverName());
			list.add(enough ? line : line.copy().withStyle(ChatFormatting.RED));
		}

		list.add(Component.empty());
		list.add(Component.literal("Outputs:").withStyle(ChatFormatting.YELLOW));

		for(AnvilOutput stack : recipe.output) {
			list.add(Component.literal(">" + stack.stack.getCount() + "x ").append(stack.stack.getHoverName())
					.append(stack.chance != 1F ? (" (" + (stack.chance * 100) + "%)") : ""));
		}

		return list;
	}

	/** All names of inputs (every ore dict variant) and outputs, for searching */
	public List<String> recipeToSearchList(AnvilConstructionRecipe recipe) {

		List<String> list = new ArrayList<>();

		for(AStack stack : recipe.input) {
			for(ItemStack in : stack.extractForNEI()) list.add(in.getHoverName().getString().toLowerCase(Locale.US));
		}

		for(AnvilOutput stack : recipe.output) {
			list.add(stack.stack.getHoverName().getString().toLowerCase(Locale.US));
		}

		return list;
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float inter, int mX, int mY) {

		drawTexturedModalRect(graphics, texture, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);

		// the side panel grows with the ingredient list
		int slide = Mth.clamp(this.lastSize - 42, 0, 1000);

		int mul = 1;
		while(true) {
			if(slide >= 51 * mul) {
				drawTexturedModalRect(graphics, texture, leftPos + 125 + 51 * mul, topPos + 17, 125, 17, 54, 108);
				mul++;
			} else {
				break;
			}
		}

		drawTexturedModalRect(graphics, texture, leftPos + 125 + slide, topPos + 17, 125, 17, 54, 108);

		if(this.search.isFocused()) {
			drawTexturedModalRect(graphics, texture, leftPos + 8, topPos + 108, 168, 222, 88, 16);
		}

		if(leftPos + 7 <= mX && leftPos + 7 + 9 > mX && topPos + 71 < mY && topPos + 71 + 36 >= mY) {
			drawTexturedModalRect(graphics, texture, leftPos + 7, topPos + 71, 176, 186, 9, 36);
		}
		if(leftPos + 106 <= mX && leftPos + 106 + 9 > mX && topPos + 71 < mY && topPos + 71 + 36 >= mY) {
			drawTexturedModalRect(graphics, texture, leftPos + 106, topPos + 71, 185, 186, 9, 36);
		}
		if(leftPos + 52 <= mX && leftPos + 52 + 18 > mX && topPos + 53 < mY && topPos + 53 + 18 >= mY) {
			drawTexturedModalRect(graphics, texture, leftPos + 52, topPos + 53, 176, 150, 18, 18);
		}

		boolean overFilter = leftPos + 88 <= mX && leftPos + 88 + 18 > mX && topPos + 53 < mY && topPos + 53 + 18 >= mY;
		int filterU = switch(this.state) {
			case SMITHING -> 200;
			case CONSTRUCTION -> 218;
			case RECYCLING -> 236;
			default -> -1;
		};
		if(filterU >= 0) drawTexturedModalRect(graphics, texture, leftPos + 88, topPos + 53, filterU, overFilter ? 18 : 0, 18, 18);

		for(int i = index * 2; i < index * 2 + 10; i++) {
			if(i >= recipes.size())
				break;

			int ind = i - index * 2;

			AnvilConstructionRecipe recipe = recipes.get(i);
			graphics.renderItem(recipe.getDisplay(), leftPos + 17 + 18 * (ind / 2), topPos + 72 + 18 * (ind % 2));

			// overlay icon and selection frame are drawn above the item
			graphics.pose().pushPose();
			graphics.pose().translate(0, 0, 300);
			drawTexturedModalRect(graphics, texture, leftPos + 16 + 18 * (ind / 2), topPos + 71 + 18 * (ind % 2), 18 + 18 * recipe.getOverlay().ordinal(), 222, 18, 18);

			if(selection == i)
				drawTexturedModalRect(graphics, texture, leftPos + 16 + 18 * (ind / 2), topPos + 71 + 18 * (ind % 2), 0, 222, 18, 18);
			graphics.pose().popPose();
		}
	}
}
