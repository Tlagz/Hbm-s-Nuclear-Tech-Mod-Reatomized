package com.hbm.blocks.generic;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * Chain link fence: flat fence panels between the posts, one block high. It connects to other metal fences, fence gates
 * and solid faces. The post variant (fence_metal_post, the original's metadata 1) always shows its post.
 */
public class BlockMetalFence extends CrossCollisionBlock {

	public static final MapCodec<BlockMetalFence> CODEC = simpleCodec(p -> new BlockMetalFence(p, false));

	public final boolean alwaysPost;

	public BlockMetalFence(Properties properties, boolean alwaysPost) {
		// 4 pixel wide post and panels (0.375-0.625), 16 high everywhere like the original's collision
		super(2.0F, 2.0F, 16.0F, 16.0F, 16.0F, properties.noOcclusion());
		this.alwaysPost = alwaysPost;
		this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false).setValue(WATERLOGGED, false));
	}

	@Override
	protected MapCodec<? extends CrossCollisionBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED);
	}

	/** The original's canConnectFenceTo: metal fences, fence gates and full solid blocks */
	public boolean connectsTo(BlockGetter world, BlockPos pos, Direction dir) {
		BlockState state = world.getBlockState(pos);
		if(state.getBlock() instanceof BlockMetalFence) return true;
		if(state.getBlock() instanceof FenceGateBlock) return FenceGateBlock.connectsToDirection(state, dir);
		return !isExceptionForConnection(state) && state.isFaceSturdy(world, pos, dir.getOpposite());
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockGetter world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		return this.defaultBlockState()
				.setValue(NORTH, connectsTo(world, pos.north(), Direction.NORTH))
				.setValue(EAST, connectsTo(world, pos.east(), Direction.EAST))
				.setValue(SOUTH, connectsTo(world, pos.south(), Direction.SOUTH))
				.setValue(WEST, connectsTo(world, pos.west(), Direction.WEST))
				.setValue(WATERLOGGED, world.getFluidState(pos).getType() == net.minecraft.world.level.material.Fluids.WATER);
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
		if(state.getValue(WATERLOGGED)) world.scheduleTick(pos, net.minecraft.world.level.material.Fluids.WATER, net.minecraft.world.level.material.Fluids.WATER.getTickDelay(world));
		if(dir.getAxis().isHorizontal()) return state.setValue(PROPERTY_BY_DIRECTION.get(dir), connectsTo(world, neighborPos, dir));
		return super.updateShape(state, dir, neighbor, world, pos, neighborPos);
	}

	/** The post shows unless the fence runs straight through, always on the post variant */
	public static boolean showPost(boolean alwaysPost, boolean north, boolean east, boolean south, boolean west) {
		boolean straightX = !north && !south && east && west;
		boolean straightZ = !east && !west && north && south;
		return alwaysPost || (!straightX && !straightZ);
	}
}
