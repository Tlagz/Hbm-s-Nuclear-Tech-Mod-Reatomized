package com.hbm.blocks.generic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Steel walls, corners, roofs and beams. They face the direction the player looked when placing them (the original's
 * metadata 2-5), which only changes the shape of walls and corners.
 * TODO turning walls and corners with the screwdriver (IToolable)
 */
public class DecoBlock extends Block {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public enum Type { WALL, CORNER, ROOF, BEAM }

	public final Type type;

	public DecoBlock(Properties properties, Type type) {
		super(properties.noOcclusion());
		this.type = type;
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	private static final VoxelShape ROOF = Block.box(0, 0, 0, 16, 1, 16);
	private static final VoxelShape BEAM = Block.box(7, 0, 7, 9, 16, 9);

	/** Wall at the edge the player stood at, NORTH is the original's meta 2 */
	public static VoxelShape wall(Direction facing) {
		return switch(facing) {
		case SOUTH -> Block.box(0, 0, 0, 16, 16, 2);
		case WEST -> Block.box(14, 0, 0, 16, 16, 16);
		case EAST -> Block.box(0, 0, 0, 2, 16, 16);
		default -> Block.box(0, 0, 14, 16, 16, 16);
		};
	}

	/** Corner piece made of three boxes, like the original's collision boxes */
	public static VoxelShape corner(Direction facing) {
		return switch(facing) {
		case SOUTH -> Shapes.or(Block.box(0, 0, 0, 12, 16, 2), Block.box(12, 0, 0, 16, 16, 4), Block.box(14, 0, 4, 16, 16, 16));
		case WEST -> Shapes.or(Block.box(14, 0, 0, 16, 16, 12), Block.box(12, 0, 12, 16, 16, 16), Block.box(0, 0, 14, 12, 16, 16));
		case EAST -> Shapes.or(Block.box(0, 0, 4, 2, 16, 16), Block.box(0, 0, 0, 4, 16, 4), Block.box(4, 0, 0, 16, 16, 2));
		default -> Shapes.or(Block.box(4, 0, 14, 16, 16, 16), Block.box(0, 0, 12, 4, 16, 16), Block.box(0, 0, 0, 2, 16, 12));
		};
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return switch(type) {
		case WALL -> wall(state.getValue(FACING));
		case CORNER -> corner(state.getValue(FACING));
		case ROOF -> ROOF;
		case BEAM -> BEAM;
		};
	}
}
