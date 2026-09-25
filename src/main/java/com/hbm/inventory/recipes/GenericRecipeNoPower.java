package com.hbm.inventory.recipes;

import com.hbm.inventory.recipes.loader.GenericRecipe;

/** Generic recipe of machines that don't use power (blast furnace), only the duration counts */
public class GenericRecipeNoPower extends GenericRecipe {

	public GenericRecipeNoPower(String name) {
		super(name);
	}
}
