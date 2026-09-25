package com.hbm.blocks.generic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Steel scaffold, a 12 pixel thick panel placed upright or flat. The original stored the orientation in the upper
 * metadata bits (0-3 upright along x, 4-7 flat, 8-11 upright along z, 12-15 flat, turned) and the color in the
 * lower ones; the colors are separate blocks here (steel_scaffold_red...), all named "Steel Scaffold".
 */
public class BlockScaffold extends Block {

	/** The original's meta / 4 */
	public static final IntegerProperty ORIENTATION = IntegerProperty.create("orientation", 0, 3);

	private static final VoxelShape[] SHAPES = new VoxelShape[] {
			Block.box(0, 0, 2, 16, 16, 14),
			Block.box(0, 2, 0, 16, 14, 16),
			Block.box(2, 0, 0, 14, 16, 16),
			Block.box(0, 2, 0, 16, 14, 16)
	};

	private final String descriptionId;

	public BlockScaffold(Properties properties, String descriptionId) {
		super(properties.noOcclusion());
		this.descriptionId = descriptionId;
		this.registerDefaultState(this.stateDefinition.any().setValue(ORIENTATION, 0));
	}

	@Override
	public String getDescriptionId() {
		return descriptionId;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(ORIENTATION);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction placed = context.getClickedFace();
		int orientation;

		if(placed == Direction.UP || placed == Direction.DOWN) {
			// the original: rotation yaw quadrant, even -> along x, odd -> along z
			int rot = Math.floorMod((int) Math.floor(context.getRotation() * 4.0F / 360.0F + 0.5D), 4);
			orientation = rot % 2 == 0 ? 0 : 2;
		} else if(placed == Direction.NORTH || placed == Direction.SOUTH) {
			orientation = 1;
		} else {
			orientation = 3;
		}

		return this.defaultBlockState().setValue(ORIENTATION, orientation);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return SHAPES[state.getValue(ORIENTATION)];
	}
}
