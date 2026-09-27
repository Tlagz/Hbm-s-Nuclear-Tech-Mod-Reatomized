package com.hbm.inventory.recipes;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.recipes.gen.GenSILEXRecipes;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import com.hbm.util.Tuple.Pair;

import net.minecraft.world.item.ItemStack;

/**
 * SILEX recipes: an ingredient (item, ore dictionary key or fluid icon for fluids) is turned into "fluid" which the
 * laser separates into weighted outputs. Item translations make one item count as another (e.g. lapis dust as
 * lapis), dictionary translations one dictionary key as another (uranium dust as uranium ingots).
 * The recipe list is translated from the original by tools/gen_generic.py (GenSILEXRecipes).
 *
 * TODO the RBMK pellet and nuclear waste recipes (not ported yet), the tiny waste translation, JEI
 */
public class SILEXRecipes {

	private static final List<Pair<AStack, SILEXRecipe>> recipes = new ArrayList<>();
	private static final List<Pair<AStack, ItemStack>> itemTranslation = new ArrayList<>();
	private static final List<Pair<String, String>> dictTranslation = new ArrayList<>();

	public static void register() {
		recipes.clear();
		itemTranslation.clear();
		dictTranslation.clear();
		GenSILEXRecipes.register();
	}

	public static void put(AStack key, SILEXRecipe recipe) {
		recipes.add(new Pair<>(key, recipe));
	}

	public static void translateItem(AStack from, AStack to) {
		itemTranslation.add(new Pair<>(from, to.extractForNEI().get(0)));
	}

	public static void translateDict(String from, String to) {
		dictTranslation.add(new Pair<>(from, to));
	}

	public static SILEXRecipe getOutput(ItemStack stack) {

		if(stack.isEmpty()) return null;

		for(Pair<AStack, ItemStack> translation : itemTranslation) {
			if(translation.getKey().matchesRecipe(stack, true)) {
				stack = translation.getValue();
				break;
			}
		}

		for(Pair<AStack, SILEXRecipe> recipe : recipes) {
			if(recipe.getKey().matchesRecipe(stack, true)) return recipe.getValue();
		}

		// dictionary translation: the input is in the "from" tag, the recipe is keyed with the "to" key
		for(Pair<String, String> translation : dictTranslation) {
			if(!new OreDictStack(translation.getKey()).matchesRecipe(stack, true)) continue;
			for(Pair<AStack, SILEXRecipe> recipe : recipes) {
				if(recipe.getKey() instanceof OreDictStack dict && dict.name.equals(translation.getValue())) return recipe.getValue();
			}
		}

		return null;
	}

	public static List<Pair<AStack, SILEXRecipe>> getRecipes() {
		return recipes;
	}

	public static class SILEXRecipe {

		public int fluidProduced;
		public int fluidConsumed;
		public EnumWavelengths laserStrength;
		public List<WeightedOutput> outputs = new ArrayList<>();

		public SILEXRecipe(int fluidProduced, int fluidConsumed, EnumWavelengths laserStrength) {
			this.fluidProduced = fluidProduced;
			this.fluidConsumed = fluidConsumed;
			this.laserStrength = laserStrength;
		}

		public SILEXRecipe addOut(ItemStack stack, int weight) {
			outputs.add(new WeightedOutput(stack, weight));
			return this;
		}
	}

	public static record WeightedOutput(ItemStack stack, int weight) { }
}
