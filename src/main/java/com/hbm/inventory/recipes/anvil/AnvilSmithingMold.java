package com.hbm.inventory.recipes.anvil;

import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMold;

import net.minecraft.world.item.ItemStack;

/**
 * Pressing a blank mold into a shape: any item of the shape (every ingot for the ingot mold, in the given count) or
 * one of the given stacks works as the template. Only the blank mold is used up.
 */
public class AnvilSmithingMold extends AnvilSmithingRecipe {

	/** Shape prefix ("ingot", "plateTriple"...) and count, or the template stacks */
	private final String prefix;
	private final int count;
	private final ItemStack[] matchesStack;

	public AnvilSmithingMold(int moldId, AStack demo, String prefix, int count) {
		super(1, ItemMold.stack(moldId), demo, new ComparableStack(ModItems.mold_base.get()));
		this.prefix = prefix;
		this.count = count;
		this.matchesStack = null;
	}

	public AnvilSmithingMold(int moldId, AStack demo, ItemStack... templates) {
		super(1, ItemMold.stack(moldId), demo, new ComparableStack(ModItems.mold_base.get()));
		this.prefix = null;
		this.count = 0;
		this.matchesStack = templates;
	}

	@Override
	public boolean matches(ItemStack left, ItemStack right) {
		if(!doesStackMatch(right, this.right)) return false;

		// the original checked the ore dictionary names of the item, here the shape's parent tag (c:ingots...)
		if(prefix != null && left.getCount() == count && left.is(OreDictManager.shapeTag(prefix))) {
			return true;
		}

		if(matchesStack != null) {
			for(ItemStack stack : matchesStack) {
				if(ItemStack.isSameItem(left, stack) && left.getCount() == stack.getCount()) return true;
			}
		}

		return false;
	}

	@Override
	public int matchesInt(ItemStack left, ItemStack right) {
		return matches(left, right) ? 0 : -1;
	}

	/** The template stays, only the blank mold is used */
	@Override
	public int amountConsumed(int index, boolean mirrored) {
		return index;
	}
}
