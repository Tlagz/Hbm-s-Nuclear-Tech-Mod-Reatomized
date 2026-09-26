package com.hbm.inventory.recipes;

import java.util.List;

import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;

/** Crucible alloying recipe: molten materials in, molten materials out, every [frequency] ticks */
public class CrucibleRecipe extends GenericRecipe {

	public MaterialStack[] input;
	public MaterialStack[] output;
	public int frequency = 1;

	public CrucibleRecipe(String name) {
		super(name);
	}

	public CrucibleRecipe setup(int frequency, ItemStack icon) {
		this.frequency = frequency;
		this.setIcon(icon);
		return this;
	}

	public CrucibleRecipe inputs(MaterialStack... input) { this.input = input; return this; }
	public CrucibleRecipe outputs(MaterialStack... output) { this.output = output; return this; }

	public int getInputAmount() {
		int content = 0;
		for(MaterialStack stack : input) content += stack.amount;
		return content;
	}

	@Override
	public List<String> print(boolean extended) {
		List<String> list = new java.util.ArrayList<>();
		header(list, extended);
		input(list);
		output(list);
		return list;
	}

	@Override
	protected void input(List<String> list) {
		list.add(ChatFormatting.BOLD + I18nUtil.resolveKey("gui.recipe.input") + ":");
		for(MaterialStack stack : input) {
			list.add(I18nUtil.resolveKey(stack.material.getUnlocalizedName()) + ": " + Mats.formatAmount(stack.amount, false));
		}
	}

	@Override
	protected void output(List<String> list) {
		list.add(ChatFormatting.BOLD + I18nUtil.resolveKey("gui.recipe.output") + ":");
		for(MaterialStack stack : output) {
			list.add(I18nUtil.resolveKey(stack.material.getUnlocalizedName()) + ": " + Mats.formatAmount(stack.amount, false));
		}
	}
}
