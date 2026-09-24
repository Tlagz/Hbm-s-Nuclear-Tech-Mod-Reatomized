package com.hbm.items.machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Press stamps, the durability is the number of operations (0 = unbreakable, desh stamps) */
public class ItemStamp extends Item {

	protected StampType type;
	public static final HashMap<StampType, List<ItemStack>> stamps = new HashMap<>();

	public ItemStamp(Properties properties, int dura, StampType type) {
		super(dura > 0 ? properties.durability(dura) : properties.stacksTo(1));
		this.type = type;

		if(type != null) {
			stamps.computeIfAbsent(type, k -> new ArrayList<>()).add(new ItemStack(this));
		}
	}

	public StampType getStampType(ItemStack stack) {
		return type;
	}

	public static enum StampType {
		FLAT,
		PLATE,
		WIRE,
		CIRCUIT,
		C357,
		C44,
		C50,
		C9,
		PRINTING1,
		PRINTING2,
		PRINTING3,
		PRINTING4,
		PRINTING5,
		PRINTING6,
		PRINTING7,
		PRINTING8;
	}
}
