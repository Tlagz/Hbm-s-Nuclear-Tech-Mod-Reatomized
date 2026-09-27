package com.hbm.blocks.machine;

import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityLoadedBase;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Charger, mounted on the wall behind it (FACING is its front like the original's metadata, pointing away from the
 * wall), drawn by its tile renderer. Ticks on both sides for the fold-out animation.
 */
public class Charger extends Block implements EntityBlock {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	private static final VoxelShape NORTH = Block.box(5, 4, 12, 11, 12, 16);
	private static final VoxelShape SOUTH = Block.box(5, 4, 0, 11, 12, 4);
	private static final VoxelShape WEST = Block.box(12, 4, 5, 16, 12, 11);
	private static final VoxelShape EAST = Block.box(0, 4, 5, 4, 12, 11);

	public Charger(Properties properties) {
		super(properties.noOcclusion());
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	/** Like the original, it faces the player, the wall is the block the player looks at */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return switch(state.getValue(FACING)) {
			case SOUTH -> SOUTH;
			case WEST -> WEST;
			case EAST -> EAST;
			default -> NORTH;
		};
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return ModTileEntities.CHARGER.get().create(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return type == ModTileEntities.CHARGER.get() ? TileEntityLoadedBase.ticker() : null;
	}
}
