package com.hbm.blocks.machine;

import com.hbm.tileentity.machine.TileEntityFoundryBasin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Full block basin for the large molds (ingots, plates, blocks...), only filled by pouring */
public class FoundryBasin extends FoundryCastingBase {

	public FoundryBasin(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityFoundryBasin(pos, state);
	}
}
