package com.hbm.datagen;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {

	public ModBlockStateProvider(PackOutput output, ExistingFileHelper efh) {
		super(output, RefStrings.MODID, efh);
	}

	@Override
	protected void registerStatesAndModels() {
		cube(ModBlocks.ore_uranium);
		cube(ModBlocks.ore_titanium);
		cube(ModBlocks.block_uranium);
		cube(ModBlocks.block_titanium);
		cube(ModBlocks.block_steel);
	}

	/** Simple full cube using the original's texture location, textures/blocks/[name].png */
	private void cube(DeferredBlock<? extends Block> block) {
		String name = block.getId().getPath();
		simpleBlockWithItem(block.get(), models().cubeAll(name, texture("blocks/" + name)));
	}

	private ResourceLocation texture(String path) {
		ResourceLocation tex = modLoc(path);
		ModSpriteSourceProvider.USED_TEXTURES.add(tex);
		return tex;
	}
}
