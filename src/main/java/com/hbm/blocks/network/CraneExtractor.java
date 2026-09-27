package com.hbm.blocks.network;

import com.hbm.tileentity.network.TileEntityCraneExtractor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Conveyor extractor, takes items out of inventories and onto belts */
public class CraneExtractor extends BlockCraneBase {

	public CraneExtractor(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityCraneExtractor(pos, state);
	}

	/** Buffer and upgrades drop, the filter ghosts don't */
	@Override
	protected int[] getDropRange() {
		return new int[] {9, 20};
	}
}
