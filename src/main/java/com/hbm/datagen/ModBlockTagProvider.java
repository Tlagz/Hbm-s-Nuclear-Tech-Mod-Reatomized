package com.hbm.datagen;

import java.util.concurrent.CompletableFuture;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockTagProvider extends BlockTagsProvider {

	public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper efh) {
		super(output, lookup, RefStrings.MODID, efh);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		// the original has no harvest levels on these, any pickaxe works
		tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
				ModBlocks.ore_uranium.get(),
				ModBlocks.ore_titanium.get(),
				ModBlocks.block_uranium.get(),
				ModBlocks.block_titanium.get(),
				ModBlocks.block_steel.get());
	}
}
