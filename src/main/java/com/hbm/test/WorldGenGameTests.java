package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;
import com.hbm.world.gen.ModWorldGen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Ore generation data is loaded and the ore features actually place ore */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class WorldGenGameTests {

	@GameTest(template = "empty_8x4x8")
	public static void oreFeaturesAreRegistered(GameTestHelper helper) {
		var placed = helper.getLevel().registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
		for(ModWorldGen.OreVein vein : ModWorldGen.ORES) {
			helper.assertTrue(placed.containsKey(RefStrings.loc("ore_" + vein.name())), "placed feature ore_" + vein.name() + " missing");
		}

		// the biome modifiers have to have added them to the biomes
		var plains = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS).value();
		boolean hasUranium = plains.getGenerationSettings().features().stream().flatMap(set -> set.stream())
				.anyMatch(f -> f.unwrapKey().map(k -> k.location().equals(RefStrings.loc("ore_uranium"))).orElse(false));
		helper.assertTrue(hasUranium, "plains should generate uranium ore");

		var crimson = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.CRIMSON_FOREST).value();
		boolean hasNetherSulfur = crimson.getGenerationSettings().features().stream().flatMap(set -> set.stream())
				.anyMatch(f -> f.unwrapKey().map(k -> k.location().equals(RefStrings.loc("ore_nether_sulfur"))).orElse(false));
		helper.assertTrue(hasNetherSulfur, "nether biomes should generate nether sulfur");
		helper.succeed();
	}

	@GameTest(template = "empty_8x4x8")
	public static void uraniumFeaturePlacesOre(GameTestHelper helper) {
		for(int x = 0; x < 8; x++) for(int y = 0; y < 4; y++) for(int z = 0; z < 8; z++) {
			helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
		}

		ConfiguredFeature<?, ?> feature = helper.getLevel().registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE)
				.get(ResourceKey.create(Registries.CONFIGURED_FEATURE, RefStrings.loc("ore_uranium")));
		helper.assertTrue(feature != null, "configured feature missing");

		int placed = 0;
		for(int attempt = 0; attempt < 20 && placed == 0; attempt++) {
			feature.place(helper.getLevel(), helper.getLevel().getChunkSource().getGenerator(), helper.getLevel().random, helper.absolutePos(new BlockPos(4, 2, 4)));
			for(int x = 0; x < 8; x++) for(int y = 0; y < 4; y++) for(int z = 0; z < 8; z++) {
				if(helper.getBlockState(new BlockPos(x, y, z)).is(ModBlocks.ore_uranium.get())) placed++;
			}
		}

		helper.assertTrue(placed > 0, "no uranium ore was placed");
		helper.succeed();
	}
}
