package com.hbm.items.machine;

import java.util.List;

import com.hbm.items.ModDataComponents;
import com.hbm.lib.RefStrings;
import com.hbm.util.BobMathUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Battery packs and capacitors for the battery socket, one item per type (ModItems.battery_pack). Rendered as the
 * socket's battery or capacitor model with the type's texture. Unlike the old batteries they start out empty.
 */
public class ItemBatteryPack extends ItemBattery implements com.hbm.items.ISubItems {

	private final String descriptionId;
	public final EnumBatteryPack pack;

	public ItemBatteryPack(Properties properties, String descriptionId, EnumBatteryPack pack) {
		super(properties.stacksTo(1), pack.capacity, pack.chargeRate, pack.dischargeRate);
		this.descriptionId = descriptionId;
		this.pack = pack;
	}

	public static enum EnumBatteryPack {
		BATTERY_REDSTONE	("battery_redstone",	      100L, false),
		BATTERY_LEAD		("battery_lead",		    1_000L, false),
		BATTERY_LITHIUM		("battery_lithium",		   10_000L, false),
		BATTERY_SODIUM		("battery_sodium",		   50_000L, false),
		BATTERY_SCHRABIDIUM	("battery_schrabidium",	  250_000L, false),
		BATTERY_QUANTUM		("battery_quantum",		1_000_000L, 20 * 60 * 60),

		CAPACITOR_COPPER	("capacitor_copper",	     1_000L, true),
		CAPACITOR_GOLD		("capacitor_gold",		    10_000L, true),
		CAPACITOR_NIOBIUM	("capacitor_niobium",	   100_000L, true),
		CAPACITOR_TANTALUM	("capacitor_tantalum",	   500_000L, true),
		CAPACITOR_BISMUTH	("capacitor_bismuth",	 2_500_000L, true),
		CAPACITOR_SPARK		("capacitor_spark",		10_000_000L, true);

		public ResourceLocation texture;
		public long capacity;
		public long chargeRate;
		public long dischargeRate;

		private EnumBatteryPack(String tex, long dischargeRate, boolean capacitor) {
			this(tex,
					capacitor ? (dischargeRate * 20 * 30) : (dischargeRate * 20 * 60 * 15),
					capacitor ? dischargeRate : dischargeRate * 10,
					dischargeRate);
		}

		private EnumBatteryPack(String tex, long dischargeRate, long duration) {
			this(tex, dischargeRate * duration, dischargeRate * 10, dischargeRate);
		}

		private EnumBatteryPack(String tex, long capacity, long chargeRate, long dischargeRate) {
			this.texture = RefStrings.loc("textures/models/machines/" + tex + ".png");
			this.capacity = capacity;
			this.chargeRate = chargeRate;
			this.dischargeRate = dischargeRate;
		}

		public boolean isCapacitor() { return this.ordinal() > BATTERY_QUANTUM.ordinal(); }
	}

	@Override
	public String getDescriptionId() {
		return descriptionId;
	}

	/** Without a charge component the pack is empty (the original's getCharge created a tag with 0) */
	@Override
	public long getCharge(ItemStack stack) {
		return stack.getOrDefault(ModDataComponents.CHARGE.get(), 0L);
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return getCharge(stack) < getMaxCharge(stack);
	}

	/** An empty and a full one of each type, like the original's sub items */
	@Override
	public List<ItemStack> getSubItems() {
		ItemStack empty = new ItemStack(this);
		setCharge(empty, 0);
		ItemStack full = new ItemStack(this);
		setCharge(full, pack.capacity);
		return List.of(empty, full);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		long maxCharge = pack.capacity;
		long charge = stack.has(ModDataComponents.CHARGE.get()) ? getCharge(stack) : maxCharge;

		list.add(Component.literal("Energy stored: " + BobMathUtil.getShortNumber(charge) + "/" + BobMathUtil.getShortNumber(maxCharge) + "HE (" + (charge * 1000 / maxCharge / 10D) + "%)").withStyle(ChatFormatting.GREEN));
		list.add(Component.literal("Charge rate: " + BobMathUtil.getShortNumber(pack.chargeRate) + "HE/t").withStyle(ChatFormatting.YELLOW));
		list.add(Component.literal("Discharge rate: " + BobMathUtil.getShortNumber(pack.dischargeRate) + "HE/t").withStyle(ChatFormatting.YELLOW));
		list.add(Component.literal("Time for full charge: " + (maxCharge / pack.chargeRate / 20 / 60D) + "min").withStyle(ChatFormatting.GOLD));
		list.add(Component.literal("Charge lasts for: " + (maxCharge / pack.dischargeRate / 20 / 60D) + "min").withStyle(ChatFormatting.GOLD));
	}
}
