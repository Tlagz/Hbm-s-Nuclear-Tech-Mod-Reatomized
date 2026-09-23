package com.hbm.datagen;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.hbm.lib.RefStrings;

import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SpriteSourceProvider;

/**
 * The original keeps its textures in textures/blocks and textures/items, which the modern block atlas
 * does not scan. Instead of adding the whole folders (thousands of sprites, some with odd sizes that
 * would disable mipmapping for the entire atlas), only textures referenced by generated models are added.
 */
public class ModSpriteSourceProvider extends SpriteSourceProvider {

	/** Filled by the model providers, which have to run before this one. */
	public static final Set<ResourceLocation> USED_TEXTURES = new LinkedHashSet<>();

	public ModSpriteSourceProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper efh) {
		super(output, lookup, RefStrings.MODID, efh);
	}

	@Override
	protected void gather() {
		SourceList blocks = atlas(BLOCKS_ATLAS);
		USED_TEXTURES.forEach(tex -> blocks.addSource(new SingleFile(tex, Optional.empty())));
	}
}
