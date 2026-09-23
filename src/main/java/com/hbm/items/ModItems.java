package com.hbm.items;

import java.util.function.Function;

import com.hbm.creativetabs.NtmTab;
import com.hbm.lib.RefStrings;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RefStrings.MODID);

	public static final DeferredItem<Item> ingot_uranium = register("ingot_uranium", NtmTab.PARTS);
	public static final DeferredItem<Item> ingot_titanium = register("ingot_titanium", NtmTab.PARTS);
	public static final DeferredItem<Item> ingot_steel = register("ingot_steel", NtmTab.PARTS);
	public static final DeferredItem<Item> nugget_uranium = register("nugget_uranium", NtmTab.PARTS);

	public static DeferredItem<Item> register(String name, NtmTab tab) {
		return register(name, Item::new, new Item.Properties(), tab);
	}

	public static <T extends Item> DeferredItem<T> register(String name, Function<Item.Properties, T> factory, Item.Properties props, NtmTab tab) {
		DeferredItem<T> item = ITEMS.registerItem(name, factory, props);
		if(tab != null) tab.add(item);
		return item;
	}
}
