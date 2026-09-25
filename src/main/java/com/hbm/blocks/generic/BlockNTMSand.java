package com.hbm.blocks.generic;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Mixed sands (sand_mix), falling like sand, one block per type */
public class BlockNTMSand extends FallingBlock {

	public static final MapCodec<BlockNTMSand> CODEC = simpleCodec(p -> new BlockNTMSand(p, "block.hbm.sand_boron", EnumSandType.BORON));

	private final String descriptionId;

	public BlockNTMSand(Properties properties, String descriptionId, EnumSandType type) {
		super(properties);
		this.descriptionId = descriptionId;
	}

	@Override
	public String getDescriptionId() {
		return descriptionId;
	}

	@Override
	protected MapCodec<? extends FallingBlock> codec() {
		return CODEC;
	}

	@Override
	public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
		return state.getMapColor(level, pos).col;
	}

	public static enum EnumSandType {
		BORON, LEAD, URANIUM, POLONIUM, QUARTZ
	}
}
