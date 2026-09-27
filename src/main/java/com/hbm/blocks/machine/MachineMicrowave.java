package com.hbm.blocks.machine;

import com.hbm.tileentity.ModTileEntities;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/** Microwave, rendered by RenderMicrowave (the plate spins while it's running) */
public class MachineMicrowave extends BlockMachineTile {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public MachineMicrowave(Properties properties) {
		super(properties.noOcclusion(), ModTileEntities.MICROWAVE);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	/** Faces the player, the original's BlockMachineBase rotatable metadata */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}
}
