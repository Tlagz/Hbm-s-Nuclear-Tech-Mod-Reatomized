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
 * Cyclotron recipes: a particle item plus an input make an output and some antimatter.
 * Translated from the original's registerDefaults; the JSON override file isn't supported (yet).
 */
public class CyclotronRecipes {

	public record Key(ComparableStack particle, AStack input) { }
	public record Result(ItemStack output, int antimatter) { }
	public record Recipe(Key key, Result result) { }

	/** A list, the original's HashMap had identity keys so recipes with equal inputs all stayed in */
	public static List<Recipe> recipes = new ArrayList<>();

	public static void registerDefaults() {


		/// LITHIUM START ///
		int liA = 50;

		makeRecipe(new ComparableStack(ModItems.part_lithium.get()), new OreDictStack("dustLithium"), new ItemStack(ModItems.powder_beryllium.get()), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium.get()), new OreDictStack("dustBeryllium"), new ItemStack(ModItems.powder_boron.get()), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium.get()), new OreDictStack("dustBoron"), new ItemStack(ModItems.powder_coal.get()), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium.get()), new OreDictStack("dustNetherQuartz"), new ItemStack(ModItems.powder_fire.get()), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium.get()), new OreDictStack("dustPhosphorus"), new ItemStack(ModItems.sulfur.get()), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium.get()), new OreDictStack("dustIron"), new ItemStack(ModItems.powder_cobalt.get()), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium.get()), new ComparableStack(ModItems.powder_strontium.get()), new ItemStack(ModItems.powder_zirconium.get()), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium.get()), new OreDictStack("dustGold"), new ItemStack(ModItems.ingot_mercury.get()), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium.get()), new OreDictStack("dustPolonium"), new ItemStack(ModItems.powder_astatine.get()), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium.get()), new OreDictStack("dustLanthanium"), new ItemStack(ModItems.powder_cerium.get()), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium.get()), new OreDictStack("dustActinium"), new ItemStack(ModItems.powder_thorium.get()), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium.get()), new OreDictStack(U.dust()), new ItemStack(ModItems.powder_neptunium.get()), liA);
		makeRecipe(new ComparableStack(ModItems.part_lithium.get()), new OreDictStack(NP237.dust()), new ItemStack(ModItems.powder_plutonium.get()), liA);
		/// LITHIUM END ///

		/// BERYLLIUM START ///
		int beA = 25;

		makeRecipe(new ComparableStack(ModItems.part_beryllium.get()), new OreDictStack("dustLithium"), new ItemStack(ModItems.powder_boron.get()), beA);
		makeRecipe(new ComparableStack(ModItems.part_beryllium.get()), new OreDictStack("dustNetherQuartz"), new ItemStack(ModItems.sulfur.get()), beA);
		makeRecipe(new ComparableStack(ModItems.part_beryllium.get()), new OreDictStack("dustTitanium"), new ItemStack(ModItems.powder_iron.get()), beA);
		makeRecipe(new ComparableStack(ModItems.part_beryllium.get()), new OreDictStack("dustCobalt"), new ItemStack(ModItems.powder_copper.get()), beA);
		makeRecipe(new ComparableStack(ModItems.part_beryllium.get()), new ComparableStack(ModItems.powder_strontium.get()), new ItemStack(ModItems.powder_niobium.get()), beA);
		makeRecipe(new ComparableStack(ModItems.part_beryllium.get()), new ComparableStack(ModItems.powder_cerium.get()), new ItemStack(ModItems.powder_neodymium.get()), beA);
		makeRecipe(new ComparableStack(ModItems.part_beryllium.get()), new OreDictStack("dustThorium"), new ItemStack(ModItems.powder_uranium.get()), beA);
		/// BERYLLIUM END ///
		
		/// CARBON START ///
		int caA = 10;

		makeRecipe(new ComparableStack(ModItems.part_carbon.get()), new OreDictStack("dustBoron"), new ItemStack(ModItems.powder_aluminium.get()), caA);
		makeRecipe(new ComparableStack(ModItems.part_carbon.get()), new OreDictStack("dustSulfur"), new ItemStack(ModItems.powder_titanium.get()), caA);
		makeRecipe(new ComparableStack(ModItems.part_carbon.get()), new OreDictStack("dustTitanium"), new ItemStack(ModItems.powder_cobalt.get()), caA);
		makeRecipe(new ComparableStack(ModItems.part_carbon.get()), new ComparableStack(ModItems.powder_caesium.get()), new ItemStack(ModItems.powder_lanthanium.get()), caA);
		makeRecipe(new ComparableStack(ModItems.part_carbon.get()), new ComparableStack(ModItems.powder_neodymium.get()), new ItemStack(ModItems.powder_gold.get()), caA);
		makeRecipe(new ComparableStack(ModItems.part_carbon.get()), new ComparableStack(ModItems.ingot_mercury.get()), new ItemStack(ModItems.powder_polonium.get()), caA);
		makeRecipe(new ComparableStack(ModItems.part_carbon.get()), new OreDictStack(PB.dust()), new ItemStack(ModItems.powder_ra226.get()),caA);
		makeRecipe(new ComparableStack(ModItems.part_carbon.get()), new ComparableStack(ModItems.powder_astatine.get()), new ItemStack(ModItems.powder_actinium.get()), caA);
		/// CARBON END ///
		
		/// COPPER START ///
		int coA = 15;
		
		makeRecipe(new ComparableStack(ModItems.part_copper.get()), new OreDictStack("dustBeryllium"), new ItemStack(ModItems.powder_quartz.get()), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper.get()), new OreDictStack("dustCoal"), new ItemStack(ModItems.powder_bromine.get()), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper.get()), new OreDictStack("dustTitanium"), new ItemStack(ModItems.powder_strontium.get()), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper.get()), new OreDictStack("dustIron"), new ItemStack(ModItems.powder_niobium.get()), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper.get()), new ComparableStack(ModItems.powder_bromine.get()), new ItemStack(ModItems.powder_iodine.get()), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper.get()), new ComparableStack(ModItems.powder_strontium.get()), new ItemStack(ModItems.powder_neodymium.get()), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper.get()), new ComparableStack(ModItems.powder_niobium.get()), new ItemStack(ModItems.powder_caesium.get()), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper.get()), new ComparableStack(ModItems.powder_iodine.get()), new ItemStack(ModItems.powder_polonium.get()), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper.get()), new ComparableStack(ModItems.powder_caesium.get()), new ItemStack(ModItems.powder_actinium.get()), coA);
		makeRecipe(new ComparableStack(ModItems.part_copper.get()), new OreDictStack("dustGold"), new ItemStack(ModItems.powder_uranium.get()), coA);
		/// COPPER END ///

		/// PLUTONIUM START ///
		int plA = 100;
		
		makeRecipe(new ComparableStack(ModItems.part_plutonium.get()), new OreDictStack("dustPhosphorus"), new ItemStack(ModItems.powder_tennessine.get()), plA);
		makeRecipe(new ComparableStack(ModItems.part_plutonium.get()), new OreDictStack(PU.dust()), new ItemStack(ModItems.powder_tennessine.get()), plA);
		makeRecipe(new ComparableStack(ModItems.part_plutonium.get()), new ComparableStack(ModItems.powder_tennessine.get()), new ItemStack(ModItems.powder_australium.get()), plA);
		makeRecipe(new ComparableStack(ModItems.part_plutonium.get()), new ComparableStack(ModItems.pellet_charged.get()), new ItemStack(ModItems.nugget_schrabidium.get()), 1000);
		/// PLUTONIUM END ///
	}

	private static void makeRecipe(ComparableStack part, AStack in, ItemStack out, int amat) {
		recipes.add(new Recipe(new Key(part, in), new Result(out, amat)));
	}

	/** Output copy and antimatter for the input and particle, null if nothing matches */
	public static Result getOutput(ItemStack stack, ItemStack box) {

		if(stack == null || stack.isEmpty() || box == null || box.isEmpty()) return null;

		//boo hoo we iterate over a hash map, cry me a river
		for(Recipe recipe : recipes) {

			if(recipe.key().particle().matchesRecipe(box, true) && recipe.key().input().matchesRecipe(stack, true)) {
				return new Result(recipe.result().output().copy(), recipe.result().antimatter());
			}
		}

		return null;
	}
}
