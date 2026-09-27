package com.hbm.util;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.OreDictManager;

import net.minecraft.world.item.ItemStack;

/** Stack helpers of the original's ItemStackUtil */
public class ItemStackUtil {

	/** The ore dictionary keys of the stack, i.e. the registered keys whose item tag it's in, in registration order */
	public static List<String> getOreDictNames(ItemStack stack) {
		List<String> names = new ArrayList<>();
		if(stack.isEmpty()) return names;

		for(String key : OreDictManager.ENTRIES.keySet()) {
			if(stack.is(OreDictManager.tag(key))) names.add(key);
		}
		return names;
	}
}
