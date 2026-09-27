package com.hbm.inventory.recipes;

import com.hbm.inventory.recipes.gen.GenPUREXRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;

/**
 * PUREX recipes: fuel reprocessing and vitrification, up to 3 items and 3 fluids in, 6 items and 1 fluid out.
 * Translated from the original by tools/gen_generic.py (GenPUREXRecipes), recipes with items that aren't ported yet
 * are left out. The original's PUREXRecipe subclass only changed the NEI display, here they're plain GenericRecipes.
 *
 * TODO recipe config file, JEI, the recipes generated in loops
 */
public class PUREXRecipes extends GenericRecipes<GenericRecipe> {

	public static final PUREXRecipes INSTANCE = new PUREXRecipes();

	@Override public int inputItemLimit() { return 3; }
	@Override public int inputFluidLimit() { return 3; }
	@Override public int outputItemLimit() { return 6; }
	@Override public int outputFluidLimit() { return 1; }

	@Override public GenericRecipe instantiateRecipe(String name) { return new GenericRecipe(name); }

	@Override
	public void registerDefaults() {
		GenPUREXRecipes.register(this);
	}
}
