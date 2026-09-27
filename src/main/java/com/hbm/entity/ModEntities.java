package com.hbm.entity;

import com.hbm.entity.projectile.EntityCog;
import com.hbm.lib.RefStrings;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Entity types, named like the original's EntityRegistry names */
public class ModEntities {

	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, RefStrings.MODID);

	public static final DeferredHolder<EntityType<?>, EntityType<EntityCog>> COG = ENTITIES.register("entity_stirling_cog",
			() -> EntityType.Builder.<EntityCog>of(EntityCog::new, MobCategory.MISC).sized(1F, 1F).clientTrackingRange(16).updateInterval(1).build("entity_stirling_cog"));
	public static final DeferredHolder<EntityType<?>, EntityType<com.hbm.entity.projectile.EntitySawblade>> SAWBLADE = ENTITIES.register("entity_sawblade",
			() -> EntityType.Builder.<com.hbm.entity.projectile.EntitySawblade>of(com.hbm.entity.projectile.EntitySawblade::new, MobCategory.MISC).sized(1F, 1F).clientTrackingRange(16).updateInterval(1).build("entity_sawblade"));
}
