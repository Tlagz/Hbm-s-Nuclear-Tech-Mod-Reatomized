package com.hbm.items.machine;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Infinite battery, machines with a battery slot fill up completely from it */
public class ItemBatteryCreative extends Item implements IBatteryItem {

	public ItemBatteryCreative(Properties properties) {
		super(properties);
	}

	@Override public void chargeBattery(ItemStack stack, long i) { }
	@Override public void setCharge(ItemStack stack, long i) { }
	@Override public void dischargeBattery(ItemStack stack, long i) { }

	@Override public long getCharge(ItemStack stack) { return Long.MAX_VALUE / 2L; }
	@Override public long getMaxCharge(ItemStack stack) { return Long.MAX_VALUE; }

	@Override public long getChargeRate(ItemStack stack) { return Long.MAX_VALUE / 100L; }
	@Override public long getDischargeRate(ItemStack stack) { return Long.MAX_VALUE / 100L; }
}
