package com.hbm.blocks.network;

import com.hbm.blocks.IToolable;
import com.hbm.blocks.ModBlocks;

import api.hbm.conveyor.IConveyorBelt;
import api.hbm.conveyor.IEnterableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Chute: items fall down through it, the bottom-most one acts like a belt (BELT). The connection properties only
 * pick the model (belt stubs towards neighboring belts, glass everywhere else), the original's RenderConveyorChute.
 */
public class BlockConveyorChute extends BlockConveyorBase implements IToolable {

	public static final BooleanProperty BELT = BooleanProperty.create("belt");
	public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
	public static final BooleanProperty EAST = BlockStateProperties.EAST;
	public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
	public static final BooleanProperty WEST = BlockStateProperties.WEST;

	public BlockConveyorChute(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(BELT, true)
				.setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, BELT, NORTH, EAST, SOUTH, WEST);
	}

	public static BooleanProperty connection(Direction dir) {
		return switch(dir) {
		case EAST -> EAST;
		case SOUTH -> SOUTH;
		case WEST -> WEST;
		default -> NORTH;
		};
	}

	/** Neighbor-derived properties */
	@Override
	public BlockState withConnections(BlockState state, BlockGetter world, BlockPos pos) {
		Block below = world.getBlockState(pos.below()).getBlock();
		state = state.setValue(BELT, !(below instanceof IConveyorBelt || below instanceof IEnterableBlock));
		for(Direction dir : Direction.Plane.HORIZONTAL) state = state.setValue(connection(dir), world.getBlockState(pos.relative(dir)).getBlock() instanceof IConveyorBelt);
		return state;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return withConnections(super.getStateForPlacement(context), context.getLevel(), context.getClickedPos());
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
		return withConnections(state, world, pos);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return Shapes.block();
	}

	private static boolean isStacked(Level world, BlockPos pos, Vec3 itemPos) {
		Block below = world.getBlockState(pos.below()).getBlock();
		return below instanceof IConveyorBelt || below instanceof IEnterableBlock || itemPos.y > pos.getY() + 0.25;
	}

	@Override
	public Vec3 getTravelLocation(Level world, BlockPos pos, Vec3 itemPos, double speed) {

		Block below = world.getBlockState(pos.below()).getBlock();
		if(below instanceof IConveyorBelt || below instanceof IEnterableBlock) {
			speed *= 5;
		} else if(itemPos.y > pos.getY() + 0.25) {
			speed *= 3;
		}

		return super.getTravelLocation(world, pos, itemPos, speed);
	}

	@Override
	public Direction getInputDirection(Level world, BlockPos pos) {
		return Direction.UP;
	}

	@Override
	public Direction getOutputDirection(Level world, BlockPos pos) {
		return Direction.DOWN;
	}

	@Override
	public Direction getTravelDirection(Level world, BlockPos pos, Vec3 itemPos) {
		if(isStacked(world, pos, itemPos)) return Direction.UP;
		return world.getBlockState(pos).getValue(FACING);
	}

	@Override
	public Vec3 getClosestSnappingPosition(Level world, BlockPos pos, Vec3 itemPos) {
		if(isStacked(world, pos, itemPos)) {
			return new Vec3(pos.getX() + 0.5, itemPos.y, pos.getZ() + 0.5);
		} else {
			return super.getClosestSnappingPosition(world, pos, itemPos);
		}
	}

	/** Screwdriver: turn clockwise, sneaking turns it back into a regular belt */
	@Override
	public boolean onScrew(Level world, Player player, BlockPos pos, Direction side, float fX, float fY, float fZ, ToolType tool) {

		if(tool != ToolType.SCREWDRIVER)
			return false;

		if(world.isClientSide) return true;

		BlockState state = world.getBlockState(pos);

		if(!player.isShiftKeyDown()) {
			world.setBlock(pos, state.setValue(FACING, state.getValue(FACING).getClockWise()), 3);
		} else {
			world.setBlock(pos, ModBlocks.conveyor.get().getPlacementState(world, pos, getMeta(state)), 3);
		}

		return true;
	}
}
