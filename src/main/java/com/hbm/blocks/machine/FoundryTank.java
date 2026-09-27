package com.hbm.blocks.machine;

import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.machine.ItemScraps;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.machine.TileEntityFoundryTank;

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
import net.neoforged.neoforge.common.ItemAbilities;

/**
 * Foundry tank: an open box that merges with adjacent tanks (walls and floors between them disappear) and shows an
 * outlet hole where an outlet points away from it. The original drew this in an ISBRH, here the connections are
 * block state properties picked up by a multipart model; the molten contents are drawn by RenderFoundry.
 */
public class FoundryTank extends Block implements EntityBlock, ICrucibleAcceptor {

	public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
	public static final BooleanProperty EAST = BlockStateProperties.EAST;
	public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
	public static final BooleanProperty WEST = BlockStateProperties.WEST;
	public static final BooleanProperty UP = BlockStateProperties.UP;
	public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
	public static final BooleanProperty OUT_NORTH = BooleanProperty.create("out_north");
	public static final BooleanProperty OUT_EAST = BooleanProperty.create("out_east");
	public static final BooleanProperty OUT_SOUTH = BooleanProperty.create("out_south");
	public static final BooleanProperty OUT_WEST = BooleanProperty.create("out_west");

	public FoundryTank(Properties properties) {
		super(properties.noOcclusion());
		BlockState state = this.stateDefinition.any();
		for(Direction dir : Direction.values()) state = state.setValue(connection(dir), false);
		for(Direction dir : Direction.Plane.HORIZONTAL) state = state.setValue(outlet(dir), false);
		this.registerDefaultState(state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, OUT_NORTH, OUT_EAST, OUT_SOUTH, OUT_WEST);
	}

	public static BooleanProperty connection(Direction dir) {
		return switch(dir) {
		case NORTH -> NORTH;
		case EAST -> EAST;
		case SOUTH -> SOUTH;
		case WEST -> WEST;
		case UP -> UP;
		default -> DOWN;
		};
	}

	public static BooleanProperty outlet(Direction dir) {
		return switch(dir) {
		case EAST -> OUT_EAST;
		case SOUTH -> OUT_SOUTH;
		case WEST -> OUT_WEST;
		default -> OUT_NORTH;
		};
	}

	/** Outlets (and slag taps) facing away from the tank get a hole in the wall */
	private static boolean isOutlet(BlockState neighbor, Direction dir) {
		return neighbor.getBlock() instanceof FoundryOutlet && neighbor.getValue(FoundryOutlet.FACING) == dir;
	}

	private BlockState update(BlockState state, Direction dir, BlockState neighbor) {
		state = state.setValue(connection(dir), neighbor.is(this));
		if(dir.getAxis().isHorizontal()) state = state.setValue(outlet(dir), isOutlet(neighbor, dir));
		return state;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = this.defaultBlockState();
		for(Direction dir : Direction.values()) state = update(state, dir, context.getLevel().getBlockState(context.getClickedPos().relative(dir)));
		return state;
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
		return update(state, dir, neighbor);
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState state, BlockGetter world, BlockPos pos) {
		return true;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityFoundryTank(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return world.isClientSide ? null : TileEntityLoadedBase.ticker();
	}

	@Override
	public boolean canAcceptPartialPour(Level world, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {
		return world.getBlockEntity(pos) instanceof TileEntityFoundryTank tank && tank.canAcceptPartialPour(world, pos, dX, dY, dZ, side, stack);
	}

	@Override
	public MaterialStack pour(Level world, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {
		return world.getBlockEntity(pos) instanceof TileEntityFoundryTank tank ? tank.pour(world, pos, dX, dY, dZ, side, stack) : stack;
	}

	@Override public boolean canAcceptPartialFlow(Level world, BlockPos pos, Direction side, MaterialStack stack) { return false; }
	@Override public MaterialStack flow(Level world, BlockPos pos, Direction side, MaterialStack stack) { return stack; }

	/** Empty with a shovel */
	@Override
	protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if(!held.canPerformAction(ItemAbilities.SHOVEL_DIG)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		if(!world.isClientSide && world.getBlockEntity(pos) instanceof TileEntityFoundryTank tank && tank.amount > 0 && tank.type != null) {
			ItemStack scrap = ItemScraps.create(new MaterialStack(tank.type, tank.amount));
			if(!player.getInventory().add(scrap)) player.drop(scrap, false);
			tank.amount = 0;
			tank.type = null;
			tank.sync();
		}
		return ItemInteractionResult.sidedSuccess(world.isClientSide);
	}

	@Override
	protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
		if(!state.is(newState.getBlock()) && world.getBlockEntity(pos) instanceof TileEntityFoundryTank tank && tank.amount > 0 && tank.type != null) {
			Containers.dropItemStack(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, ItemScraps.create(new MaterialStack(tank.type, tank.amount)));
			tank.amount = 0;
		}
		super.onRemove(state, world, pos, newState, moved);
	}
}
