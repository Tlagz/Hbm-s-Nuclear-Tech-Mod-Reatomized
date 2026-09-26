package com.hbm.inventory.recipes;

import java.util.HashMap;
import java.util.Map.Entry;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.gen.GenCrystallizerRecipes;
import com.hbm.main.MainRegistry;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Crystallizer (ore acidizer) recipes: an item and an acid (peroxide unless stated) into one output, some with a
 * productivity bonus. Translated from the original by tools/gen_generic.py (GenCrystallizerRecipes).
 *
 * TODO the bedrock ore loops and the chemical dye loop, compat recipes, JSON config, JEI
 */
public class CrystallizerRecipes {

	private record Key(AStack input, FluidType fluid) { }

	private static HashMap<Key, CrystallizerRecipe> recipes = new HashMap<>();
	private static HashMap<AStack, Integer> amounts = new HashMap<>(); // for use in the partitioner

	public static void registerDefaults() {
		recipes.clear();
		amounts.clear();
		GenCrystallizerRecipes.register();
		MainRegistry.logger.info("Crystallizer recipes: " + recipes.size());
	}

	public static void registerRecipe(AStack input, CrystallizerRecipe recipe) {
		registerRecipe(input, recipe, new FluidStack(Fluids.PEROXIDE, 500));
	}

	public static void registerRecipe(AStack input, CrystallizerRecipe recipe, FluidStack stack) {
		recipe.acidAmount = stack.fill;
		recipes.put(new Key(input, stack.type), recipe);
		amounts.put(input, recipe.itemAmount);
	}

	/** The recipe for the item with the acid: the exact item first, then its tags */
	public static CrystallizerRecipe getOutput(ItemStack stack, FluidType type) {

		if(stack == null || stack.isEmpty())
			return null;

		ComparableStack comp = new ComparableStack(stack).makeSingular();
		CrystallizerRecipe exact = recipes.get(new Key(comp, type));
		if(exact != null) return exact;

		for(Entry<Key, CrystallizerRecipe> entry : recipes.entrySet()) {
			if(entry.getKey().fluid() == type && entry.getKey().input() instanceof OreDictStack dict && dict.matchesRecipe(stack, true)) {
				return entry.getValue();
			}
		}

		return null;
	}

	/** How many of the item a recipe takes (for the partitioner) */
	public static int getAmount(ItemStack stack) {

		if(stack == null || stack.isEmpty()) return 0;

		ComparableStack comp = new ComparableStack(stack).makeSingular();
		Integer exact = amounts.get(comp);
		if(exact != null) return exact;

		for(Entry<AStack, Integer> entry : amounts.entrySet()) {
			if(entry.getKey() instanceof OreDictStack dict && dict.matchesRecipe(stack, true)) return entry.getValue();
		}

		return 0;
	}

	public static class CrystallizerRecipe {
		public int acidAmount;
		public int itemAmount = 1;
		public int duration;
		public float productivity = 0F;
		public ItemStack output;

		public CrystallizerRecipe(ItemLike output, int duration) { this(new ItemStack(output), duration); }

		public CrystallizerRecipe(ItemStack output, int duration) {
			this.output = output;
			this.duration = duration;
			this.acidAmount = 500;
		}

		public CrystallizerRecipe setReq(int amount) {
			this.itemAmount = amount;
			return this;
		}

		public CrystallizerRecipe prod(float productivity) {
			this.productivity = productivity;
			return this;
		}
	}
}
