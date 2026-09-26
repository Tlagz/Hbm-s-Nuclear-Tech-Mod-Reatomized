package com.hbm.blocks.machine;

import com.hbm.tileentity.ModTileEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * Bricked furnace. The original had separate _off and _on blocks swapped while burning, now it's one block with a
 * LIT state (like the electric furnace).
 */
public class MachineBrickFurnace extends BlockMachineTile {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	public MachineBrickFurnace(Properties properties) {
		super(properties.lightLevel(state -> state.getValue(LIT) ? 13 : 0), ModTileEntities.FURNACE_BRICK);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	/** Switches the lit state, keeps the tile (the original had to preserve it while swapping blocks) */
	public static void updateBlockState(boolean isProcessing, Level world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if(state.hasProperty(LIT) && state.getValue(LIT) != isProcessing) {
			world.setBlock(pos, state.setValue(LIT, isProcessing), Block.UPDATE_ALL);
		}
	}

	/** Flames and smoke at the door while lit */
	@Override
	public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
		if(!state.getValue(LIT)) return;

		Direction dir = state.getValue(FACING);
		double cX = pos.getX() + 0.5;
		double cY = pos.getY() + rand.nextFloat() * 0.375F;
		double cZ = pos.getZ() + 0.5;
		double off = 0.52;
		double var = rand.nextFloat() * 0.6F - 0.3F;

		double x = cX + dir.getStepX() * off + (dir.getAxis() == Direction.Axis.Z ? var : 0);
		double z = cZ + dir.getStepZ() * off + (dir.getAxis() == Direction.Axis.X ? var : 0);

		world.addParticle(ParticleTypes.SMOKE, x, cY, z, 0.0D, 0.0D, 0.0D);
		world.addParticle(ParticleTypes.FLAME, x, cY, z, 0.0D, 0.0D, 0.0D);
	}
}
