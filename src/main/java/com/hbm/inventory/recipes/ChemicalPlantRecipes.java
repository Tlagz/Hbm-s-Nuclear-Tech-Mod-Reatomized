package com.hbm.inventory.recipes;

import com.hbm.inventory.recipes.gen.GenChemicalPlantRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;

/**
 * Chemical plant recipes: up to 3 items and 3 fluids in and out. The recipe list is translated from the original
 * by tools/gen_generic.py (GenChemicalPlantRecipes), recipes with items that aren't ported yet are left out.
 *
 * TODO recipe config file, JEI
 */
public class ChemicalPlantRecipes extends GenericRecipes<GenericRecipe> {

	public static final ChemicalPlantRecipes INSTANCE = new ChemicalPlantRecipes();

	@Override public int inputItemLimit() { return 3; }
	@Override public int inputFluidLimit() { return 3; }
	@Override public int outputItemLimit() { return 3; }
	@Override public int outputFluidLimit() { return 3; }

	@Override public GenericRecipe instantiateRecipe(String name) { return new GenericRecipe(name); }

	@Override
	public void registerDefaults() {
		GenChemicalPlantRecipes.register(this);
	}
}
