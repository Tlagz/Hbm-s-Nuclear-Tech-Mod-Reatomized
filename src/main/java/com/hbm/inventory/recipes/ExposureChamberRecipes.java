package com.hbm.inventory.recipes;

import static com.hbm.inventory.OreDictManager.*;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.items.ModItems;

import net.minecraft.world.item.ItemStack;

/**
 * Exposure chamber: an ingot bombarded with the particles of one capsule (8 uses) turns into something exotic.
 *
 * TODO the schraranium recipe (the ingot isn't ported yet), expensive mode, recipe config file, JEI
 */
public class ExposureChamberRecipes {

	public static final List<ExposureChamberRecipe> recipes = new ArrayList<>();

	public static void registerDefaults() {
		recipes.clear();
		recipes.add(new ExposureChamberRecipe(new ComparableStack(ModItems.particle_higgs.get()), new OreDictStack(U238.ingot()), new ItemStack(ModItems.ingot_schrabidium.get())));
		recipes.add(new ExposureChamberRecipe(new ComparableStack(ModItems.particle_dark.get()), new OreDictStack(PU.ingot()), new ItemStack(ModItems.ingot_euphemium.get())));
		recipes.add(new ExposureChamberRecipe(new ComparableStack(ModItems.particle_sparkticle.get()), new OreDictStack(SBD.ingot()), new ItemStack(ModItems.ingot_dineutronium.get())));
	}

	public static ExposureChamberRecipe getRecipe(ItemStack particle, ItemStack input) {
		for(ExposureChamberRecipe recipe : recipes) if(recipe.particle.matchesRecipe(particle, true) && recipe.ingredient.matchesRecipe(input, true)) return recipe;
		return null;
	}

	public static class ExposureChamberRecipe {

		public AStack particle;
		public AStack ingredient;
		public ItemStack output;

		public ExposureChamberRecipe(AStack particle, AStack ingredient, ItemStack output) {
			this.particle = particle;
			this.ingredient = ingredient;
			this.output = output;
		}
	}
}
