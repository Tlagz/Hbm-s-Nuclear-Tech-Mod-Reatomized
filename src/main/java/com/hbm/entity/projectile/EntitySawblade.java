package com.hbm.entity.projectile;

import com.hbm.entity.ModEntities;
import com.hbm.items.ModItems;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** The sawmill's blade flying off at overspeed, behaves like the Stirling engine's gear and gives the blade back */
public class EntitySawblade extends EntityCog {

	public EntitySawblade(EntityType<? extends EntitySawblade> type, Level world) {
		super(type, world);
	}

	public EntitySawblade(Level world, double x, double y, double z) {
		super(ModEntities.SAWBLADE.get(), world, x, y, z);
	}

	@Override
	protected ItemStack getDropItem() {
		return new ItemStack(ModItems.sawblade.get());
	}
}
