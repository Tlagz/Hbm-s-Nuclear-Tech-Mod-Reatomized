package com.hbm.inventory.recipes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.recipes.gen.GenSolderingRecipes;
import com.hbm.main.MainRegistry;

import net.minecraft.world.item.ItemStack;

/**
 * Soldering station recipes: up to three toppings, two boards and one solder (plus an optional fluid) into one
 * output. Translated from the original by tools/gen_generic.py (GenSolderingRecipes).
 *
 * TODO JSON config, JEI
 */
public class SolderingRecipes {

	public static List<SolderingRecipe> recipes = new ArrayList<>();

	/** Every ingredient of each kind, for the slot restrictions */
	public static HashSet<AStack> toppings = new HashSet<>();
	public static HashSet<AStack> pcb = new HashSet<>();
	public static HashSet<AStack> solder = new HashSet<>();

	public static void registerDefaults() {
		recipes.clear();
		toppings.clear();
		pcb.clear();
		solder.clear();
		GenSolderingRecipes.register(recipes);
		MainRegistry.logger.info("Soldering recipes: " + recipes.size());
	}

	/** Slots 0-2 toppings, 3-4 boards, 5 solder */
	public static SolderingRecipe getRecipe(ItemStack[] inputs) {
		for(SolderingRecipe recipe : recipes) {
			if(matchesIngredients(new ItemStack[] {inputs[0], inputs[1], inputs[2]}, recipe.toppings) &&
					matchesIngredients(new ItemStack[] {inputs[3], inputs[4]}, recipe.pcb) &&
					matchesIngredients(new ItemStack[] {inputs[5]}, recipe.solder)) return recipe;
		}
		return null;
	}

	/** Every filled input has to match a different ingredient (in the needed amount) and every ingredient has to be used */
	public static boolean matchesIngredients(ItemStack[] inputs, AStack[] recipe) {

		List<AStack> recipeList = new ArrayList<>(List.of(recipe));

		for(ItemStack inputStack : inputs) {

			if(inputStack != null && !inputStack.isEmpty()) {
				boolean hasMatch = false;
				Iterator<AStack> iterator = recipeList.iterator();

				while(iterator.hasNext()) {
					AStack recipeStack = iterator.next();

					if(recipeStack.matchesRecipe(inputStack, true) && inputStack.getCount() >= recipeStack.stacksize) {
						hasMatch = true;
						iterator.remove();
						break;
					}
				}

				if(!hasMatch) return false;
			}
		}

		return recipeList.isEmpty();
	}

	public static class SolderingRecipe {
		public AStack[] toppings;
		public AStack[] pcb;
		public AStack[] solder;
		public FluidStack fluid;
		public ItemStack output;
		public int duration;
		public long consumption;

		public SolderingRecipe(ItemStack output, int duration, long consumption, FluidStack fluid, AStack[] toppings, AStack[] pcb, AStack[] solder) {
			this.toppings = toppings;
			this.pcb = pcb;
			this.solder = solder;
			this.fluid = fluid;
			this.output = output;
			this.duration = duration;
			this.consumption = consumption;

			for(AStack t : toppings) SolderingRecipes.toppings.add(t);
			for(AStack t : pcb) SolderingRecipes.pcb.add(t);
			for(AStack t : solder) SolderingRecipes.solder.add(t);
		}
	}
}
