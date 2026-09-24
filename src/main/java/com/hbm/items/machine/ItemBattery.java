package com.hbm.items.machine;

import java.util.List;

import com.hbm.items.ModDataComponents;
import com.hbm.util.BobMathUtil;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Rechargeable batteries. Stacks without a charge component count as full, like in the original. */
public class ItemBattery extends Item implements IBatteryItem {

	protected long maxCharge;
	protected long chargeRate;
	protected long dischargeRate;

	public ItemBattery(Properties properties, long dura, long chargeRate, long dischargeRate) {
		super(properties);
		this.maxCharge = dura;
		this.chargeRate = chargeRate;
		this.dischargeRate = dischargeRate;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		long charge = getCharge(stack);
		list.add(Component.literal("Energy stored: " + BobMathUtil.getShortNumber(charge) + "/" + BobMathUtil.getShortNumber(maxCharge) + "HE"));
		list.add(Component.literal("Charge rate: " + BobMathUtil.getShortNumber(chargeRate) + "HE/t"));
		list.add(Component.literal("Discharge rate: " + BobMathUtil.getShortNumber(dischargeRate) + "HE/t"));
	}

	@Override
	public void chargeBattery(ItemStack stack, long i) {
		setCharge(stack, getCharge(stack) + i);
	}

	@Override
	public void setCharge(ItemStack stack, long i) {
		stack.set(ModDataComponents.CHARGE.get(), i);
	}

	@Override
	public void dischargeBattery(ItemStack stack, long i) {
		setCharge(stack, getCharge(stack) - i);
	}

	@Override
	public long getCharge(ItemStack stack) {
		return stack.getOrDefault(ModDataComponents.CHARGE.get(), maxCharge);
	}

	@Override
	public long getMaxCharge(ItemStack stack) {
		return maxCharge;
	}

	@Override
	public long getChargeRate(ItemStack stack) {
		return chargeRate;
	}

	@Override
	public long getDischargeRate(ItemStack stack) {
		return dischargeRate;
	}

	public static ItemStack getEmptyBattery(Item item) {
		return IBatteryItem.emptyBattery(item);
	}

	public static ItemStack getFullBattery(Item item) {
		ItemStack stack = new ItemStack(item);
		if(item instanceof ItemBattery battery) battery.setCharge(stack, battery.maxCharge);
		return stack;
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return true;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return Math.round(13F * getCharge(stack) / Math.max(1F, getMaxCharge(stack)));
	}

	@Override
	public int getBarColor(ItemStack stack) {
		float f = Math.max(0.0F, (float) getCharge(stack) / Math.max(1F, getMaxCharge(stack)));
		return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
	}
}
