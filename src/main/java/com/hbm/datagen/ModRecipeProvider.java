package com.hbm.datagen;

import java.util.concurrent.CompletableFuture;

import com.hbm.crafting.RecipeBase;
import com.hbm.crafting.MaterialRecipes;
import com.hbm.crafting.RecipeSink;
import com.hbm.crafting.gen.GenArmorRecipes;
import com.hbm.crafting.gen.GenConsumableRecipes;
import com.hbm.crafting.gen.GenCraftingManager;
import com.hbm.crafting.gen.GenMineralRecipes;
import com.hbm.crafting.gen.GenPowderRecipes;
import com.hbm.crafting.gen.GenRodRecipes;
import com.hbm.crafting.gen.GenToolRecipes;
import com.hbm.crafting.gen.GenWeaponRecipes;
import com.hbm.inventory.OreDictManager;
import com.hbm.main.MainRegistry;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;

/**
 * Crafting recipes, translated from the original's CraftingManager and recipe classes by tools/gen_recipes.py,
 * in the same order as the original registered them.
 */
public class ModRecipeProvider extends RecipeProvider {

	public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void buildRecipes(RecipeOutput output) {
		OreDictManager.registerOres();

		RecipeSink sink = new RecipeSink(output);
		RecipeBase.sink = sink;

		GenCraftingManager.register();
		MaterialRecipes.register();
		GenMineralRecipes.register();
		GenRodRecipes.register();
		GenToolRecipes.register();
		GenArmorRecipes.register();
		GenWeaponRecipes.register();
		GenConsumableRecipes.register();
		GenPowderRecipes.register();

		RecipeBase.sink = null;
		MainRegistry.logger.info("Crafting recipes: " + sink.shaped + " shaped, " + sink.shapeless + " shapeless");
	}
}
