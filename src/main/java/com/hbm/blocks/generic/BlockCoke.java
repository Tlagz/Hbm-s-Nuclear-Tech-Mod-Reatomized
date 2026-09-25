package com.hbm.blocks.generic;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.items.ItemEnums.EnumCokeType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/** Coke blocks, flammable, furnace fuel through the fuel data map */
public class BlockCoke extends BlockEnumMulti {

	public BlockCoke(Properties properties, String descriptionId, EnumCokeType type) {
		super(properties, descriptionId);
	}

	@Override
	public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return 5;
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return 10;
	}
}
