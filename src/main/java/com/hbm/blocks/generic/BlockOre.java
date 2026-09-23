package com.hbm.blocks.generic;

import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.potion.HbmPotion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ores and other blocks with special drops. The drops themselves are loot tables now
 * (see ModBlockLootProvider), this keeps the remaining behavior: walking effects and block radiation.
 *
 * TODO molten meteor, oil ore, trinitite particles
 */
public class BlockOre extends Block {

	private float rad = 0.0F;
	public boolean allowFortune = true;

	public BlockOre(Properties properties) {
		super(properties);
	}

	/** The original's deprecated constructor for radioactive ores, adds chunk radiation while placed */
	public BlockOre setRad(float rad) {
		this.rad = rad;
		return this;
	}

	public BlockOre noFortune() {
		this.allowFortune = false;
		return this;
	}

	@Override
	public void stepOn(Level world, BlockPos pos, BlockState state, Entity entity) {
		if(entity instanceof LivingEntity living && !world.isClientSide) {
			switch(BuiltInRegistries.BLOCK.getKey(this).getPath()) {
			case "frozen_dirt" -> living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2 * 60 * 20, 2));
			case "block_trinitite", "block_waste" -> living.addEffect(new MobEffectInstance(HbmPotion.radiation, 30 * 20, 2));
			case "waste_trinitite", "waste_trinitite_red" -> living.addEffect(new MobEffectInstance(HbmPotion.radiation, 5 * 20, 2));
			case "brick_jungle_ooze" -> living.addEffect(new MobEffectInstance(HbmPotion.radiation, 15 * 20, 9));
			case "brick_jungle_mystic" -> living.addEffect(new MobEffectInstance(HbmPotion.taint, 15 * 20, 2));
			default -> { }
			}
		}
		super.stepOn(world, pos, state, entity);
	}

	@Override
	protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		super.onPlace(state, world, pos, oldState, movedByPiston);
		if(this.rad > 0) world.scheduleTick(pos, this, 20);
	}

	@Override
	protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
		if(this.rad > 0) {
			ChunkRadiationManager.proxy.incrementRad(world, pos, rad);
			world.scheduleTick(pos, this, 20);
		}
	}
}
