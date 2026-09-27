package com.hbm.blocks.network;

import com.hbm.blocks.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** The regular belt, bending it past the right turn turns it into a chain lift */
public class BlockConveyor extends BlockConveyorBendable {

	public BlockConveyor(Properties properties) {
		super(properties);
	}

	@Override
	protected boolean onBendPastRight(Level world, BlockPos pos, BlockState state) {
		// switcheroo
		world.setBlock(pos, ModBlocks.conveyor_lift.get().getPlacementState(world, pos, state.getValue(FACING).get3DDataValue()), 3);
		return true;
	}
}
