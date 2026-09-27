package com.hbm.blocks.network;

import java.util.List;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.entity.item.EntityMovingItem;

import api.hbm.conveyor.IConveyorBelt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Conveyor belts. FACING is the original's metadata 2-5: the side items come in from, they travel towards the
 * opposite side. Items dropped on a belt become EntityMovingItems snapped onto it.
 */
public abstract class BlockConveyorBase extends Block implements IConveyorBelt, ITooltipProvider {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	protected static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 4, 16);

	public BlockConveyorBase(Properties properties) {
		super(properties.noOcclusion());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	/** The original's metadata: facing (2-5), plus 4 or 8 for bent belts */
	public int getMeta(BlockState state) {
		return state.getValue(FACING).get3DDataValue();
	}

	/** The state for one of the original's metadata values */
	public BlockState getStateForMeta(int meta) {
		Direction dir = Direction.from3DDataValue(Math.max(2, Math.min(5, meta)));
		return this.defaultBlockState().setValue(FACING, dir);
	}

	/** Neighbor-derived properties (chutes and lifts), set blocks don't update their own state */
	public BlockState withConnections(BlockState state, BlockGetter world, BlockPos pos) {
		return state;
	}

	/** The state for the metadata at that position, what the wand and the screwdriver place */
	public BlockState getPlacementState(BlockGetter world, BlockPos pos, int meta) {
		return withConnections(getStateForMeta(meta), world, pos);
	}

	/** Items travel the way the player looks when placing */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return getStateForMeta(getFacingMeta(context.getRotation()));
	}

	/** The original's yaw to metadata table: looking south gives 2 (items move south) */
	public static int getFacingMeta(float yaw) {
		int i = Mth.floor(yaw * 4.0F / 360.0F + 0.5D) & 3;
		switch(i) {
		case 0: return 2;
		case 1: return 5;
		case 2: return 3;
		case 3: return 4;
		}
		return 2;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public boolean canItemStay(Level world, BlockPos pos, Vec3 itemPos) {
		return true;
	}

	@Override
	public Vec3 getTravelLocation(Level world, BlockPos pos, Vec3 itemPos, double speed) {

		Direction dir = this.getTravelDirection(world, pos, itemPos);
		// the original's snapping clamped the passed position in place, the rest works on the clamped one
		itemPos = clampToBlock(pos, itemPos);
		//snapping point
		Vec3 snap = this.getClosestSnappingPosition(world, pos, itemPos);
		//snapping point + speed
		Vec3 dest = new Vec3(snap.x - dir.getStepX() * speed, snap.y - dir.getStepY() * speed, snap.z - dir.getStepZ() * speed);
		//delta to get to that point
		Vec3 motion = new Vec3((dest.x - itemPos.x), (dest.y - itemPos.y), (dest.z - itemPos.z));
		double len = motion.length();
		//the effective destination towards "dest" after taking speed into consideration
		return new Vec3(itemPos.x + motion.x / len * speed, itemPos.y + motion.y / len * speed, itemPos.z + motion.z / len * speed);
	}

	public static Vec3 clampToBlock(BlockPos pos, Vec3 itemPos) {
		return new Vec3(Mth.clamp(itemPos.x, pos.getX(), pos.getX() + 1), itemPos.y, Mth.clamp(itemPos.z, pos.getZ(), pos.getZ() + 1));
	}

	public Direction getInputDirection(Level world, BlockPos pos) {
		return Direction.from3DDataValue(getMeta(world.getBlockState(pos)));
	}

	public Direction getOutputDirection(Level world, BlockPos pos) {
		return getInputDirection(world, pos).getOpposite();
	}

	public Direction getTravelDirection(Level world, BlockPos pos, Vec3 itemPos) {
		return Direction.from3DDataValue(getMeta(world.getBlockState(pos)));
	}

	@Override
	public Vec3 getClosestSnappingPosition(Level world, BlockPos pos, Vec3 itemPos) {

		Direction dir = this.getTravelDirection(world, pos, itemPos);

		double ix = Mth.clamp(itemPos.x, pos.getX(), pos.getX() + 1);
		double iz = Mth.clamp(itemPos.z, pos.getZ(), pos.getZ() + 1);

		double posX = pos.getX() + 0.5;
		double posZ = pos.getZ() + 0.5;

		if(dir.getStepX() != 0) {
			posX = ix;
		}
		if(dir.getStepZ() != 0) {
			posZ = iz;
		}

		return new Vec3(posX, pos.getY() + 0.25, posZ);
	}

	/** Dropped items get onto the belt */
	@Override
	protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {

		if(!world.isClientSide) {

			if(entity instanceof ItemEntity itemEntity && entity.tickCount > 10 && entity.isAlive()) {

				EntityMovingItem item = new EntityMovingItem(world);
				item.setItemStack(itemEntity.getItem().copy());
				Vec3 snap = this.getClosestSnappingPosition(world, pos, entity.position());
				item.moveTo(snap.x, snap.y, snap.z, 0, 0);
				world.addFreshEntity(item);

				entity.discard();
			}
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		this.addStandardInfo(list);
	}
}
