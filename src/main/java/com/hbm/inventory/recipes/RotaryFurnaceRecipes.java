package com.hbm.inventory.recipes;

import static com.hbm.inventory.OreDictManager.*;
import static com.hbm.inventory.material.MaterialShapes.INGOT;
import static com.hbm.inventory.material.Mats.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;

import net.minecraft.world.item.ItemStack;

/**
 * Rotary furnace recipes: up to three items and an optional fluid into molten metal, powered by steam and fuel.
 *
 * TODO JSON config, JEI
 */
public class RotaryFurnaceRecipes {

	public static List<RotaryFurnaceRecipe> recipes = new ArrayList<>();

	public static void registerDefaults() {
		recipes.clear();

		recipes.add(new RotaryFurnaceRecipe(new MaterialStack(MAT_STEEL, INGOT.q(1)), 100, 100, new OreDictStack(IRON.ingot()), new OreDictStack(COAL.gem())));
		recipes.add(new RotaryFurnaceRecipe(new MaterialStack(MAT_STEEL, INGOT.q(1)), 100, 100, new OreDictStack(IRON.ingot()), new OreDictStack(ANY_COKE.gem())));

		recipes.add(new RotaryFurnaceRecipe(new MaterialStack(MAT_STEEL, INGOT.q(2)), 200, 25, new OreDictStack(IRON.fragment(), 9), new OreDictStack(COAL.gem())));
		recipes.add(new RotaryFurnaceRecipe(new MaterialStack(MAT_STEEL, INGOT.q(3)), 200, 25, new OreDictStack(IRON.fragment(), 9), new OreDictStack(ANY_COKE.gem())));
		recipes.add(new RotaryFurnaceRecipe(new MaterialStack(MAT_STEEL, INGOT.q(4)), 400, 25, new OreDictStack(IRON.fragment(), 9), new OreDictStack(ANY_COKE.gem()), new ComparableStack(ModItems.powder_flux.get())));

		recipes.add(new RotaryFurnaceRecipe(new MaterialStack(MAT_DESH, INGOT.q(1)), 100, 200, new FluidStack(Fluids.LIGHTOIL, 100), new ComparableStack(ModItems.powder_desh_ready.get())));

		recipes.add(new RotaryFurnaceRecipe(new MaterialStack(MAT_GUNMETAL, INGOT.q(4)), 200, 100, new OreDictStack(CU.ingot(), 3), new OreDictStack(AL.ingot(), 1)));
		recipes.add(new RotaryFurnaceRecipe(new MaterialStack(MAT_WEAPONSTEEL, INGOT.q(1)), 200, 400, new FluidStack(Fluids.GAS_COKER, 100), new OreDictStack(STEEL.ingot(), 1), new ComparableStack(ModItems.powder_flux.get(), 2)));
		recipes.add(new RotaryFurnaceRecipe(new MaterialStack(MAT_SATURN, INGOT.q(2)), 200, 400, new FluidStack(Fluids.REFORMGAS, 250), new OreDictStack(DURA.dust(), 4), new OreDictStack(CU.dust())));
		recipes.add(new RotaryFurnaceRecipe(new MaterialStack(MAT_SATURN, INGOT.q(4)), 200, 300, new FluidStack(Fluids.REFORMGAS, 250), new OreDictStack(DURA.dust(), 4), new OreDictStack(CU.dust()), new OreDictStack(BORAX.dust())));
		recipes.add(new RotaryFurnaceRecipe(new MaterialStack(MAT_ALUMINIUM, INGOT.q(2)), 100, 400, new FluidStack(Fluids.SODIUM_ALUMINATE, 150)));
		recipes.add(new RotaryFurnaceRecipe(new MaterialStack(MAT_ALUMINIUM, INGOT.q(3)), 40, 200, new FluidStack(Fluids.SODIUM_ALUMINATE, 150), new ComparableStack(ModItems.powder_flux.get(), 2)));

		MainRegistry.logger.info("Rotary furnace recipes: " + recipes.size());
	}

	/** The first recipe whose ingredients are all present, each filled slot has to match a different ingredient */
	public static RotaryFurnaceRecipe getRecipe(ItemStack... inputs) {

		outer:
		for(RotaryFurnaceRecipe recipe : recipes) {

			List<AStack> recipeList = new ArrayList<>(List.of(recipe.ingredients));

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

					if(!hasMatch) continue outer;
				}
			}

			if(recipeList.isEmpty()) return recipe;
		}

		return null;
	}

	public static class RotaryFurnaceRecipe {

		public AStack[] ingredients;
		public FluidStack fluid;
		public MaterialStack output;
		public int duration;
		public int steam;

		public RotaryFurnaceRecipe(MaterialStack output, int duration, int steam, FluidStack fluid, AStack... ingredients) {
			this.ingredients = ingredients;
			this.fluid = fluid;
			this.output = output;
			this.duration = duration;
			this.steam = steam;
		}

		public RotaryFurnaceRecipe(MaterialStack output, int duration, int steam, AStack... ingredients) {
			this(output, duration, steam, null, ingredients);
		}
	}
}
