package com.hbm.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Same options as the original (hbm.cfg "radiation"/"hazard" categories), backed by NeoForge's config.
 * The static fields keep the original access pattern (RadiationConfig.enableContamination etc.)
 * and are refreshed whenever the config (re)loads.
 *
 * TODO pollution options once the pollution system is ported
 */
public class RadiationConfig {

	public static int fogRad = 100;
	public static int fogCh = 20;
	public static double hellRad = 0.1;
	public static int worldRad = 10;
	public static int worldRadThreshold = 20;
	public static boolean worldRadEffects = true;
	public static boolean cleanupDeadDirt = false;

	public static boolean enableContamination = true;
	public static boolean enableChunkRads = true;

	public static boolean disableAsbestos = false;
	public static boolean disableCoal = false;
	public static boolean disableHot = false;
	public static boolean disableExplosive = false;
	public static boolean disableHydro = false;
	public static boolean disableBlinding = false;
	public static boolean disableFibrosis = false;

	private static ModConfigSpec.IntValue FOG_RAD, FOG_CH, WORLD_RAD, WORLD_RAD_THRESHOLD;
	private static ModConfigSpec.DoubleValue HELL_RAD;
	private static ModConfigSpec.BooleanValue WORLD_RAD_EFFECTS, CLEANUP_DEAD_DIRT, ENABLE_CONTAMINATION, ENABLE_CHUNK_RADS;
	private static ModConfigSpec.BooleanValue DISABLE_ASBESTOS, DISABLE_COAL, DISABLE_HOT, DISABLE_EXPLOSIVE, DISABLE_HYDRO, DISABLE_BLINDING, DISABLE_FIBROSIS;

	static void define(ModConfigSpec.Builder builder) {
		builder.push("radiation");
		FOG_RAD = builder.comment("Radiation in RADs required for fog to spawn").defineInRange("FOG_00_threshold", 100, 0, Integer.MAX_VALUE);
		FOG_CH = builder.comment("1:n chance of fog spawning every second").defineInRange("FOG_01_threshold", 20, 1, Integer.MAX_VALUE);
		HELL_RAD = builder.comment("RAD/s in the nether").defineInRange("AMBIENT_00_nether", 0.1D, 0D, Double.MAX_VALUE);
		WORLD_RAD_EFFECTS = builder.comment("Whether high radiation levels should perform changes in the world").define("RADWORLD_00_toggle", true);
		WORLD_RAD = builder.comment("How many block operations radiation can perform per tick").defineInRange("RADWORLD_01_amount", 10, 0, Integer.MAX_VALUE);
		WORLD_RAD_THRESHOLD = builder.comment("The least amount of RADs required for block modification to happen").defineInRange("RADWORLD_02_minimum", 20, 0, Integer.MAX_VALUE);
		CLEANUP_DEAD_DIRT = builder.comment("Whether dead grass and mycelium should decay into dirt").define("RADWORLD_03_regrow", false);
		ENABLE_CONTAMINATION = builder.comment("Toggles player contamination (and negative effects from radiation poisoning)").define("RADIATION_00_enableContamination", true);
		ENABLE_CHUNK_RADS = builder.comment("Toggles the world radiation system (chunk radiation only, some blocks use an AoE!)").define("RADIATION_01_enableChunkRads", true);
		builder.pop();

		builder.push("hazard");
		DISABLE_ASBESTOS = builder.comment("When turned off, all asbestos hazards are disabled").define("HAZ_00_disableAsbestos", false);
		DISABLE_COAL = builder.comment("When turned off, all coal dust hazards are disabled").define("HAZ_01_disableCoaldust", false);
		DISABLE_HOT = builder.comment("When turned off, all hot hazards are disabled").define("HAZ_02_disableHot", false);
		DISABLE_EXPLOSIVE = builder.comment("When turned off, all explosive hazards are disabled").define("HAZ_03_disableExplosive", false);
		DISABLE_HYDRO = builder.comment("When turned off, all hydroactive hazards are disabled").define("HAZ_04_disableHydroactive", false);
		DISABLE_BLINDING = builder.comment("When turned off, all blinding hazards are disabled").define("HAZ_05_disableBlinding", false);
		DISABLE_FIBROSIS = builder.comment("When turned off, all fibrosis hazards are disabled").define("HAZ_06_disableFibrosis", false);
		builder.pop();
	}

	static void load() {
		fogRad = FOG_RAD.get();
		fogCh = FOG_CH.get();
		hellRad = HELL_RAD.get();
		worldRadEffects = WORLD_RAD_EFFECTS.get();
		worldRad = WORLD_RAD.get();
		worldRadThreshold = WORLD_RAD_THRESHOLD.get();
		cleanupDeadDirt = CLEANUP_DEAD_DIRT.get();
		enableContamination = ENABLE_CONTAMINATION.get();
		enableChunkRads = ENABLE_CHUNK_RADS.get();

		disableAsbestos = DISABLE_ASBESTOS.get();
		disableCoal = DISABLE_COAL.get();
		disableHot = DISABLE_HOT.get();
		disableExplosive = DISABLE_EXPLOSIVE.get();
		disableHydro = DISABLE_HYDRO.get();
		disableBlinding = DISABLE_BLINDING.get();
		disableFibrosis = DISABLE_FIBROSIS.get();
	}
}
