package com.hbm.blocks.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.tileentity.ModTileEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * Alloy furnace. The original swapped between an on and an off block, here it's the LIT state; EXTENDED is set while
 * the extension sits on top (the tall textures, the original checked the block above in getIcon).
 */
public class MachineDiFurnace extends BlockMachineTile {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty LIT = BlockStateProperties.LIT;
	public static final BooleanProperty EXTENDED = BooleanProperty.create("extended");

	public MachineDiFurnace(Properties properties) {
		super(properties.lightLevel(state -> state.getValue(LIT) ? 13 : 0), ModTileEntities.DI_FURNACE);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false).setValue(EXTENDED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT, EXTENDED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		boolean extended = context.getLevel().getBlockState(context.getClickedPos().above()).is(ModBlocks.machine_difurnace_extension.get());
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(EXTENDED, extended);
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
		if(dir == Direction.UP) return state.setValue(EXTENDED, neighbor.is(ModBlocks.machine_difurnace_extension.get()));
		return state;
	}

	/** Switches the lit state, keeps the tile (the original had to preserve it while swapping blocks) */
	public static void updateBlockState(boolean isProcessing, Level world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if(state.hasProperty(LIT) && state.getValue(LIT) != isProcessing) {
			world.setBlock(pos, state.setValue(LIT, isProcessing), Block.UPDATE_ALL);
		}
	}

	/** Flames at the door and smoke on top (on top of the extension if there is one) */
	@Override
	public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
		if(!state.getValue(LIT)) return;

		Direction dir = state.getValue(FACING);
		float x0 = pos.getX() + 0.5F;
		float y0 = pos.getY() + 0.25F + rand.nextFloat() * 6.0F / 16.0F;
		float z0 = pos.getZ() + 0.5F;
		float sideOff = 0.52F;
		float sideRand = rand.nextFloat() * 0.5F - 0.25F;
		float xOff = rand.nextFloat() * 0.375F + 0.3125F;
		float zOff = rand.nextFloat() * 0.375F + 0.3125F;
		int top = pos.getY() + (state.getValue(EXTENDED) ? 2 : 1);

		double x = x0 + dir.getStepX() * sideOff + (dir.getAxis() == Direction.Axis.Z ? sideRand : 0);
		double z = z0 + dir.getStepZ() * sideOff + (dir.getAxis() == Direction.Axis.X ? sideRand : 0);

		world.addParticle(ParticleTypes.FLAME, x, y0, z, 0.0D, 0.0D, 0.0D);
		world.addParticle(ParticleTypes.SMOKE, pos.getX() + xOff, top, pos.getZ() + zOff, 0.0D, 0.0D, 0.0D);
	}
}
