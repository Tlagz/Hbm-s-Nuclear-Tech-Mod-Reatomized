package com.hbm.inventory.recipes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.recipes.gen.GenArcWelderRecipes;
import com.hbm.main.MainRegistry;

import net.minecraft.world.item.ItemStack;

/**
 * Arc welder recipes: up to 3 shapeless inputs, an optional fluid, power per tick and duration.
 * The list is translated from the original by tools/gen_generic.py (GenArcWelderRecipes).
 *
 * TODO JSON config (SerializableRecipe), JEI
 */
public class ArcWelderRecipes {

	public static List<ArcWelderRecipe> recipes = new ArrayList<>();

	public static void registerDefaults() {
		recipes.clear();
		GenArcWelderRecipes.register(recipes);
		MainRegistry.logger.info("Arc welder recipes: " + recipes.size());
	}

	/** The recipe whose ingredients are all covered by the inputs (in any order), null if none */
	public static ArcWelderRecipe getRecipe(ItemStack... inputs) {

		outer:
		for(ArcWelderRecipe recipe : recipes) {

			List<AStack> recipeList = new ArrayList<>();
			for(AStack ingredient : recipe.ingredients) recipeList.add(ingredient);

			for(int i = 0; i < inputs.length; i++) {

				ItemStack inputStack = inputs[i];

				if(inputStack != null && !inputStack.isEmpty()) {

					boolean hasMatch = false;
					Iterator<AStack> iterator = recipeList.iterator();

					while(iterator.hasNext()) {
						AStack recipeStack = iterator.next();

						if(recipeStack.matchesRecipe(inputStack, true) && inputStack.getCount() >= recipeStack.stacksize) {
							hasMatch = true;
							recipeList.remove(recipeStack);
							break;
						}
					}

					if(!hasMatch) {
						continue outer;
					}
				}
			}

			if(recipeList.isEmpty()) return recipe;
		}

		return null;
	}

	public static class ArcWelderRecipe {

		public AStack[] ingredients;
		public FluidStack fluid;
		public ItemStack output;
		public int duration;
		public long consumption;

		public ArcWelderRecipe(ItemStack output, int duration, long consumption, FluidStack fluid, AStack... ingredients) {
			this.ingredients = ingredients;
			this.fluid = fluid;
			this.output = output;
			this.duration = duration;
			this.consumption = consumption;
		}

		public ArcWelderRecipe(ItemStack output, int duration, long consumption, AStack... ingredients) {
			this(output, duration, consumption, null, ingredients);
		}
	}
}
