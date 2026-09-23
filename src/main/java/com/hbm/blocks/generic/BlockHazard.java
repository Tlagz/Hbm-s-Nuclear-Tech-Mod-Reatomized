package com.hbm.blocks.generic;

import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Storage blocks of hazardous materials. Placed blocks irradiate their chunk with 10% of the item's radiation.
 *
 * TODO display effects (rad fog, schrab fog, flames, lava pops) need the particle system
 */
public class BlockHazard extends Block {

	/** Looked up lazily, the hazard registry isn't filled when blocks get constructed */
	private float rad = -1F;

	public BlockHazard(Properties properties) {
		super(properties);
	}

	private float getRad() {
		if(rad < 0) rad = HazardSystem.getHazardLevelFromStack(new ItemStack(this), HazardRegistry.RADIATION) * 0.1F;
		return rad;
	}

	@Override
	protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		super.onPlace(state, world, pos, oldState, movedByPiston);
		if(!world.isClientSide && getRad() > 0) world.scheduleTick(pos, this, 20);
	}

	@Override
	protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
		if(getRad() > 0) {
			ChunkRadiationManager.proxy.incrementRad(world, pos, getRad());
			world.scheduleTick(pos, this, 20);
		}
	}
}
