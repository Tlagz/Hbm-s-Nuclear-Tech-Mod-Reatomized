package com.hbm.blocks.network;

import com.hbm.tileentity.network.TileEntityCraneGrabber;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Conveyor grabber, pulls items off belts */
public class CraneGrabber extends BlockCraneBase {

	public CraneGrabber(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityCraneGrabber(pos, state);
	}

	/** The upgrades drop, the filter ghosts don't */
	@Override
	protected int[] getDropRange() {
		return new int[] {9, 11};
	}
}
