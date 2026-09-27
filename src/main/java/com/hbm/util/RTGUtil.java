package com.hbm.util;

import java.util.List;

import com.hbm.items.machine.ItemRTGPellet;

import net.minecraft.world.item.ItemStack;

public class RTGUtil {

	/** The original's MachineConfig defaults (VersatileConfig.rtgDecay / scaleRTGPower). TODO config */
	public static final boolean RTG_DECAY = true;
	public static final boolean SCALE_RTG_POWER = false;

	public static enum HalfLifeType {
		/** Counted in hundreds of years */
		LONG,
		/** Counted in years */
		MEDIUM,
		/** Counted in days */
		SHORT;
	}

	public static short getPower(ItemRTGPellet fuel, ItemStack stack) {
		return SCALE_RTG_POWER ? ItemRTGPellet.getScaledPower(fuel, stack) : fuel.getHeat();
	}

	public static boolean hasHeat(List<ItemStack> inventory, int[] rtgSlots) {
		for(int slot : rtgSlots) {
			if(inventory.get(slot).getItem() instanceof ItemRTGPellet) return true;
		}
		return false;
	}

	/** Sums up the heat of the pellets in the slots and decays them */
	public static int updateRTGs(List<ItemStack> inventory, int[] rtgSlots) {
		int newHeat = 0;
		for(int slot : rtgSlots) {
			if(!(inventory.get(slot).getItem() instanceof ItemRTGPellet pellet)) continue;
			newHeat += getPower(pellet, inventory.get(slot));
			inventory.set(slot, ItemRTGPellet.handleDecay(inventory.get(slot), pellet));
		}
		return newHeat;
	}

	/**
	 * Gets the lifespan of an RTG based on half-life
	 * @author UFFR
	 * @param halfLife The half-life
	 * @param type Half-life units
	 * @param realYears Whether or not to use 365 days per year instead of 100 to calculate time
	 * @return The half-life calculated into Minecraft ticks
	 */
	public static long getLifespan(float halfLife, HalfLifeType type, boolean realYears) {
		float life = 0;
		switch(type) {
		case LONG: life = (48000 * (realYears ? 365 : 100) * 100) * halfLife; break;
		case MEDIUM: life = (48000 * (realYears ? 365 : 100)) * halfLife; break;
		case SHORT: life = 48000 * halfLife; break;
		}
		return (long) life;
	}
}
