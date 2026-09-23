package com.hbm.config;

import java.util.LinkedHashMap;
import java.util.Map;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Ore generation config, same keys and defaults as the original's "oregen" category.
 * Vein counts are looked up by name at generation time (see ConfigCountPlacement), so changing the config
 * affects newly generated chunks without touching the worldgen data.
 *
 * TODO structures, bedrock ores, clusters, depth deposits, meteorites
 */
public class WorldConfig {

	public static boolean overworldOre = true;
	public static boolean netherOre = true;
	public static boolean endOre = true;

	/** Vein counts by config name, e.g. "uraniumSpawn" -> 7 */
	public static final Map<String, Integer> SPAWN_RATES = new LinkedHashMap<>();

	private static ModConfigSpec.BooleanValue OVERWORLD_ORE, NETHER_ORE, END_ORE;
	private static final Map<String, ModConfigSpec.IntValue> RATE_VALUES = new LinkedHashMap<>();

	private record Rate(String name, String key, String comment, int def) { }

	private static final Rate[] RATES = {
			new Rate("uraniumSpawn", "2.00_uraniumSpawnrate", "Amount of uranium ore veins per chunk", 7),
			new Rate("titaniumSpawn", "2.01_titaniumSpawnrate", "Amount of titanium ore veins per chunk", 8),
			new Rate("sulfurSpawn", "2.02_sulfurSpawnrate", "Amount of sulfur ore veins per chunk", 5),
			new Rate("aluminiumSpawn", "2.03_aluminiumSpawnrate", "Amount of aluminium ore veins per chunk", 7),
			new Rate("copperSpawn", "2.04_copperSpawnrate", "Amount of copper ore veins per chunk", 12),
			new Rate("fluoriteSpawn", "2.05_fluoriteSpawnrate", "Amount of fluorite ore veins per chunk", 6),
			new Rate("niterSpawn", "2.06_niterSpawnrate", "Amount of niter ore veins per chunk", 6),
			new Rate("tungstenSpawn", "2.07_tungstenSpawnrate", "Amount of tungsten ore veins per chunk", 10),
			new Rate("leadSpawn", "2.08_leadSpawnrate", "Amount of lead ore veins per chunk", 6),
			new Rate("berylliumSpawn", "2.09_berylliumSpawnrate", "Amount of beryllium ore veins per chunk", 6),
			new Rate("thoriumSpawn", "2.10_thoriumSpawnrate", "Amount of thorium ore veins per chunk", 7),
			new Rate("ligniteSpawn", "2.11_ligniteSpawnrate", "Amount of lignite ore veins per chunk", 2),
			new Rate("asbestosSpawn", "2.12_asbestosSpawnRate", "Amount of asbestos ore veins per chunk", 2),
			new Rate("rareSpawn", "2.14_rareEarthSpawnRate", "Amount of rare earth ore veins per chunk", 6),
			new Rate("cinnebarSpawn", "2.18_cinnebarSpawnRate", "Amount of cinnebar ore veins per chunk", 1),
			new Rate("cobaltSpawn", "2.18_cobaltSpawnRate", "Amount of cobalt ore veins per chunk", 2),
			new Rate("netherUraniumuSpawn", "2.N00_uraniumSpawnrate", "Amount of nether uranium per chunk", 8),
			new Rate("netherTungstenSpawn", "2.N01_tungstenSpawnrate", "Amount of nether tungsten per chunk", 10),
			new Rate("netherSulfurSpawn", "2.N02_sulfurSpawnrate", "Amount of nether sulfur per chunk", 26),
			new Rate("netherPhosphorusSpawn", "2.N03_phosphorusSpawnrate", "Amount of nether phosphorus per chunk", 24),
			new Rate("netherPlutoniumSpawn", "2.N05_plutoniumSpawnrate", "Amount of nether plutonium per chunk, if enabled", 8),
			new Rate("netherCobaltSpawn", "2.N06_cobaltSpawnrate", "Amount of nether cobalt per chunk", 2),
	};

	/** From the original's GeneralConfig */
	public static boolean enablePlutoniumOre = false;
	private static ModConfigSpec.BooleanValue ENABLE_PLUTONIUM_ORE;

	static {
		for(Rate rate : RATES) SPAWN_RATES.put(rate.name(), rate.def());
	}

	static void define(ModConfigSpec.Builder builder) {
		builder.push("oregen");
		OVERWORLD_ORE = builder.comment("General switch for whether overworld ores should be generated. Does not include special structures like oil.").define("2.D00_overworldOres", true);
		NETHER_ORE = builder.comment("General switch for whether nether ores should be generated.").define("2.D01_netherOres", true);
		END_ORE = builder.comment("General switch for whether end ores should be generated. Does not include special structures like trixite crystals.").define("2.D02_endOres", true);
		for(Rate rate : RATES) RATE_VALUES.put(rate.name(), builder.comment(rate.comment()).defineInRange(rate.key(), rate.def(), 0, 1000));
		ENABLE_PLUTONIUM_ORE = builder.comment("Enables plutonium ore generation in the nether").define("1.02_enablePlutoniumNetherOre", false);
		builder.pop();
	}

	static void load() {
		overworldOre = OVERWORLD_ORE.get();
		netherOre = NETHER_ORE.get();
		endOre = END_ORE.get();
		RATE_VALUES.forEach((name, value) -> SPAWN_RATES.put(name, value.get()));
		enablePlutoniumOre = ENABLE_PLUTONIUM_ORE.get();
	}

	/** Vein count for the ore, 0 if the whole category is switched off */
	public static int getRate(String name, String dimension) {
		if(dimension.equals("overworld") && !overworldOre) return 0;
		if(dimension.equals("nether") && !netherOre) return 0;
		if(dimension.equals("end") && !endOre) return 0;
		if(name.equals("netherPlutoniumSpawn") && !enablePlutoniumOre) return 0;
		return SPAWN_RATES.getOrDefault(name, 0);
	}
}
