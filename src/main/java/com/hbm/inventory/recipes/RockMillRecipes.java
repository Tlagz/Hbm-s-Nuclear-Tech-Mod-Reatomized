package com.hbm.inventory.recipes;

import com.hbm.inventory.recipes.gen.GenRockMillRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;

/**
 * Rock mill recipes: crushing stone into gravel, sand and some ore dust (random outputs), with water turning into
 * colloid. Translated from the original by tools/gen_generic.py (GenRockMillRecipes).
 *
 * TODO recipe config file, JEI
 */
public class RockMillRecipes extends GenericRecipes<GenericRecipe> {

	public static final RockMillRecipes INSTANCE = new RockMillRecipes();

	@Override public int inputItemLimit() { return 3; }
	@Override public int inputFluidLimit() { return 1; }
	@Override public int outputItemLimit() { return 3; }
	@Override public int outputFluidLimit() { return 1; }

	@Override public GenericRecipe instantiateRecipe(String name) { return new GenericRecipe(name); }

	@Override
	public void registerDefaults() {
		GenRockMillRecipes.register(this);
	}
}
