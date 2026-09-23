package com.hbm.datagen;

import java.util.Set;

import com.hbm.blocks.ModBlocks;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public class ModBlockLootProvider extends BlockLootSubProvider {

	public ModBlockLootProvider(HolderLookup.Provider registries) {
		super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
	}

	@Override
	protected void generate() {
		dropSelf(ModBlocks.ore_uranium.get());
		dropSelf(ModBlocks.ore_titanium.get());
		dropSelf(ModBlocks.block_uranium.get());
		dropSelf(ModBlocks.block_titanium.get());
		dropSelf(ModBlocks.block_steel.get());
		dropSelf(ModBlocks.red_cable.get());
	}

	@Override
	protected Iterable<Block> getKnownBlocks() {
		return ModBlocks.BLOCKS.getEntries().stream().<Block>map(e -> e.value())::iterator;
	}
}
