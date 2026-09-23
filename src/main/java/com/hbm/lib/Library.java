package com.hbm.lib;

import api.hbm.energymk2.IEnergyConnectorBlock;
import api.hbm.energymk2.IEnergyConnectorMK2;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class Library {

	/**
	 * Whether a cable can visually connect to the block at pos
	 * @param dir the direction from the cable towards pos
	 */
	public static boolean canConnect(BlockGetter world, BlockPos pos, Direction dir) {

		if(world instanceof LevelReader reader && reader.isOutsideBuildHeight(pos))
			return false;

		BlockState state = world.getBlockState(pos);

		if(state.getBlock() instanceof IEnergyConnectorBlock con) {
			if(con.canConnect(world, pos, dir.getOpposite() /* machine's connecting side */))
				return true;
		}

		BlockEntity te = world.getBlockEntity(pos);

		if(te instanceof IEnergyConnectorMK2 con) {
			if(con.canConnect(dir.getOpposite() /* machine's connecting side */))
				return true;
		}

		return false;
	}
}
