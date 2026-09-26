package com.hbm.world.feature;

import com.hbm.blocks.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Blocks;

/**
 * Oil contamination around oil spills and fracking towers: plants die, grass and dirt turn dead or oily, sand gets
 * dirty, stone cracks, natural leaves drop.
 *
 * TODO plant_dead (dead plants instead of removing them), mustard willows
 */
public class OilSpot {

	public static void generateOilSpot(Level world, int x, int z, int width, int count, boolean addWillows) {

		for(int i = 0; i < count; i++) {
			int rX = x + (int) (world.random.nextGaussian() * width);
			int rZ = z + (int) (world.random.nextGaussian() * width);
			int rY = world.getHeight(Heightmap.Types.WORLD_SURFACE, rX, rZ);

			for(int y = rY; y > rY - 4; y--) {

				BlockPos pos = new BlockPos(rX, y, rZ);
				BlockState belowState = world.getBlockState(pos.below());
				BlockState groundState = world.getBlockState(pos);
				Block ground = groundState.getBlock();

				if(belowState.isRedstoneConductor(world, pos.below()) && ground instanceof BushBlock) {
					world.removeBlock(pos, false);
					continue;
				}

				if(ground == Blocks.GRASS_BLOCK || ground == Blocks.DIRT) {
					world.setBlockAndUpdate(pos, (world.random.nextInt(10) == 0 ? ModBlocks.dirt_oily : ModBlocks.dirt_dead).get().defaultBlockState());
					break;

				} else if(ground == Blocks.SAND || ground == ModBlocks.ore_oil_sand.get()) {
					world.setBlockAndUpdate(pos, ModBlocks.sand_dirty.get().defaultBlockState());
					break;

				} else if(ground == Blocks.RED_SAND) {
					world.setBlockAndUpdate(pos, ModBlocks.sand_dirty_red.get().defaultBlockState());
					break;

				} else if(ground == Blocks.STONE) {
					world.setBlockAndUpdate(pos, ModBlocks.stone_cracked.get().defaultBlockState());
					break;

				} else if(groundState.is(BlockTags.LEAVES) && groundState.hasProperty(LeavesBlock.PERSISTENT) && !groundState.getValue(LeavesBlock.PERSISTENT)) {
					world.removeBlock(pos, false);
					break;
				}
			}
		}
	}
}
