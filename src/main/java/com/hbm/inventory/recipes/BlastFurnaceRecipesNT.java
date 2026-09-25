package com.hbm.inventory.recipes;

import com.hbm.inventory.recipes.gen.GenBlastFurnaceRecipesNT;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;

import net.minecraft.world.item.ItemStack;

/**
 * Blast furnace recipes: one or two inputs (any order), one or two outputs (steel and slag), no power.
 * Translated from the original by tools/gen_generic.py (GenBlastFurnaceRecipesNT).
 *
 * TODO recipe config file, JEI
 */
public class BlastFurnaceRecipesNT extends GenericRecipes<GenericRecipeNoPower> {

	public static final BlastFurnaceRecipesNT INSTANCE = new BlastFurnaceRecipesNT();

	@Override public int inputItemLimit() { return 2; }
	@Override public int inputFluidLimit() { return 0; }
	@Override public int outputItemLimit() { return 2; }
	@Override public int outputFluidLimit() { return 0; }
	@Override public boolean hasPower() { return false; }

	@Override
	public GenericRecipeNoPower instantiateRecipe(String name) {
		return new GenericRecipeNoPower(name);
	}

	@Override
	public void registerDefaults() {
		GenBlastFurnaceRecipesNT.register(this);
	}

	public GenericRecipe getRecipe(ItemStack s0, ItemStack s1) {

		for(GenericRecipe recipe : this.recipeOrderedList) {
			if(recipe.inputItem.length == 1) {
				if(!s0.isEmpty() && s1.isEmpty() && recipe.inputItem[0].matchesRecipe(s0, false)) return recipe;
				if(s0.isEmpty() && !s1.isEmpty() && recipe.inputItem[0].matchesRecipe(s1, false)) return recipe;
			}
			if(recipe.inputItem.length == 2 && !s0.isEmpty() && !s1.isEmpty()) {
				if(recipe.inputItem[0].matchesRecipe(s0, true) && recipe.inputItem[1].matchesRecipe(s1, false)) return recipe;
				if(recipe.inputItem[1].matchesRecipe(s0, true) && recipe.inputItem[0].matchesRecipe(s1, false)) return recipe;
			}
		}

		return null;
	}
}
