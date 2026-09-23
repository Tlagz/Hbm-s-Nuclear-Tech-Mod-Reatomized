package com.hbm.lib;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

/**
 * Damage types are data driven now, see data/hbm/damage_type and the damage type tags
 * (bypasses_armor etc.) for the properties the original set in code. The message ids are unchanged,
 * so the original death.attack.* translations still apply.
 *
 * TODO entity-caused sources (bullets, tau, subatomic etc.) when weapons get ported
 */
public class ModDamageSource {

	public static final ResourceKey<DamageType> NUCLEAR_BLAST = key("nuclear_blast");
	public static final ResourceKey<DamageType> MUD_POISONING = key("mud_poisoning");
	public static final ResourceKey<DamageType> ACID = key("acid");
	public static final ResourceKey<DamageType> EUTHANIZED_SELF = key("euthanized_self");
	public static final ResourceKey<DamageType> EUTHANIZED_SELF2 = key("euthanized_self2");
	public static final ResourceKey<DamageType> TAU_BLAST = key("tau_blast");
	public static final ResourceKey<DamageType> RADIATION = key("radiation");
	public static final ResourceKey<DamageType> DIGAMMA = key("digamma");
	public static final ResourceKey<DamageType> SUICIDE = key("suicide");
	public static final ResourceKey<DamageType> RUBBLE = key("rubble");
	public static final ResourceKey<DamageType> SHRAPNEL = key("shrapnel");
	public static final ResourceKey<DamageType> BLACKHOLE = key("blackhole");
	public static final ResourceKey<DamageType> TURBOFAN = key("turbofan");
	public static final ResourceKey<DamageType> METEORITE = key("meteorite");
	public static final ResourceKey<DamageType> BOXCAR = key("boxcar");
	public static final ResourceKey<DamageType> BOAT = key("boat");
	public static final ResourceKey<DamageType> BUILDING = key("building");
	public static final ResourceKey<DamageType> TAINT = key("taint");
	public static final ResourceKey<DamageType> AMS = key("ams");
	public static final ResourceKey<DamageType> AMS_CORE = key("ams_core");
	public static final ResourceKey<DamageType> BROADCAST = key("broadcast");
	public static final ResourceKey<DamageType> BANG = key("bang");
	public static final ResourceKey<DamageType> PC = key("pc");
	public static final ResourceKey<DamageType> CLOUD = key("cloud");
	public static final ResourceKey<DamageType> LEAD = key("lead");
	public static final ResourceKey<DamageType> ENERVATION = key("enervation");
	public static final ResourceKey<DamageType> ELECTRICITY = key("electricity");
	public static final ResourceKey<DamageType> EXHAUST = key("exhaust");
	public static final ResourceKey<DamageType> SPIKES = key("spikes");
	public static final ResourceKey<DamageType> LUNAR = key("lunar");
	public static final ResourceKey<DamageType> MONOXIDE = key("monoxide");
	public static final ResourceKey<DamageType> ASBESTOS = key("asbestos");
	public static final ResourceKey<DamageType> BLACKLUNG = key("blacklung");
	public static final ResourceKey<DamageType> MKU = key("mku");
	public static final ResourceKey<DamageType> VACUUM = key("vacuum");
	public static final ResourceKey<DamageType> OVERDOSE = key("overdose");
	public static final ResourceKey<DamageType> MICROWAVE = key("microwave");

	private static ResourceKey<DamageType> key(String name) {
		return ResourceKey.create(Registries.DAMAGE_TYPE, RefStrings.loc(name));
	}

	public static DamageSource source(Level level, ResourceKey<DamageType> type) {
		return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type));
	}
}
