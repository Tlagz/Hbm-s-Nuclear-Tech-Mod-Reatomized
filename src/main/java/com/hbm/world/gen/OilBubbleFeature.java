package com.hbm.world.gen;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.WorldConfig;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Oil deposits, the original's MapGenBubble: an oblate sphere (3 x 1 x 3) of ore_oil replacing stone, radius
 * 8-15, centered at y 15-39, one every "oilSpawn" chunks (three times as often in hot and dry biomes), with a
 * patch of oily dirt / cracked stone on the surface above it.
 * The original generated during terrain generation, as a feature it's limited to the neighboring chunks, the
 * sizes keep it within that.
 *
 * TODO dead plants and the oil spill hole on the surface (blocks not ported), oil sand bubbles in deserts
 */
public class OilBubbleFeature extends Feature<NoneFeatureConfiguration> {

	public int minSize = 8;
	public int maxSize = 16;
	public int minY = 15;
	public int rangeY = 25;

	public OilBubbleFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel world = context.level();
		RandomSource rand = context.random();
		BlockPos origin = context.origin();

		int effecFreq = WorldConfig.getRate("oilSpawn", "overworld");
		if(effecFreq <= 0) return false;
		Biome biome = world.getBiome(origin).value();
		if(biome.getBaseTemperature() >= 2 && !biome.hasPrecipitation()) effecFreq /= 3;
		if(effecFreq <= 0) effecFreq = 1;

		if(rand.nextInt(effecFreq) != effecFreq - 1) return false;

		int yCoord = rand.nextInt(rangeY) + minY;
		double radius = rand.nextInt(maxSize - minSize) + minSize;
		double radiusSqr = (radius * radius) / 2; // original OilBubble implementation divided the square by 2 for some reason

		int horizontal = Mth.ceil(Math.sqrt(radiusSqr));
		int vertical = Mth.ceil(Math.sqrt(radiusSqr / 3));
		BlockState oil = ModBlocks.ore_oil.get().defaultBlockState();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		for(int x = -horizontal; x <= horizontal; x++)
		for(int z = -horizontal; z <= horizontal; z++)
		for(int y = -vertical; y <= vertical; y++) {
			double rSqr = x * x + z * z + y * y * 3;
			if(rSqr >= radiusSqr) continue;

			pos.set(origin.getX() + x, yCoord + y, origin.getZ() + z);
			if(world.getBlockState(pos).is(Blocks.STONE)) {
				world.setBlock(pos, oil, Block.UPDATE_CLIENTS);
			}
		}

		addSurfaceSpot(world, rand, origin);
		return true;
	}

	protected void addSurfaceSpot(WorldGenLevel world, RandomSource rand, BlockPos origin) {

		int spotCount = 150;
		int spotWidth = 7;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		// Add oil spot damage
		for(int i = 0; i < spotCount; i++) {
			// clamped so the spot stays within the chunks a feature may touch
			int offX = Mth.clamp((int) (rand.nextGaussian() * spotWidth), -15, 15);
			int offZ = Mth.clamp((int) (rand.nextGaussian() * spotWidth), -15, 15);
			int x = origin.getX() + offX;
			int z = origin.getZ() + offZ;
			int ground = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;

			int distSq = offX * offX + offZ * offZ;
			boolean inner = distSq < (spotWidth / 2) * (spotWidth / 2);

			for(int oy = 1; oy > -3; oy--) {
				pos.set(x, ground + oy, z);
				BlockState state = world.getBlockState(pos);

				if(state.is(Blocks.GRASS_BLOCK) || state.is(BlockTags.DIRT) && !state.is(Blocks.MUD)) {
					world.setBlock(pos, (inner ? ModBlocks.dirt_oily : ModBlocks.dirt_dead).get().defaultBlockState(), Block.UPDATE_CLIENTS);
					break;
				} else if(state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
					world.setBlock(pos, (state.is(Blocks.RED_SAND) ? ModBlocks.sand_dirty_red : ModBlocks.sand_dirty).get().defaultBlockState(), Block.UPDATE_CLIENTS);
					break;
				} else if(state.is(Blocks.STONE)) {
					world.setBlock(pos, ModBlocks.stone_cracked.get().defaultBlockState(), Block.UPDATE_CLIENTS);
					break;
				}
			}
		}

		// cracked stone around where the original put the oil spill hole
		for(Direction dir : Direction.Plane.HORIZONTAL) {
			int x = origin.getX() + dir.getStepX();
			int z = origin.getZ() + dir.getStepZ();
			int solids = 0;

			for(int y = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1; y > world.getMinBuildHeight(); y--) {
				pos.set(x, y, z);
				BlockState state = world.getBlockState(pos);
				if(!state.getFluidState().isEmpty()) break;
				if(state.isSolidRender(world, pos)) {
					solids++;
					world.setBlock(pos, ModBlocks.stone_cracked.get().defaultBlockState(), Block.UPDATE_CLIENTS);
					if(solids >= 4) break;
				}
			}
		}
	}
}
