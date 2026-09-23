package com.hbm.datagen;

import java.util.concurrent.CompletableFuture;

import java.util.List;
import java.util.Map;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.OreDictManager;
import com.hbm.lib.RefStrings;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockTagProvider extends BlockTagsProvider {

	public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper efh) {
		super(output, lookup, RefStrings.MODID, efh);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		ModBlocks.TOOLS.forEach((block, tool) -> tag(switch(tool) {
			case PICKAXE -> BlockTags.MINEABLE_WITH_PICKAXE;
			case AXE -> BlockTags.MINEABLE_WITH_AXE;
			case SHOVEL -> BlockTags.MINEABLE_WITH_SHOVEL;
		}).add(block.get()));

		ModBlocks.BEACON_BASES.forEach(block -> tag(BlockTags.BEACON_BASE_BLOCKS).add(block.get()));

		// ores and storage blocks also get block tags, like the items (c:ores/uranium etc.)
		OreDictManager.registerOres();
		for(Map.Entry<String, List<Object>> entry : OreDictManager.ENTRIES.entrySet()) {
			ResourceLocation loc = OreDictManager.tagLocation(entry.getKey());
			if(!loc.getPath().startsWith("ores/") && !loc.getPath().startsWith("storage_blocks/")) continue;
			TagKey<Block> tag = TagKey.create(Registries.BLOCK, loc);
			for(Object o : entry.getValue()) {
				if(OreDictManager.toItem(o) instanceof BlockItem blockItem) {
					tag(tag).add(blockItem.getBlock());
					tag(TagKey.create(Registries.BLOCK, OreDictManager.parentTag(loc))).addTag(tag);
				}
			}
		}
	}
}
