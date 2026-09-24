package com.hbm.blocks.machine;

import com.hbm.tileentity.ModTileEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * The original had separate _off and _on blocks, swapped while running. Now it's one block with a LIT state.
 */
public class MachineElectricFurnace extends BlockMachineTile {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	public MachineElectricFurnace(Properties properties) {
		super(properties.lightLevel(state -> state.getValue(LIT) ? 15 : 0), ModTileEntities.ELECTRIC_FURNACE);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	/** Switches the lit state, keeps the tile (the original had to preserve it while swapping blocks) */
	public static void updateBlockState(boolean isProcessing, Level world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if(state.hasProperty(LIT) && state.getValue(LIT) != isProcessing) {
			world.setBlock(pos, state.setValue(LIT, isProcessing), Block.UPDATE_ALL);
		}
	}
}
