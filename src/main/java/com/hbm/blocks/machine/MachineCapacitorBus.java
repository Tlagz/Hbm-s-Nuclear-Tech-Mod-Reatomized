package com.hbm.blocks.machine;

import api.hbm.energymk2.IEnergyConnectorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * Carries the output of capacitors. A straight chain of buses behind a capacitor provides its power at the end
 * the buses point to. Oriented like a piston (towards the player when placed).
 */
public class MachineCapacitorBus extends Block implements IEnergyConnectorBlock {

	public static final DirectionProperty FACING = DirectionalBlock.FACING;

	public MachineCapacitorBus(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
	}

	@Override
	public boolean canConnect(BlockGetter world, BlockPos pos, Direction dir) {
		return dir == world.getBlockState(pos).getValue(FACING);
	}
}
