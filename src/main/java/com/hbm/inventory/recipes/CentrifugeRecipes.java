package com.hbm.inventory.recipes;

import java.util.HashMap;
import java.util.Map.Entry;

import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.recipes.gen.GenCentrifugeRecipes;
import com.hbm.main.MainRegistry;

import net.minecraft.world.item.ItemStack;

/**
 * Centrifuge recipes: one input, up to four outputs. The table is translated from the original by
 * tools/gen_generic.py (GenCentrifugeRecipes).
 *
 * TODO bedrock ore processing loops (ore_bedrock etc. not ported), JSON config (SerializableRecipe), IMC, JEI
 */
public class CentrifugeRecipes {

	public static HashMap<AStack, ItemStack[]> recipes = new HashMap<>();

	public static void registerDefaults() {
		recipes.clear();
		GenCentrifugeRecipes.register();
		MainRegistry.logger.info("Centrifuge recipes: " + recipes.size());
	}

	/** Copies of the outputs for the stack, null if there's no recipe */
	public static ItemStack[] getOutput(ItemStack stack) {

		if(stack == null || stack.isEmpty())
			return null;

		ComparableStack comp = new ComparableStack(stack).makeSingular();

		if(recipes.containsKey(comp))
			return copy(recipes.get(comp));

		for(Entry<AStack, ItemStack[]> entry : recipes.entrySet()) {
			if(entry.getKey().matchesRecipe(stack, true)) {
				return copy(entry.getValue());
			}
		}

		return null;
	}

	private static ItemStack[] copy(ItemStack[] stacks) {
		ItemStack[] copy = new ItemStack[stacks.length];
		for(int i = 0; i < stacks.length; i++) copy[i] = stacks[i] == null ? ItemStack.EMPTY : stacks[i].copy();
		return copy;
	}
}
