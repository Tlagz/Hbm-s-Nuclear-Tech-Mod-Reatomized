package com.hbm.blocks;

import java.util.HashSet;
import java.util.Set;

import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.tileentity.TileEntityLoadedBase;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Multiblock machines: one core block with the tile entity, surrounded by dummy blocks of the same type.
 * The original stored everything in the block metadata, which is kept as the META state (0-15):
 * <ul>
 * <li>0-5: dummy, the direction from its parent (towards the core it's the opposite)
 * <li>6-11: dummy with the "extra" flag (connection points, usually with a proxy tile)
 * <li>12-15: the core, facing (meta - offset) as a Direction ordinal (2 north, 3 south, 4 west, 5 east)
 * </ul>
 * Everything is rendered by the core's tile entity renderer, the blocks themselves are invisible.
 *
 * TODO placement preview, copy/paste settings, NBT structure transforms
 */
public abstract class BlockDummyable extends Block implements EntityBlock {

	public static final IntegerProperty META = IntegerProperty.create("meta", 0, 15);

	// meta offset from dummy to TE rotation
	public static final int offset = 10;
	// meta offset from dummy to extra rotation
	public static final int extra = 6;

	public static boolean safeRem = false;

	public BlockDummyable(Properties properties) {
		super(properties.noOcclusion());
		this.registerDefaultState(this.stateDefinition.any().setValue(META, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(META);
	}

	public static int getMeta(BlockState state) {
		return state.hasProperty(META) ? state.getValue(META) : 0;
	}

	/** The facing of a core block, like the original's getBlockMetadata() - offset */
	public static Direction getRotation(BlockState state) {
		int meta = getMeta(state);
		return meta >= 12 ? Direction.from3DDataValue(meta - offset) : Direction.NORTH;
	}

	/// DETAILED HITBOXES ///

	/**
	 * The original's detailed hitboxes: boxes relative to the bottom center of the core, for a multiblock facing
	 * north, turned with it. Every block of the multiblock uses the part of them inside its own space. Empty means
	 * full blocks.
	 */
	public final java.util.List<AABB> bounding = new java.util.ArrayList<>();

	/** The original's getAABBRotationOffset, rot is the facing turned clockwise */
	public static AABB rotateBox(AABB aabb, Direction rot) {
		return switch(rot) {
		case EAST -> new AABB(-aabb.maxZ, aabb.minY, aabb.minX, -aabb.minZ, aabb.maxY, aabb.maxX);
		case SOUTH -> new AABB(-aabb.maxX, aabb.minY, -aabb.maxZ, -aabb.minX, aabb.maxY, -aabb.minZ);
		case WEST -> new AABB(aabb.minZ, aabb.minY, -aabb.maxX, aabb.maxZ, aabb.maxY, -aabb.minX);
		default -> aabb;
		};
	}

	private static final AABB UNIT = new AABB(0, 0, 0, 1, 1, 1);

	protected VoxelShape detailedShape(BlockGetter world, BlockPos pos) {
		BlockPos core = findCore(world, pos);
		if(core == null) return Shapes.block();
		Direction rot = getRotation(world.getBlockState(core)).getClockWise();

		VoxelShape shape = Shapes.empty();
		for(AABB box : bounding) {
			AABB placed = rotateBox(box, rot).move(core.getX() + 0.5 - pos.getX(), core.getY() - pos.getY(), core.getZ() + 0.5 - pos.getZ());
			if(placed.intersects(UNIT)) shape = Shapes.or(shape, Shapes.create(placed.intersect(UNIT)));
		}
		return shape;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return bounding.isEmpty() ? Shapes.block() : detailedShape(world, pos);
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return bounding.isEmpty() ? Shapes.block() : detailedShape(world, pos);
	}

	/// TILE ENTITIES ///

	/** The original's createNewTileEntity(world, meta): the core (meta >= 12), a proxy or null */
	public abstract BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta);

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return createNewTileEntity(pos, state, getMeta(state));
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return getMeta(state) >= 12 ? (level, pos, st, tile) -> { if(tile instanceof TileEntityLoadedBase loaded) loaded.updateEntity(); } : null;
	}

	/// STRUCTURE ///

	/** @return six fields, the amount of dummy blocks around the core: UP, DOWN, FORWARD, BACKWARD, LEFT, RIGHT */
	public abstract int[] getDimensions();

	public abstract int getOffset();

	public int getHeightOffset() {
		return 0;
	}

	@Override
	protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
		super.neighborChanged(state, world, pos, block, fromPos, moving);

		if(safeRem) return;

		destroyIfOrphan(world, pos);
	}

	private void destroyIfOrphan(Level world, BlockPos pos) {
		if(world.isClientSide) return;

		int metadata = getMeta(world.getBlockState(pos));

		if(metadata >= 12) return;

		// if it's an extra, remove the extra-ness
		if(metadata >= extra)
			metadata -= extra;

		Direction dir = Direction.from3DDataValue(metadata).getOpposite();

		if(world.getBlockState(pos.relative(dir)).getBlock() != this && world.hasChunksAt(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
			world.removeBlock(pos, false);
		}
	}

	/** Finds the core by following the dummies' directions, null if there is none */
	public BlockPos findCore(BlockGetter world, BlockPos start) {
		Set<BlockPos> visited = new HashSet<>();
		BlockPos pos = start;

		while(true) {
			BlockState state = world.getBlockState(pos);
			if(state.getBlock() != this) return null;

			int metadata = getMeta(state);
			if(metadata >= 12) return pos;
			if(metadata >= extra) metadata -= extra;

			if(!visited.add(pos)) return null;

			pos = pos.relative(Direction.from3DDataValue(metadata).getOpposite());
		}
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {

		if(!(placer instanceof Player player))
			return;

		safeRem = true;
		world.removeBlock(pos, false);
		safeRem = false;

		int o = -getOffset();
		BlockPos base = pos.above(getHeightOffset());

		// the machine faces the player
		Direction dir = getDirModified(player.getDirection().getOpposite());
		BlockPos core = base.relative(dir, o);

		if(!checkRequirement(world, core, base, dir)) {

			if(!player.isCreative()) {
				ItemStack refund = new ItemStack(this);
				if(!player.getInventory().add(refund)) player.drop(refund, false);
			}

			return;
		}

		if(!world.isClientSide) {
			int meta = getMetaForCore(world, core, player, dir.get3DDataValue() + offset);
			world.setBlock(core, this.defaultBlockState().setValue(META, meta), Block.UPDATE_ALL);
			fillSpace(world, base, dir, o);
		}

		super.setPlacedBy(world, pos, state, placer, itemStack);
	}

	/**
	 * Builds the whole multiblock without a player, as if it was placed at pos facing dir.
	 * @return the core position, null if there was no space
	 */
	public BlockPos placeMultiblock(Level world, BlockPos pos, Direction dir) {
		int o = -getOffset();
		BlockPos base = pos.above(getHeightOffset());
		BlockPos core = base.relative(dir, o);

		if(!checkRequirement(world, core, base, dir)) return null;

		world.setBlock(core, this.defaultBlockState().setValue(META, dir.get3DDataValue() + offset), Block.UPDATE_ALL);
		fillSpace(world, base, dir, o);
		return core;
	}

	/** Hook for blocks whose core meta differs from the placement direction */
	protected int getMetaForCore(Level world, BlockPos pos, Player player, int original) {
		return original;
	}

	/** Allows to modify the general placement direction as if the player had another rotation */
	protected Direction getDirModified(Direction dir) {
		return dir;
	}

	protected boolean checkRequirement(Level world, BlockPos core, BlockPos placed, Direction dir) {
		return MultiblockHandlerXR.checkSpace(world, core, getDimensions(), placed, dir);
	}

	/** Places the dummies, pos is where the player placed the block, o the core offset along dir */
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		MultiblockHandlerXR.fillSpace(world, pos.relative(dir, o), getDimensions(), this, dir);
	}

	/** "upgrades" regular dummy blocks to ones with the extra flag */
	public void makeExtra(Level world, BlockPos pos) {

		BlockState state = world.getBlockState(pos);
		if(state.getBlock() != this)
			return;

		int meta = getMeta(state);

		if(meta > 5)
			return;

		safeRem = true;
		// set to air first so the tile entity gets recreated (dummies become proxies)
		world.removeBlock(pos, false);
		world.setBlock(pos, state.setValue(META, meta + extra), Block.UPDATE_ALL);
		safeRem = false;
	}

	public void removeExtra(Level world, BlockPos pos) {

		BlockState state = world.getBlockState(pos);
		if(state.getBlock() != this)
			return;

		int meta = getMeta(state);

		if(meta <= 5 || meta >= 12)
			return;

		safeRem = true;
		world.removeBlock(pos, false);
		world.setBlock(pos, state.setValue(META, meta - extra), Block.UPDATE_ALL);
		safeRem = false;
	}

	// checks if the dummy metadata is within the extra range
	public boolean hasExtra(int meta) {
		return meta > 5 && meta < 12;
	}

	@Override
	protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston) {
		if(state.is(newState.getBlock())) {
			super.onRemove(state, world, pos, newState, movedByPiston);
			return;
		}

		int i = getMeta(state);

		if(i < 12 && !safeRem) {
			if(i >= extra) i -= extra;

			Direction d = Direction.from3DDataValue(i);
			BlockPos parent = pos.relative(d.getOpposite());

			if(world.getBlockState(parent).getBlock() == this)
				world.removeBlock(parent, false);
		}

		if(world.getBlockEntity(pos) instanceof Container container && i >= 12) {
			Containers.dropContents(world, pos, container);
			world.updateNeighbourForOutputSignal(pos, this);
		}

		super.onRemove(state, world, pos, newState, movedByPiston);
	}

	/**
	 * The original's onBlockHarvested: only the block the player breaks drops the machine, with the core's
	 * persistent data (IPersistentNBT) read before the chain removal takes the core away. The loot tables of
	 * dummyables are empty, the rest of the structure disappears without drops.
	 */
	@Override
	public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {

		if(!world.isClientSide && !player.isCreative()) {
			for(ItemStack drop : getHarvestDrops(world, findCore(world, pos))) Block.popResource(world, pos, drop);
		}

		return super.playerWillDestroy(world, pos, state, player);
	}

	/** What breaking the machine drops, by default the machine with the core's persistent data */
	protected java.util.List<ItemStack> getHarvestDrops(Level world, BlockPos core) {
		ItemStack drop = new ItemStack(this);
		if(core != null && world.getBlockEntity(core) != null) {
			drop.applyComponents(world.getBlockEntity(core).collectComponents());
		}
		return java.util.List.of(drop);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	/** Opens the core's GUI, the original's standardOpenBehavior */
	protected InteractionResult standardOpenBehavior(Level world, BlockPos pos, Player player) {

		if(player.isShiftKeyDown()) return InteractionResult.PASS;

		if(!world.isClientSide && player instanceof ServerPlayer serverPlayer) {
			BlockPos core = this.findCore(world, pos);
			if(core == null) return InteractionResult.FAIL;

			if(world.getBlockEntity(core) instanceof MenuProvider provider) {
				serverPlayer.openMenu(provider, buf -> buf.writeBlockPos(core));
			}
		}

		return InteractionResult.sidedSuccess(world.isClientSide);
	}

	/** The original's MathHelper based yaw to direction, kept for machines that need it */
	public static int getPlayerYawIndex(Player player) {
		return Mth.floor(player.getYRot() * 4.0F / 360.0F + 0.5D) & 3;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return InteractionResult.PASS;
	}
}
