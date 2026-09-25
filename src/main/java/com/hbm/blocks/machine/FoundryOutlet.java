package com.hbm.blocks.machine;

import com.hbm.blocks.IToolable;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemScraps;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.machine.TileEntityFoundryBase;
import com.hbm.tileentity.machine.TileEntityFoundryOutlet;

import api.hbm.block.ICrucibleAcceptor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Foundry outlet: hangs off the end of a channel (facing away from it) and pours into molds below. Using it inverts
 * the redstone behavior, scraps set a material filter, the screwdriver clears the filter and the hand drill inverts it.
 * FILTER and LOCK only show the filter and the closed gate on the model.
 */
public class FoundryOutlet extends Block implements EntityBlock, ICrucibleAcceptor, IToolable {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty FILTER = BooleanProperty.create("filter");
	public static final BooleanProperty LOCK = BooleanProperty.create("lock");

	public FoundryOutlet(Properties properties) {
		super(properties.noOcclusion());
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FILTER, false).setValue(LOCK, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, FILTER, LOCK);
	}

	/** Faces the player, i.e. away from the channel it was placed against */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	public static VoxelShape shape(Direction facing) {
		return switch(facing) {
		case SOUTH -> Block.box(5, 0, 0, 11, 8, 6);
		case WEST -> Block.box(10, 0, 5, 16, 8, 11);
		case EAST -> Block.box(0, 0, 5, 6, 8, 11);
		default -> Block.box(5, 0, 10, 11, 8, 16);
		};
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return shape(state.getValue(FACING));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityFoundryOutlet(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return world.isClientSide ? null : TileEntityLoadedBase.ticker();
	}

	@Override
	protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean moved) {
		if(!world.isClientSide && world.getBlockEntity(pos) instanceof TileEntityFoundryOutlet outlet) outlet.updateState();
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if(player.isShiftKeyDown()) return InteractionResult.PASS;
		if(!world.isClientSide && world.getBlockEntity(pos) instanceof TileEntityFoundryOutlet outlet) {
			outlet.invertRedstone = !outlet.invertRedstone;
			outlet.sync();
			outlet.updateState();
		}
		return InteractionResult.sidedSuccess(world.isClientSide);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if(player.isShiftKeyDown() || !held.is(ModItems.scraps.get())) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if(!world.isClientSide && world.getBlockEntity(pos) instanceof TileEntityFoundryOutlet outlet) {
			MaterialStack mat = ItemScraps.getMats(held);
			if(mat != null) outlet.filter = mat.material;
			outlet.sync();
			outlet.updateState();
		}
		return ItemInteractionResult.sidedSuccess(world.isClientSide);
	}

	@Override
	public boolean onScrew(Level world, Player player, BlockPos pos, Direction side, float fX, float fY, float fZ, ToolType tool) {
		if(!(world.getBlockEntity(pos) instanceof TileEntityFoundryOutlet outlet)) return false;
		if(tool == ToolType.SCREWDRIVER) {
			if(world.isClientSide) return true;
			outlet.filter = null;
			outlet.invertFilter = false;
			outlet.sync();
			outlet.updateState();
			return true;
		}
		if(tool == ToolType.HAND_DRILL) {
			if(world.isClientSide) return true;
			outlet.invertFilter = !outlet.invertFilter;
			outlet.sync();
			return true;
		}
		return false;
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
}
