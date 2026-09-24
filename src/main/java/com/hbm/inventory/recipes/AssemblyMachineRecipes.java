package com.hbm.inventory.recipes;

import com.hbm.inventory.recipes.gen.GenAssemblyMachineRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;

/**
 * Assembly machine recipes. The recipe list is translated from the original by tools/gen_assembly.py
 * (GenAssemblyMachineRecipes), recipes with items that aren't ported yet are left out.
 *
 * TODO the fluid package recipes (fluid_pack_full isn't ported), recipe config file, JEI
 */
public class AssemblyMachineRecipes extends GenericRecipes<GenericRecipe> {

	public static final AssemblyMachineRecipes INSTANCE = new AssemblyMachineRecipes();

	@Override public int inputItemLimit() { return 12; }
	@Override public int inputFluidLimit() { return 1; }
	@Override public int outputItemLimit() { return 1; }
	@Override public int outputFluidLimit() { return 1; }

	@Override public GenericRecipe instantiateRecipe(String name) { return new GenericRecipe(name); }

	@Override
	public void registerDefaults() {
		GenAssemblyMachineRecipes.register(this);
	}
}
