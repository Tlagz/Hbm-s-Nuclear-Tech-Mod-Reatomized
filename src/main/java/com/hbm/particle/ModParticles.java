package com.hbm.particle;

import com.hbm.lib.RefStrings;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Particle types. The original's custom particles are spawned client side from {@link ParticleEffectsNT#effectNT},
 * the types here mostly exist to get their sprites onto the particle atlas.
 */
public class ModParticles {

	public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, RefStrings.MODID);

	/** The original's particleBase icon, a soft round puff (particle_base.png), used by the cooling tower steam */
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BASE = PARTICLES.register("base", () -> new SimpleParticleType(false));
	/** The vanilla smoke sprites, for the gas flame (the original extended EntitySmokeFX) */
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GAS_FLAME = PARTICLES.register("gas_flame", () -> new SimpleParticleType(false));
}
