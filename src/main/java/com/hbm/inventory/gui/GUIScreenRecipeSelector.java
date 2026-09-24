package com.hbm.inventory.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.lib.RefStrings;
import com.hbm.packet.toserver.NBTControlPacket;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

/**
 * Recipe picker of generic recipe machines, opened from the machine GUI over it. Pooled recipes only show up
 * with the matching blueprint installed. The selection is sent to the machine when the screen closes, closing
 * returns to the machine GUI.
 */
public class GUIScreenRecipeSelector extends Screen {

	protected static final ResourceLocation texture = RefStrings.loc("textures/gui/processing/gui_recipe_selector.png");

	//basic GUI setup
	protected int xSize = 176;
	protected int ySize = 132;
	protected int guiLeft;
	protected int guiTop;
	// search crap
	protected GenericRecipes<? extends GenericRecipe> recipeSet;
	protected List<GenericRecipe> recipes = new ArrayList<>();
	protected EditBox search;
	protected int pageIndex;
	protected int size;
	protected String selection;
	public static final String NULL_SELECTION = "null";
	// callback
	protected int index;
	protected BlockPos tile;
	protected Screen previousScreen;
	protected String installedPool;

	public static void openSelector(GenericRecipes<? extends GenericRecipe> recipeSet, BlockPos tile, String selection, int index, String installedPool, Screen previousScreen) {
		Minecraft.getInstance().setScreen(new GUIScreenRecipeSelector(recipeSet, tile, selection, index, installedPool, previousScreen));
	}

	public GUIScreenRecipeSelector(GenericRecipes<? extends GenericRecipe> recipeSet, BlockPos tile, String selection, int index, String installedPool, Screen previousScreen) {
		super(Component.empty());
		this.recipeSet = recipeSet;
		this.tile = tile;
		this.selection = selection;
		this.index = index;
		this.installedPool = installedPool;
		this.previousScreen = previousScreen;
		if(this.selection == null) this.selection = NULL_SELECTION;

		regenerateRecipes();
	}

	@Override
	protected void init() {
		super.init();
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;

		String text = this.search != null ? this.search.getValue() : "";
		this.search = new EditBox(this.font, guiLeft + 28, guiTop + 111, 102, 12, Component.empty());
		this.search.setTextColor(-1);
		this.search.setTextColorUneditable(-1);
		this.search.setBordered(false);
		this.search.setMaxLength(32);
		this.search.setValue(text);
		this.search.setResponder(this::search);
		this.addRenderableWidget(this.search);
	}

	private boolean isAvailable(GenericRecipe recipe) {
		return !recipe.isPooled() || (this.installedPool != null && recipe.isPartOfPool(installedPool));
	}

	private void regenerateRecipes() {

		this.recipes.clear();

		for(GenericRecipe recipe : recipeSet.recipeOrderedList) {
			if(isAvailable(recipe)) this.recipes.add(recipe);
		}

		resetPaging();
	}

	protected void search(String search) {
		this.recipes.clear();

		if(search.isEmpty()) {
			regenerateRecipes();
		} else {
			for(GenericRecipe recipe : recipeSet.recipeOrderedList) {
				if(recipe.matchesSearch(search) && isAvailable(recipe)) this.recipes.add(recipe);
			}

			resetPaging();
		}
	}

	private void resetPaging() {
		this.pageIndex = 0;
		this.size = Math.max(0, (int) Math.ceil((this.recipes.size() - 40) / 8D));
	}

	private boolean hovering(int mouseX, int mouseY, int x, int y, int w, int h) {
		return guiLeft + x <= mouseX && guiLeft + x + w > mouseX && guiTop + y < mouseY && guiTop + y + h >= mouseY;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		this.drawIcons(graphics);

		if(hovering(mouseX, mouseY, 7, 17, 144, 90)) {
			for(int i = pageIndex * 8; i < pageIndex * 8 + 40; i++) {
				if(i >= this.recipes.size()) break;

				int ind = i - pageIndex * 8;
				int ix = 7 + 18 * (ind % 8);
				int iy = 17 + 18 * (ind / 8);

				if(hovering(mouseX, mouseY, ix, iy, 18, 18)) {
					GenericRecipe recipe = recipes.get(i);
					GUIElements.drawHoveringTextRecipe(graphics, recipe.print(Screen.hasShiftDown()), mouseX, mouseY, font, this.width, this.height);
				}
			}
		}

		if(hovering(mouseX, mouseY, 151, 71, 18, 18)) {
			GenericRecipe recipe = this.recipeSet.recipeNameMap.get(selection);
			if(recipe != null) GUIElements.drawHoveringTextRecipe(graphics, recipe.print(Screen.hasShiftDown()), mouseX, mouseY, font, this.width, this.height);
		}

		if(hovering(mouseX, mouseY, 152, 90, 16, 16)) {
			graphics.renderTooltip(font, Component.literal("Close").withStyle(ChatFormatting.YELLOW), mouseX, mouseY);
		}

		if(hovering(mouseX, mouseY, 134, 108, 16, 16)) {
			graphics.renderTooltip(font, Component.literal("Clear search").withStyle(ChatFormatting.YELLOW), mouseX, mouseY);
		}

		if(hovering(mouseX, mouseY, 8, 108, 16, 16)) {
			graphics.renderTooltip(font, Component.literal("Press ENTER to toggle focus").withStyle(ChatFormatting.ITALIC), mouseX, mouseY);
		}
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.renderBackground(graphics, mouseX, mouseY, partialTick);

		graphics.blit(texture, guiLeft, guiTop, 0, 0, xSize, ySize);

		if(this.search.isFocused()) {
			graphics.blit(texture, guiLeft + 26, guiTop + 108, 0, 132, 106, 16);
		}

		if(hovering(mouseX, mouseY, 152, 18, 16, 16)) graphics.blit(texture, guiLeft + 152, guiTop + 18, 176, 0, 16, 16);
		if(hovering(mouseX, mouseY, 152, 36, 16, 16)) graphics.blit(texture, guiLeft + 152, guiTop + 36, 176, 16, 16, 16);
		if(hovering(mouseX, mouseY, 152, 90, 16, 16)) graphics.blit(texture, guiLeft + 152, guiTop + 90, 176, 32, 16, 16);
		if(hovering(mouseX, mouseY, 134, 108, 16, 16)) graphics.blit(texture, guiLeft + 134, guiTop + 108, 176, 48, 16, 16);
		if(hovering(mouseX, mouseY, 8, 108, 16, 16)) graphics.blit(texture, guiLeft + 8, guiTop + 108, 176, 64, 16, 16);

		for(int i = pageIndex * 8; i < pageIndex * 8 + 40; i++) {
			if(i >= recipes.size()) break;
			int ind = i - pageIndex * 8;
			GenericRecipe recipe = recipes.get(i);
			if(recipe.getInternalName().equals(this.selection)) graphics.blit(texture, guiLeft + 7 + 18 * (ind % 8), guiTop + 17 + 18 * (ind / 8), 192, 0, 18, 18);
		}
	}

