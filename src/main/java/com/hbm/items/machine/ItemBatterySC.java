package com.hbm.items.machine;

import java.util.List;

import com.hbm.items.ItemEnumMulti;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Self-charging (radiovoltaic) batteries, always full, one item per isotope (ModItems.battery_sc) */
public class ItemBatterySC extends ItemEnumMulti implements IBatteryItem {

	public final EnumBatterySC pack;

	public ItemBatterySC(Properties properties, String descriptionId, EnumBatterySC pack) {
		super(properties.stacksTo(1), descriptionId);
		this.pack = pack;
	}

	public static enum EnumBatterySC {

		EMPTY(	    0),
		WASTE(	  150),
		RA226(	  200),
		TC99(	  500),
		CO60(	  750),
		PU238(	1_000),
		PO210(	1_250),
		AU198(	1_500),
		PB209(	2_000),
		AM241(	2_500);

		public long power;

		private EnumBatterySC(long power) {
			this.power = power;
		}
	}

	@Override public void chargeBattery(ItemStack stack, long i) { }
	@Override public void setCharge(ItemStack stack, long i) { }
	@Override public void dischargeBattery(ItemStack stack, long i) { }
	@Override public long getChargeRate(ItemStack stack) { return 0; }

	@Override public long getCharge(ItemStack stack) { return getMaxCharge(stack); }
	@Override public long getDischargeRate(ItemStack stack) { return getMaxCharge(stack); }

	@Override
	public long getMaxCharge(ItemStack stack) {
		return pack.power;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		if(pack.power > 0) list.add(Component.literal("Discharge rate: " + BobMathUtil.getShortNumber(pack.power) + "HE/t").withStyle(ChatFormatting.YELLOW));

		for(String line : I18nUtil.resolveKeyArray("item.battery_sc.desc")) {
			list.add(Component.literal(line).withStyle(ChatFormatting.RED));
		}
	}
}
