package com.hbm.blocks.network;

import java.util.Map;

import com.hbm.lib.Library;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.network.TileEntityCableBaseNT;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Connections are block state properties (the original computed them on the fly while rendering),
 * that way the model and hitbox can be baked/cached.
 */
public class BlockCable extends Block implements EntityBlock {

	public static final Map<Direction, BooleanProperty> CONNECTIONS = PipeBlock.PROPERTY_BY_DIRECTION;

	private static final VoxelShape[] SHAPES = new VoxelShape[64];

	static {
		double min = 5.5, max = 10.5;
		VoxelShape core = Block.box(min, min, min, max, max, max);
		VoxelShape[] arms = new VoxelShape[6];
		for(Direction dir : Direction.values()) {
			arms[dir.ordinal()] = Block.box(
					dir.getStepX() < 0 ? 0 : min, dir.getStepY() < 0 ? 0 : min, dir.getStepZ() < 0 ? 0 : min,
					dir.getStepX() > 0 ? 16 : max, dir.getStepY() > 0 ? 16 : max, dir.getStepZ() > 0 ? 16 : max);
		}
		for(int mask = 0; mask < 64; mask++) {
			VoxelShape shape = core;
			for(Direction dir : Direction.values()) if((mask & (1 << dir.ordinal())) != 0) shape = Shapes.or(shape, arms[dir.ordinal()]);
			SHAPES[mask] = shape.optimize();
		}
	}

	public BlockCable(Properties properties) {
		super(properties);
		BlockState state = this.stateDefinition.any();
		for(BooleanProperty prop : CONNECTIONS.values()) state = state.setValue(prop, false);
		this.registerDefaultState(state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		CONNECTIONS.values().forEach(builder::add);
	}

	public static int connectionMask(BlockState state) {
		int mask = 0;
		for(Direction dir : Direction.values()) if(state.getValue(CONNECTIONS.get(dir))) mask |= 1 << dir.ordinal();
		return mask;
	}

	protected boolean canConnectTo(BlockGetter world, BlockPos pos, Direction dir) {
		return Library.canConnect(world, pos.relative(dir), dir);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = this.defaultBlockState();
		for(Direction dir : Direction.values()) state = state.setValue(CONNECTIONS.get(dir), canConnectTo(context.getLevel(), context.getClickedPos(), dir));
		return state;
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction dir, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
		return state.setValue(CONNECTIONS.get(dir), canConnectTo(world, pos, dir));
	}

	/** Machines only get their tile entity after the block update, recheck once it exists */
	@Override
	protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
		super.neighborChanged(state, world, pos, neighborBlock, neighborPos, movedByPiston);
		BlockState updated = state;
		for(Direction dir : Direction.values()) updated = updated.setValue(CONNECTIONS.get(dir), canConnectTo(world, pos, dir));
		if(updated != state) world.setBlock(pos, updated, Block.UPDATE_CLIENTS);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return SHAPES[connectionMask(state)];
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityCableBaseNT(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return world.isClientSide || type != ModTileEntities.CABLE.get() ? null : TileEntityLoadedBase.ticker();
	}
}
