package com.hbm.blocks.network;

import java.util.Map;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Universal fluid duct (the original's FluidDuctBase + FluidDuctStandard). The three textures (normal, silver,
 * colored) were metadata, now the STYLE property; the item carries it in the BLOCK_STATE component.
 * Connections are block state properties like the cables, they depend on the pipe's fluid type so they are
 * recomputed whenever the type changes.
 *
 * TODO alt-click copying the type into the identifier (keybinds), analyzer info
 */
public class FluidDuctStandard extends Block implements EntityBlock, IBlockFluidDuct, com.hbm.blocks.ILookOverlay {

	public static final IntegerProperty STYLE = IntegerProperty.create("style", 0, 2);
	public static final Map<Direction, BooleanProperty> CONNECTIONS = PipeBlock.PROPERTY_BY_DIRECTION;
	public static final String[] STYLE_TEXTURES = { "pipe_neo", "pipe_silver", "pipe_colored" };

	private static final VoxelShape[] SHAPES = new VoxelShape[64];

	static {
		double min = 5, max = 11;
		VoxelShape core = Block.box(min, min, min, max, max, max);
		VoxelShape[] arms = new VoxelShape[6];
		for(Direction dir : Direction.values()) {
			arms[dir.ordinal()] = Block.box(
					dir.getStepX() < 0 ? 0 : dir.getStepX() > 0 ? max : min, dir.getStepY() < 0 ? 0 : dir.getStepY() > 0 ? max : min, dir.getStepZ() < 0 ? 0 : dir.getStepZ() > 0 ? max : min,
					dir.getStepX() > 0 ? 16 : dir.getStepX() < 0 ? min : max, dir.getStepY() > 0 ? 16 : dir.getStepY() < 0 ? min : max, dir.getStepZ() > 0 ? 16 : dir.getStepZ() < 0 ? min : max);
		}
		for(int mask = 0; mask < 64; mask++) {
			VoxelShape shape = core;
			int visible = visibleArms(mask);
			for(Direction dir : Direction.values()) if((visible & (1 << dir.ordinal())) != 0) shape = Shapes.or(shape, arms[dir.ordinal()]);
			SHAPES[mask] = shape.optimize();
		}
	}

	private static int bits(Direction a, Direction b) {
		return (1 << a.ordinal()) | (1 << b.ordinal());
	}

	/**
	 * Which arms are drawn/solid for a connection mask (bit = Direction ordinal), the original's render logic:
	 * no connections is a full cross, connections along a single axis are a straight pipe through the block.
	 */
	public static int visibleArms(int mask) {
		int x = bits(Direction.EAST, Direction.WEST), y = bits(Direction.UP, Direction.DOWN), z = bits(Direction.SOUTH, Direction.NORTH);
		if(mask == 0) return 63;
		if((mask & ~x) == 0) return x;
		if((mask & ~y) == 0) return y;
		if((mask & ~z) == 0) return z;
		return mask;
	}

	public FluidDuctStandard(Properties properties) {
		super(properties);
		BlockState state = this.stateDefinition.any().setValue(STYLE, 0);
		for(BooleanProperty prop : CONNECTIONS.values()) state = state.setValue(prop, false);
		this.registerDefaultState(state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(STYLE);
		CONNECTIONS.values().forEach(builder::add);
	}

	public static int connectionMask(BlockState state) {
		int mask = 0;
		for(Direction dir : Direction.values()) if(state.getValue(CONNECTIONS.get(dir))) mask |= 1 << dir.ordinal();
		return mask;
	}

	public static FluidType getType(BlockGetter world, BlockPos pos) {
		return world.getBlockEntity(pos) instanceof TileEntityPipeBaseNT pipe ? pipe.getFluidType() : Fluids.NONE;
	}

	protected boolean canConnectTo(BlockGetter world, BlockPos pos, Direction dir) {
		return Library.canConnectFluid(world, pos.relative(dir), dir, getType(world, pos));
	}

	protected BlockState withConnections(BlockGetter world, BlockPos pos, BlockState state) {
		for(Direction dir : Direction.values()) state = state.setValue(CONNECTIONS.get(dir), canConnectTo(world, pos, dir));
		return state;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return withConnections(context.getLevel(), context.getClickedPos(), this.defaultBlockState());
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction dir, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
		return state.setValue(CONNECTIONS.get(dir), canConnectTo(world, pos, dir));
	}

	/** Machines only get their tile entity after the block update, and pipe types change without one: recheck */
	@Override
	protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
		super.neighborChanged(state, world, pos, neighborBlock, neighborPos, movedByPiston);
		refreshConnections(world, pos);
	}

