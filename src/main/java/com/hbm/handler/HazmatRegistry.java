package com.hbm.handler;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Radiation resistance of armor pieces. Resistance is a coefficient, the applied radiation is multiplied with 10^-resistance.
 *
 * TODO register the armor sets once they are ported (initDefault in the original), cladding mods,
 *  RadAway-X potion bonus, hbmRadResist.json config
 */
public class HazmatRegistry {

	public static double helmet = 0.2D;
	public static double chest = 0.4D;
	public static double legs = 0.3D;
	public static double boots = 0.1D;

	// assuming coefficient of 10, real coefficient turned out to be 5, oops
	public static final double iron = 0.0225D; // 5%
	public static final double gold = 0.0225D; // 5%
	public static final double steel = 0.045D; // 10%
	public static final double titanium = 0.045D; // 10%
	public static final double alloy = 0.07D; // 15%
	public static final double cobalt = 0.125D; // 25%
	public static final double hazYellow = 0.6D; // 50%
	public static final double hazRed = 1.0D; // 90%
	public static final double hazGray = 2D; // 99%
	public static final double paa = 1.7D; // 97%
	public static final double liquidator = 2.4D; // 99.6%
	public static final double security = 0.825D; // 85%
	public static final double star = 1D; // 90%
	public static final double cmb = 1.3D; // 95%
	public static final double schrab = 3D; // 99.9%
	public static final double euph = 10D; // <100%

	private static final Map<Item, Double> entries = new HashMap<>();

	public static void registerHazmat(Item item, double resistance) {
		entries.put(item, resistance);
	}

	public static double getResistance(ItemStack stack) {
		if(stack.isEmpty()) return 0;
		Double f = entries.get(stack.getItem());
		return f != null ? f : 0;
	}

	public static float getResistance(Player player) {
		float res = 0.0F;

		for(EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
			res += (float) getResistance(player.getItemBySlot(slot));
		}

		return res;
	}
}
