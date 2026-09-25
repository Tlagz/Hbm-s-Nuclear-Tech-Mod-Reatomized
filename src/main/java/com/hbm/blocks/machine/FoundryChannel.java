package com.hbm.blocks.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.machine.ItemScraps;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.machine.TileEntityFoundryBase;
import com.hbm.tileentity.machine.TileEntityFoundryChannel;

import api.hbm.block.ICrucibleAcceptor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbilities;

/**
 * Foundry channel, a half block high gutter that connects to other channels, molds and outlets facing away from it.
 * The connections are block state properties, a shovel empties it as scraps.
 */
public class FoundryChannel extends Block implements EntityBlock, ICrucibleAcceptor {

	public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
	public static final BooleanProperty EAST = BlockStateProperties.EAST;
	public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
	public static final BooleanProperty WEST = BlockStateProperties.WEST;

	private static final VoxelShape CENTER = Block.box(5, 0, 5, 11, 8, 11);
	private static final VoxelShape[] ARMS = {
			Block.box(5, 0, 0, 11, 8, 5),	// north
			Block.box(11, 0, 5, 16, 8, 11),	// east
			Block.box(5, 0, 11, 11, 8, 16),	// south
			Block.box(0, 0, 5, 5, 8, 11)	// west
	};

	public FoundryChannel(Properties properties) {
		super(properties.noOcclusion());
		this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, SOUTH, WEST);
	}

	public static BooleanProperty property(Direction dir) {
		return switch(dir) {
		case NORTH -> NORTH;
		case EAST -> EAST;
		case SOUTH -> SOUTH;
		default -> WEST;
		};
	}

	/** The original's canConnectTo: channels, molds, and outlets facing away from this channel */
	public static boolean canConnectTo(BlockState neighbor, Direction dir) {
		if(neighbor.getBlock() instanceof FoundryOutlet) return neighbor.getValue(FoundryOutlet.FACING) == dir;
		return neighbor.is(ModBlocks.foundry_channel.get()) || neighbor.is(ModBlocks.foundry_mold.get());
	}

	private BlockState withConnections(BlockState state, BlockGetter world, BlockPos pos) {
		for(Direction dir : Direction.Plane.HORIZONTAL) state = state.setValue(property(dir), canConnectTo(world.getBlockState(pos.relative(dir)), dir));
		return state;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return withConnections(this.defaultBlockState(), context.getLevel(), context.getClickedPos());
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
		if(dir.getAxis().isHorizontal()) return state.setValue(property(dir), canConnectTo(neighbor, dir));
		return state;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		VoxelShape shape = CENTER;
		if(state.getValue(NORTH)) shape = Shapes.or(shape, ARMS[0]);
		if(state.getValue(EAST)) shape = Shapes.or(shape, ARMS[1]);
		if(state.getValue(SOUTH)) shape = Shapes.or(shape, ARMS[2]);
		if(state.getValue(WEST)) shape = Shapes.or(shape, ARMS[3]);
		return shape;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityFoundryChannel(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return world.isClientSide ? null : TileEntityLoadedBase.ticker();
	}

	@Override
	public boolean canAcceptPartialPour(Level world, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {
		return world.getBlockEntity(pos) instanceof TileEntityFoundryBase tile && tile.canAcceptPartialPour(world, pos, dX, dY, dZ, side, stack);
	}

	@Override
	public MaterialStack pour(Level world, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {
		return world.getBlockEntity(pos) instanceof TileEntityFoundryBase tile ? tile.pour(world, pos, dX, dY, dZ, side, stack) : stack;
	}

	@Override
	public boolean canAcceptPartialFlow(Level world, BlockPos pos, Direction side, MaterialStack stack) {
		return world.getBlockEntity(pos) instanceof TileEntityFoundryBase tile && tile.canAcceptPartialFlow(world, pos, side, stack);
	}

	@Override
	public MaterialStack flow(Level world, BlockPos pos, Direction side, MaterialStack stack) {
		return world.getBlockEntity(pos) instanceof TileEntityFoundryBase tile ? tile.flow(world, pos, side, stack) : stack;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if(!held.canPerformAction(ItemAbilities.SHOVEL_DIG)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if(!world.isClientSide && world.getBlockEntity(pos) instanceof TileEntityFoundryChannel channel && channel.amount > 0 && channel.type != null) {
			ItemStack scrap = ItemScraps.create(new MaterialStack(channel.type, channel.amount));
			if(!player.getInventory().add(scrap)) player.drop(scrap, false);
			channel.amount = 0;
			channel.type = null;
			channel.sync();
		}
		return ItemInteractionResult.sidedSuccess(world.isClientSide);
	}

	@Override
	protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
		if(!state.is(newState.getBlock()) && world.getBlockEntity(pos) instanceof TileEntityFoundryChannel channel && channel.amount > 0 && channel.type != null) {
			Containers.dropItemStack(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ItemScraps.create(new MaterialStack(channel.type, channel.amount)));
			channel.amount = 0;
		}
		super.onRemove(state, world, pos, newState, moved);
	}
}
