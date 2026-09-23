package com.hbm.world.gen;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.hbm.config.WorldConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * Like minecraft:count, but the count comes from the NTM world config (e.g. "uraniumSpawn"), so the
 * original's ore spawn rate options keep working.
 */
public class ConfigCountPlacement extends PlacementModifier {

	public static final MapCodec<ConfigCountPlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.STRING.fieldOf("config").forGetter(p -> p.config),
			Codec.STRING.fieldOf("dimension").forGetter(p -> p.dimension)
	).apply(instance, ConfigCountPlacement::new));

	private final String config;
	private final String dimension;

	public ConfigCountPlacement(String config, String dimension) {
		this.config = config;
		this.dimension = dimension;
	}

	@Override
	public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
		return IntStream.range(0, WorldConfig.getRate(config, dimension)).mapToObj(i -> pos);
	}

	@Override
	public PlacementModifierType<?> type() {
		return ModWorldGen.CONFIG_COUNT.get();
	}
}
