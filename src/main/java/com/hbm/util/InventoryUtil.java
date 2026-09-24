package com.hbm.util;

import java.util.List;

import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.recipes.anvil.AnvilRecipes.AnvilOutput;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class InventoryUtil {

	/**
	 * Whether the player's main inventory holds all the ingredients (with their amounts), optionally removing them.
	 * Works on a copy first so nothing is taken unless everything is there.
	 */
	public static boolean doesPlayerHaveAStacks(Player player, List<AStack> stacks, boolean shouldRemove) {

		List<ItemStack> original = player.getInventory().items;
		ItemStack[] inventory = new ItemStack[original.size()];
		AStack[] input = new AStack[stacks.size()];

		//first we copy the inputs into an array because 1. it's easier to deal with and 2. we can dick around with the stack sized with no repercussions
		for(int i = 0; i < input.length; i++) {
			input[i] = stacks.get(i).copy();
		}

		//then we copy the inventory so we can dick around with it as well without making actual modifications to the player's inventory
		for(int i = 0; i < inventory.length; i++) {
			inventory[i] = original.get(i).copy();
		}

		//now we go through every ingredient...
		for(int i = 0; i < input.length; i++) {

			AStack stack = input[i];

			//...and compare each ingredient to every stack in the inventory
			for(int j = 0; j < inventory.length; j++) {

				ItemStack inv = inventory[j];

				//we check if it matches but ignore stack size for now
				if(!inv.isEmpty() && stack.matchesRecipe(inv, true)) {
					//and NOW we care about the stack size
					int size = Math.min(stack.stacksize, inv.getCount());
					stack.stacksize -= size;
					inv.shrink(size);

					//spent stacks are removed from the equation so that we don't cross ourselves later on
					if(stack.stacksize <= 0) {
						input[i] = null;
						break;
					}
				}
			}
		}

		for(AStack stack : input) {
			if(stack != null) {
				return false;
			}
		}

		if(shouldRemove) {
			for(int i = 0; i < inventory.length; i++) {
				original.set(i, inventory[i].isEmpty() ? ItemStack.EMPTY : inventory[i]);
			}
		}

		return true;
	}

	public static void giveChanceStacksToPlayer(Player player, List<AnvilOutput> stacks) {

		for(AnvilOutput out : stacks) {
			if(out.chance == 1.0F || player.getRandom().nextFloat() < out.chance) {
				if(!player.getInventory().add(out.stack.copy())) {
					player.drop(out.stack.copy(), false);
				}
			}
		}
	}

	/** How many items of the player's inventory match (for the anvil's ingredient list) */
	public static int countMatches(Player player, AStack stack) {
		int amount = 0;
		for(ItemStack inv : player.getInventory().items) {
			if(!inv.isEmpty() && stack.matchesRecipe(inv, true)) amount += inv.getCount();
		}
		return amount;
	}
}
