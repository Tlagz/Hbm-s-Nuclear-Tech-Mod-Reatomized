package com.hbm.lib;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

/**
 * Damage types are data driven now, see data/hbm/damage_type and the damage type tags
 * (bypasses_armor etc.) for the properties the original set in code.
 */
public class ModDamageSource {

	public static final ResourceKey<DamageType> RADIATION = key("radiation");
	public static final ResourceKey<DamageType> DIGAMMA = key("digamma");
	public static final ResourceKey<DamageType> ASBESTOS = key("asbestos");
	public static final ResourceKey<DamageType> BLACKLUNG = key("blacklung");

	private static ResourceKey<DamageType> key(String name) {
		return ResourceKey.create(Registries.DAMAGE_TYPE, RefStrings.loc(name));
	}

	public static DamageSource source(Level level, ResourceKey<DamageType> type) {
		return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type));
	}
}
