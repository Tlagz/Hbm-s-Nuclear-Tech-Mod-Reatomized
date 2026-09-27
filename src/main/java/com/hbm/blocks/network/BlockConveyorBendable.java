package com.hbm.blocks.network;

import com.hbm.blocks.IToolable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;

/**
 * Belts that can bend: the original's metadata 6-9 (left turn) and 10-13 (right turn) are the CURVE property.
 * The screwdriver turns the belt clockwise, sneaking cycles straight, left and right.
 */
public abstract class BlockConveyorBendable extends BlockConveyorBase implements IToolable {

	public enum Curve implements StringRepresentable {
		STRAIGHT, LEFT, RIGHT;

		@Override
		public String getSerializedName() {
			return name().toLowerCase(java.util.Locale.ROOT);
		}
	}

	public static final EnumProperty<Curve> CURVE = EnumProperty.create("curve", Curve.class);

	public BlockConveyorBendable(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(CURVE, Curve.STRAIGHT));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, CURVE);
	}

	@Override
	public int getMeta(BlockState state) {
		return state.getValue(FACING).get3DDataValue() + state.getValue(CURVE).ordinal() * 4;
	}

	@Override
	public BlockState getStateForMeta(int meta) {
		int dir = getPathDirection(meta);
		return super.getStateForMeta(meta - dir * 4).setValue(CURVE, Curve.values()[dir]);
	}

	protected int getPathDirection(int meta) {
		if(meta >= 6 && meta <= 9) return 1;
		if(meta >= 10 && meta <= 13) return 2;
		return 0;
	}

	@Override
	public Direction getInputDirection(Level world, BlockPos pos) {
		return world.getBlockState(pos).getValue(FACING);
	}

	@Override
	public Direction getOutputDirection(Level world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		Direction primary = state.getValue(FACING).getOpposite();
		Curve curve = state.getValue(CURVE);

		if(curve == Curve.RIGHT) return primary.getClockWise();
		if(curve == Curve.LEFT) return primary.getCounterClockWise();
		return primary;
	}

	@Override
	public Direction getTravelDirection(Level world, BlockPos pos, Vec3 itemPos) {

		BlockState state = world.getBlockState(pos);
		int dir = state.getValue(CURVE).ordinal();
		Direction primary = state.getValue(FACING);

		if(dir > 0) {
			dir--;
			double ix = pos.getX() + 0.5;
			double iz = pos.getZ() + 0.5;
			Direction secondary = primary.getClockWise();

			ix -= -primary.getStepX() * 0.5 + secondary.getStepX() * (0.5 - dir);
			iz -= -primary.getStepZ() * 0.5 + secondary.getStepZ() * (0.5 - dir);

			double dX = Math.abs(itemPos.x - ix);
			double dZ = Math.abs(itemPos.z - iz);

			if(dX + dZ >= 1) {

				if(dir == 0)
					return secondary.getOpposite();
				else
					return secondary;
			}
		}

		return primary;
	}

	/** Screwdriver: turn clockwise, or with sneaking straight -> left -> right -> straight */
	@Override
	public boolean onScrew(Level world, Player player, BlockPos pos, Direction side, float fX, float fY, float fZ, ToolType tool) {

		if(tool != ToolType.SCREWDRIVER)
			return false;

		if(world.isClientSide) return true;

		BlockState state = world.getBlockState(pos);

		if(!player.isShiftKeyDown()) {
			world.setBlock(pos, state.setValue(FACING, state.getValue(FACING).getClockWise()), 3);
		} else {
			Curve curve = state.getValue(CURVE);
			if(curve == Curve.RIGHT && onBendPastRight(world, pos, state)) return true;
			world.setBlock(pos, state.setValue(CURVE, Curve.values()[(curve.ordinal() + 1) % 3]), 3);
		}

		return true;
	}

	/** Hook for the regular belt, which turns into a lift after the right bend; true if handled */
	protected boolean onBendPastRight(Level world, BlockPos pos, BlockState state) {
		return false;
	}
}
