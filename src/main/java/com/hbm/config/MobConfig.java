package com.hbm.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Subset of the original's mob config, grows as mobs get ported (glyphids, rampant mode etc.) */
public class MobConfig {

	public static double pollutionMult = 1;

	private static ModConfigSpec.DoubleValue POLLUTION_MULT;

	static void define(ModConfigSpec.Builder builder) {
		builder.push("mobs");
		POLLUTION_MULT = builder.comment("A multiplier for soot emitted, whether you want to increase or decrease it").defineInRange("12.R08_pollutionMult", 1D, 0D, Double.MAX_VALUE);
		builder.pop();
	}

	static void load() {
		pollutionMult = POLLUTION_MULT.get();
	}
}
