package com.hbm.items;

import java.util.List;

import net.minecraft.world.item.ItemStack;

/**
 * Items that show up in the creative tab as several stacks, the original's getSubItems for items with
 * subtypes whose "metadata" is now a data component (e.g. one fluid tank per fluid).
 */
public interface ISubItems {

	List<ItemStack> getSubItems();
}