	/** Recomputes the connections and tells the neighbors (their connection to this pipe may have changed too) */
	public static void refreshConnections(Level world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if(!(state.getBlock() instanceof FluidDuctStandard duct)) return;
		BlockState updated = duct.withConnections(world, pos, state);
		if(updated != state) world.setBlock(pos, updated, Block.UPDATE_ALL);
	}

	/** Sets the pipe's type, the connections of it and its neighbors follow */
	public static void setType(Level world, BlockPos pos, FluidType type) {
		if(world.getBlockEntity(pos) instanceof TileEntityPipeBaseNT pipe) {
			pipe.setType(type);
			refreshConnections(world, pos);
			world.updateNeighborsAt(pos, world.getBlockState(pos).getBlock());
		}
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

		if(stack.getItem() instanceof IItemFluidIdentifier id) {

			if(world.getBlockEntity(pos) instanceof TileEntityPipeBaseNT pipe) {
				FluidType type = id.getType(world, pos, stack);

				if(!world.isClientSide) {
					if(!player.isShiftKeyDown()) {
						if(pipe.getFluidType() != type) setType(world, pos, type);
					} else {
						// sneaking changes the whole connected pipe line of the same type (up to 64 pipes deep)
						changeTypeRecursively(world, pos, pipe.getFluidType(), type, 64);
					}
				}
				return ItemInteractionResult.sidedSuccess(world.isClientSide);
			}
		}

		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public void changeTypeRecursively(Level world, BlockPos pos, FluidType prevType, FluidType type, int loopsRemaining) {

		if(world.getBlockEntity(pos) instanceof TileEntityPipeBaseNT pipe) {

			if(pipe.getFluidType() == prevType && pipe.getFluidType() != type) {
				setType(world, pos, type);

				if(loopsRemaining > 0) {
					for(Direction dir : Direction.values()) {
						BlockPos next = pos.relative(dir);
						if(world.getBlockState(next).getBlock() instanceof IBlockFluidDuct duct) {
							duct.changeTypeRecursively(world, next, prevType, type, loopsRemaining - 1);
						}
					}
				}
			}
		}
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return SHAPES[connectionMask(state)];
	}

	/** Pick block keeps the style (the original returned the deprecated fluid_duct item with the fluid) */
	@Override
	public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state) {
		return styled(this, state.getValue(STYLE));
	}

	public static ItemStack styled(Block block, int style) {
		ItemStack stack = new ItemStack(block);
		if(style != 0) stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(STYLE, style));
		return stack;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityPipeBaseNT(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return world.isClientSide || type != ModTileEntities.PIPE.get() ? null : TileEntityLoadedBase.ticker();
	}

	@Override
	public void printHook(net.minecraft.client.gui.GuiGraphics graphics, Level world, BlockPos pos) {
		if(!(world.getBlockEntity(pos) instanceof TileEntityPipeBaseNT duct)) return;
		FluidType type = duct.getFluidType();
		com.hbm.blocks.ILookOverlay.printGeneric(graphics, com.hbm.util.i18n.I18nUtil.resolveKey(getDescriptionId()), 0xffff00, 0x404000,
				java.util.List.of("&[" + type.getColor() + "&]" + type.getLocalizedName()));
	}
}
