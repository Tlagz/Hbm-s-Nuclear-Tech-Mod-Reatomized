package com.hbm.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Subset of the original's server config, grows as systems get ported */
public class ServerConfig {

	public static int ITEM_HAZARD_DROP_TICKRATE = 2;
	public static boolean ENABLE_MKU = true;

	private static ModConfigSpec.IntValue ITEM_HAZARD_DROP_TICKRATE_V;
	private static ModConfigSpec.BooleanValue ENABLE_MKU_V;

	static void define(ModConfigSpec.Builder builder) {
		builder.push("server");
		ITEM_HAZARD_DROP_TICKRATE_V = builder.comment("How many ticks pass between hazard updates of dropped items").defineInRange("ITEM_HAZARD_DROP_TICKRATE", 2, 1, 1200);
		ENABLE_MKU_V = builder.comment("Whether the MKU contagion is enabled").define("ENABLE_MKU", true);
		builder.pop();
	}

	static void load() {
		ITEM_HAZARD_DROP_TICKRATE = ITEM_HAZARD_DROP_TICKRATE_V.get();
		ENABLE_MKU = ENABLE_MKU_V.get();
	}
}
