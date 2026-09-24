package com.hbm.datagen;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.hbm.lib.RefStrings;
import com.hbm.world.gen.ConfigCountPlacement;
import com.hbm.world.gen.ModWorldGen;
import com.hbm.world.gen.ModWorldGen.OreVein;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Ore features and the biome modifiers that add them, generated from {@link ModWorldGen#ORES}.
 * Heights are the original's absolute Y values (minY + random(range)).
 */
public class ModWorldGenProvider extends DatapackBuiltinEntriesProvider {

	private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
			.add(Registries.CONFIGURED_FEATURE, ModWorldGenProvider::configured)
			.add(Registries.PLACED_FEATURE, ModWorldGenProvider::placed)
			.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModWorldGenProvider::biomeModifiers);

	public ModWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, BUILDER, Set.of(RefStrings.MODID));
	}

	private static ResourceKey<ConfiguredFeature<?, ?>> configuredKey(OreVein vein) {
		return ResourceKey.create(Registries.CONFIGURED_FEATURE, RefStrings.loc("ore_" + vein.name()));
	}

	private static ResourceKey<PlacedFeature> placedKey(OreVein vein) {
		return ResourceKey.create(Registries.PLACED_FEATURE, RefStrings.loc("ore_" + vein.name()));
	}

	private static RuleTest target(ModWorldGen.Target target) {
		return switch(target) {
			case STONE -> new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
			case NETHERRACK -> new BlockMatchTest(Blocks.NETHERRACK);
			case END_STONE -> new BlockMatchTest(Blocks.END_STONE);
		};
	}

	private static void configured(BootstrapContext<ConfiguredFeature<?, ?>> context) {
		context.register(OIL_BUBBLE_CONFIGURED, new ConfiguredFeature<>(ModWorldGen.OIL_BUBBLE.get(), net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.INSTANCE));
		for(OreVein vein : ModWorldGen.ORES) {
			context.register(configuredKey(vein), new ConfiguredFeature<>(Feature.ORE,
					new OreConfiguration(List.of(OreConfiguration.target(target(vein.target()), vein.ore().get().defaultBlockState())), vein.size())));
		}
	}

	private static final ResourceKey<ConfiguredFeature<?, ?>> OIL_BUBBLE_CONFIGURED = ResourceKey.create(Registries.CONFIGURED_FEATURE, RefStrings.loc("oil_bubble"));
	private static final ResourceKey<PlacedFeature> OIL_BUBBLE_PLACED = ResourceKey.create(Registries.PLACED_FEATURE, RefStrings.loc("oil_bubble"));

	private static void placed(BootstrapContext<PlacedFeature> context) {
		HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);
		// oil bubbles: the feature rolls its own chance (oilSpawn config) and height
		context.register(OIL_BUBBLE_PLACED, new PlacedFeature(configured.getOrThrow(OIL_BUBBLE_CONFIGURED), List.of(InSquarePlacement.spread(), BiomeFilter.biome())));
		for(OreVein vein : ModWorldGen.ORES) {
			context.register(placedKey(vein), new PlacedFeature(configured.getOrThrow(configuredKey(vein)), List.of(
					new ConfigCountPlacement(vein.config(), vein.dimension()),
					InSquarePlacement.spread(),
					HeightRangePlacement.uniform(VerticalAnchor.absolute(vein.minY()), VerticalAnchor.absolute(vein.minY() + vein.range() - 1)),
					BiomeFilter.biome())));
		}
	}

	private static void biomeModifiers(BootstrapContext<BiomeModifier> context) {
		HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
		HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

		context.register(ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, RefStrings.loc("oil_overworld")),
				new BiomeModifiers.AddFeaturesBiomeModifier(biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
						HolderSet.direct(placed.getOrThrow(OIL_BUBBLE_PLACED)), GenerationStep.Decoration.UNDERGROUND_ORES));

		for(String dimension : List.of("overworld", "nether", "end")) {
			List<OreVein> veins = ModWorldGen.ORES.stream().filter(v -> v.dimension().equals(dimension)).toList();
			if(veins.isEmpty()) continue;

			var biomeTag = switch(dimension) {
				case "nether" -> BiomeTags.IS_NETHER;
				case "end" -> BiomeTags.IS_END;
				default -> BiomeTags.IS_OVERWORLD;
			};

			context.register(ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, RefStrings.loc("ores_" + dimension)),
					new BiomeModifiers.AddFeaturesBiomeModifier(biomes.getOrThrow(biomeTag),
							HolderSet.direct(veins.stream().map(v -> placed.getOrThrow(placedKey(v))).toList()),
							GenerationStep.Decoration.UNDERGROUND_ORES));
		}
	}
}