	/** The recipe icons and the selected recipe's icon */
	private void drawIcons(GuiGraphics graphics) {
		for(int i = pageIndex * 8; i < pageIndex * 8 + 40; i++) {
			if(i >= recipes.size()) break;

			int ind = i - pageIndex * 8;
			GenericRecipe recipe = recipes.get(i);
			graphics.renderItem(recipe.getIcon(), guiLeft + 8 + 18 * (ind % 8), guiTop + 18 + 18 * (ind / 8));
		}

		GenericRecipe selected = this.recipeSet.recipeNameMap.get(selection);
		if(selected != null) graphics.renderItem(selected.getIcon(), guiLeft + 152, guiTop + 72);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if(scrollY > 0 && this.pageIndex > 0) this.pageIndex--;
		if(scrollY < 0 && this.pageIndex < this.size) this.pageIndex++;
		return true;
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		int x = (int) mx;
		int y = (int) my;

		if(hovering(x, y, 152, 18, 16, 16)) {
			click();
			if(this.pageIndex > 0) this.pageIndex--;
			return true;
		}

		if(hovering(x, y, 152, 36, 16, 16)) {
			click();
			if(this.pageIndex < this.size) this.pageIndex++;
			return true;
		}

		if(hovering(x, y, 134, 108, 16, 16)) {
			this.search.setValue("");
			this.search("");
			this.setFocused(this.search);
			return true;
		}

		for(int i = pageIndex * 8; i < pageIndex * 8 + 40; i++) {
			if(i >= this.recipes.size()) break;

			int ind = i - pageIndex * 8;
			int ix = 7 + 18 * (ind % 8);
			int iy = 17 + 18 * (ind / 8);

			if(hovering(x, y, ix, iy, 18, 18)) {

				String newSelection = recipes.get(i).getInternalName();

				if(!newSelection.equals(selection))
					this.selection = newSelection;
				else
					this.selection = NULL_SELECTION;

				click();
				return true;
			}
		}

		if(hovering(x, y, 151, 71, 18, 18)) {
			if(!NULL_SELECTION.equals(this.selection)) {
				this.selection = NULL_SELECTION;
				click();
				return true;
			}
		}

		if(hovering(x, y, 152, 90, 16, 16)) {
			this.onClose();
			return true;
		}

		return super.mouseClicked(mx, my, button);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

		if(keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			this.setFocused(this.search.isFocused() ? null : this.search);
			return true;
		}

		if(keyCode == GLFW.GLFW_KEY_ESCAPE) {
			this.onClose();
			return true;
		}

		if(this.search.isFocused()) {
			return this.search.keyPressed(keyCode, scanCode, modifiers) || true;
		}

		if(keyCode == GLFW.GLFW_KEY_UP) pageIndex--;
		if(keyCode == GLFW.GLFW_KEY_DOWN) pageIndex++;
		if(keyCode == GLFW.GLFW_KEY_PAGE_UP) pageIndex -= 5;
		if(keyCode == GLFW.GLFW_KEY_PAGE_DOWN) pageIndex += 5;
		if(keyCode == GLFW.GLFW_KEY_HOME) pageIndex = 0;
		if(keyCode == GLFW.GLFW_KEY_END) pageIndex = size;

		pageIndex = Mth.clamp(pageIndex, 0, size);

		if(this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
			this.onClose();
			return true;
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	/** Back to the machine GUI */
	@Override
	public void onClose() {
		this.minecraft.setScreen(previousScreen);
	}

	/** Called whenever this screen goes away (back to the machine or closed entirely), sends the selection */
	@Override
	public void removed() {
		CompoundTag data = new CompoundTag();
		data.putInt("index", this.index);
		data.putString("selection", this.selection);
		NBTControlPacket.send(data, tile);

		this.click();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	public void click() {
		this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}
}
