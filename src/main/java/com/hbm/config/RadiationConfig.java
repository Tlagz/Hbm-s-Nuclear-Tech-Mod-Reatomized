package com.hbm.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Same options as the original (hbm.cfg "radiation"/"hazard" categories), backed by NeoForge's config.
 * The static fields keep the original access pattern (RadiationConfig.enableContamination etc.)
 * and are refreshed whenever the config (re)loads.
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

	public static boolean enablePollution = true;
	public static boolean enableLeadFromBlocks = true;
	public static boolean enableLeadPoisoning = true;
	public static boolean enableSootFog = true;
	public static boolean enablePoison = true;
	public static double buffMobThreshold = 15D;
	public static double sootFogThreshold = 35D;
	public static double sootFogDivisor = 120D;
	public static double smokeStackSootMult = 0.8;

	private static ModConfigSpec.IntValue FOG_RAD, FOG_CH, WORLD_RAD, WORLD_RAD_THRESHOLD;
	private static ModConfigSpec.DoubleValue HELL_RAD;
	private static ModConfigSpec.BooleanValue WORLD_RAD_EFFECTS, CLEANUP_DEAD_DIRT, ENABLE_CONTAMINATION, ENABLE_CHUNK_RADS;
	private static ModConfigSpec.BooleanValue ENABLE_POLLUTION, ENABLE_LEAD_FROM_BLOCKS, ENABLE_LEAD_POISONING, ENABLE_SOOT_FOG, ENABLE_POISON;
	private static ModConfigSpec.DoubleValue BUFF_MOB_THRESHOLD, SOOT_FOG_THRESHOLD, SOOT_FOG_DIVISOR, SMOKE_STACK_SOOT_MULT;
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

		builder.push("pollution");
		ENABLE_POLLUTION = builder.comment("If disabled, none of the polltuion related things will work").define("POL_00_enablePollution", true);
		ENABLE_LEAD_FROM_BLOCKS = builder.comment("Whether breaking blocks in heavy metal polluted areas will poison the player").define("POL_01_enableLeadFromBlocks", true);
		ENABLE_LEAD_POISONING = builder.comment("Whether being in a heavy metal polluted area will poison the player").define("POL_02_enableLeadPoisoning", true);
		ENABLE_SOOT_FOG = builder.comment("Whether smog should be visible").define("POL_03_enableSootFog", true);
		ENABLE_POISON = builder.comment("Whether being in a poisoned area will affect the player").define("POL_04_enablePoison", true);
		BUFF_MOB_THRESHOLD = builder.comment("The amount of soot required to buff naturally spawning mobs").defineInRange("POL_05_buffMobThreshold", 15D, 0D, Double.MAX_VALUE);
		SOOT_FOG_THRESHOLD = builder.comment("How much soot is required for smog to become visible").defineInRange("POL_06_sootFogThreshold", 35D, 0D, Double.MAX_VALUE);
		SOOT_FOG_DIVISOR = builder.comment("The divisor for smog, higher numbers will require more soot for the same smog density").defineInRange("POL_07_sootFogDivisor", 120D, 0.001D, Double.MAX_VALUE);
		SMOKE_STACK_SOOT_MULT = builder.comment("How much does smokestack multiply soot by, with decimal values reducing the soot").defineInRange("POL_08_smokeStackSootMult", 0.8D, 0D, Double.MAX_VALUE);
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

		enablePollution = ENABLE_POLLUTION.get();
		enableLeadFromBlocks = ENABLE_LEAD_FROM_BLOCKS.get();
		enableLeadPoisoning = ENABLE_LEAD_POISONING.get();
		enableSootFog = ENABLE_SOOT_FOG.get();
		enablePoison = ENABLE_POISON.get();
		buffMobThreshold = BUFF_MOB_THRESHOLD.get();
		sootFogThreshold = SOOT_FOG_THRESHOLD.get();
		sootFogDivisor = SOOT_FOG_DIVISOR.get();
		smokeStackSootMult = SMOKE_STACK_SOOT_MULT.get();
	}
}
