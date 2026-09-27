package com.hbm.blocks.network;

import java.util.List;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.IToolable;
import com.hbm.items.tool.ItemConveyorWand;
import com.hbm.items.tool.ItemTooling;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.network.TileEntityCraneBase;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Conveyor cranes. INPUT is the original's metadata, OUTPUT the side items leave from (the original kept a TE-side
 * override and used the opposite of the input when unset). The screwdriver sets the input side, sneaking the output.
 * The textures for every input/output pair are baked by datagen from the original's getIcon logic.
 */
public abstract class BlockCraneBase extends Block implements EntityBlock, IToolable, ITooltipProvider {

	public static final DirectionProperty INPUT = DirectionProperty.create("input");
	public static final DirectionProperty OUTPUT = DirectionProperty.create("output");

	public BlockCraneBase(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(INPUT, Direction.NORTH).setValue(OUTPUT, Direction.SOUTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(INPUT, OUTPUT);
	}

	/** The side facing the player is the input (the original's piston orientation) */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction input = context.getNearestLookingDirection().getOpposite();
		return this.defaultBlockState().setValue(INPUT, input).setValue(OUTPUT, input.getOpposite());
	}

	/** Forge 1.7's ForgeDirection.getRotation(axis): a quarter turn clockwise around the axis */
	public static Direction rotate(Direction dir, Direction axis) {
		if(dir.getAxis() == axis.getAxis()) return dir;
		int x = -axis.getStepX(), y = -axis.getStepY(), z = -axis.getStepZ();
		int dx = dir.getStepX(), dy = dir.getStepY(), dz = dir.getStepZ();
		return Direction.fromDelta(y * dz - z * dy, z * dx - x * dz, x * dy - y * dx);
	}

	/// SIDES ///

	public static Direction getInputSide(BlockState state) {
		return state.getValue(INPUT);
	}

	public static Direction getOutputSide(BlockState state) {
		Direction output = state.getValue(OUTPUT);
		return output == state.getValue(INPUT) ? output.getOpposite() : output;
	}

	/** The original's setInput: a new input may push the output out of the way */
	public static BlockState withInput(BlockState state, Direction direction) {
		Direction oldSide = getInputSide(state);
		Direction output = getOutputSide(state);
		if(oldSide == direction) direction = direction.getOpposite();

		// taking the output's side swaps input and output
		if(direction == output) return state.setValue(INPUT, direction).setValue(OUTPUT, oldSide);
		return state.setValue(INPUT, direction).setValue(OUTPUT, output);
	}

	/** The original's setOutputOverride: clicking the output side twice flips it, taking the input's side swaps them */
	public static BlockState withOutput(BlockState state, Direction direction) {
		Direction oldSide = getOutputSide(state);
		if(oldSide == direction) direction = direction.getOpposite();

		if(direction == getInputSide(state)) {
			return state.setValue(INPUT, oldSide).setValue(OUTPUT, direction);
		}
		return state.setValue(OUTPUT, direction);
	}

	@Override
	public boolean onScrew(Level world, Player player, BlockPos pos, Direction side, float fX, float fY, float fZ, ToolType tool) {
		if(tool != ToolType.SCREWDRIVER) return false;
		if(world.isClientSide) return true;

		BlockState state = world.getBlockState(pos);
		world.setBlock(pos, player.isShiftKeyDown() ? withOutput(state, side) : withInput(state, side), 3);
		return true;
	}

	/// INTERACTION ///

	/** Tools and conveyors work on the crane instead of opening it */
	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if(stack.getItem() instanceof ItemTooling || stack.getItem() instanceof ItemConveyorWand) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if(player.isShiftKeyDown()) return InteractionResult.PASS;
		if(!(world.getBlockEntity(pos) instanceof MenuProvider provider)) return InteractionResult.PASS;

		if(!world.isClientSide && player instanceof ServerPlayer serverPlayer) {
			serverPlayer.openMenu(provider, buf -> buf.writeBlockPos(pos));
		}
		return InteractionResult.sidedSuccess(world.isClientSide);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return world.isClientSide ? null : TileEntityLoadedBase.ticker();
	}

	/** The slots from start to end (exclusive) drop when broken, filters don't */
	protected int[] getDropRange() {
		return null;
	}

	@Override
	protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
		if(!state.is(newState.getBlock()) && world.getBlockEntity(pos) instanceof TileEntityCraneBase crane) {
			int[] range = getDropRange();
			if(range != null) {
				for(int i = range[0]; i < range[1]; i++) {
					ItemStack stack = crane.getItem(i);
					if(!stack.isEmpty()) Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
				}
				world.updateNeighbourForOutputSignal(pos, this);
			}
		}
		super.onRemove(state, world, pos, newState, moved);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		this.addStandardInfo(list);
	}
}
