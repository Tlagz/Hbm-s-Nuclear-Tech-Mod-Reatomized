package com.hbm.blocks.generic;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Gravity affected blocks, vanilla BlockFalling in the original */
public class BlockFallingNT extends FallingBlock {

	public static final MapCodec<BlockFallingNT> CODEC = simpleCodec(BlockFallingNT::new);

	public BlockFallingNT(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends FallingBlock> codec() {
		return CODEC;
	}

	@Override
	public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
		return state.getMapColor(level, pos).col;
	}
}
