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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Chain lift: carries items up. BOTTOM is the lowest block (no belt below, takes items from belts around it), TOP
 * the highest one of a column that pushes them out towards FACING's opposite like a belt.
 */
public class BlockConveyorLift extends BlockConveyorBase implements IToolable {

	public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");
	public static final BooleanProperty TOP = BooleanProperty.create("top");

	private static final VoxelShape TOP_SHAPE = Block.box(0, 0, 0, 16, 8, 16);

	public BlockConveyorLift(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(BOTTOM, true).setValue(TOP, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, BOTTOM, TOP);
	}

	public static boolean isBottom(BlockGetter world, BlockPos pos) {
		return !(world.getBlockState(pos.below()).getBlock() instanceof IConveyorBelt);
	}

	public static boolean isTop(BlockGetter world, BlockPos pos) {
		Block above = world.getBlockState(pos.above()).getBlock();
		return !(above instanceof IConveyorBelt) && !isBottom(world, pos) && !(above instanceof IEnterableBlock);
	}

	@Override
	public BlockState withConnections(BlockState state, BlockGetter world, BlockPos pos) {
		return state.setValue(BOTTOM, isBottom(world, pos)).setValue(TOP, isTop(world, pos));
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
		return state.getValue(TOP) ? TOP_SHAPE : Shapes.block();
	}

	@Override
	public Direction getInputDirection(Level world, BlockPos pos) {
		return Direction.DOWN;
	}

	@Override
	public Direction getOutputDirection(Level world, BlockPos pos) {
		return Direction.UP;
	}

	@Override
	public Direction getTravelDirection(Level world, BlockPos pos, Vec3 itemPos) {
		if(!isTop(world, pos)) return Direction.DOWN;
		return world.getBlockState(pos).getValue(FACING);
	}

	@Override
	public Vec3 getClosestSnappingPosition(Level world, BlockPos pos, Vec3 itemPos) {
		if(!isTop(world, pos)) {
			return new Vec3(pos.getX() + 0.5, itemPos.y, pos.getZ() + 0.5);
		} else {
			return super.getClosestSnappingPosition(world, pos, itemPos);
		}
	}

	/** Screwdriver: turn clockwise, sneaking turns it into a chute */
	@Override
	public boolean onScrew(Level world, Player player, BlockPos pos, Direction side, float fX, float fY, float fZ, ToolType tool) {

		if(tool != ToolType.SCREWDRIVER)
			return false;

		if(world.isClientSide) return true;

		BlockState state = world.getBlockState(pos);

		if(!player.isShiftKeyDown()) {
			world.setBlock(pos, state.setValue(FACING, state.getValue(FACING).getClockWise()), 3);
		} else {
			world.setBlock(pos, ModBlocks.conveyor_chute.get().getPlacementState(world, pos, getMeta(state)), 3);
		}

		return true;
	}
}
