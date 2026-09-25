package com.hbm.blocks.machine;

import com.hbm.tileentity.machine.TileEntityFoundryMold;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Half block mold for the small molds */
public class FoundryMold extends FoundryCastingBase {

	private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 8, 16);

	public FoundryMold(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityFoundryMold(pos, state);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}
}
