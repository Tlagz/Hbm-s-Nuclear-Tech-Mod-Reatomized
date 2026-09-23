package com.hbm.config;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Builds the common config file (config/hbm-common.toml) out of the per-topic config classes */
public class CommonConfig {

	public static final ModConfigSpec SPEC;

	static {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
		RadiationConfig.define(builder);
		ServerConfig.define(builder);
		SPEC = builder.build();
	}

	public static void register(ModContainer container, IEventBus modBus) {
		container.registerConfig(ModConfig.Type.COMMON, SPEC);
		modBus.addListener(ModConfigEvent.Loading.class, e -> reload(e.getConfig()));
		modBus.addListener(ModConfigEvent.Reloading.class, e -> reload(e.getConfig()));
	}

	private static void reload(ModConfig config) {
		if(config.getSpec() != SPEC) return;
		RadiationConfig.load();
		ServerConfig.load();
	}
}
