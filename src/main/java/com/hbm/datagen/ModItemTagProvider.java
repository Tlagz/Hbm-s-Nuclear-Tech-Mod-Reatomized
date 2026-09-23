package com.hbm.datagen;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.hbm.inventory.OreDictManager;
import com.hbm.lib.RefStrings;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Item tags from the OreDictManager (the former ore dictionary). Shape tags are also added to their
 * parent (c:ingots/uranium -> c:ingots), groups include their members' tags.
 */
public class ModItemTagProvider extends ItemTagsProvider {

	public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, CompletableFuture<TagLookup<Block>> blockTags, ExistingFileHelper efh) {
		super(output, lookup, blockTags, RefStrings.MODID, efh);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		OreDictManager.registerOres();

		for(Map.Entry<String, List<Object>> entry : OreDictManager.ENTRIES.entrySet()) {
			TagKey<Item> tag = OreDictManager.tag(entry.getKey());
			for(Object o : entry.getValue()) tag(tag).add(OreDictManager.toItem(o));
			addToParent(tag);
		}

		for(Map.Entry<String, Set<String>> group : OreDictManager.GROUPS.entrySet()) {
			TagKey<Item> tag = OreDictManager.tag(group.getKey());
			for(String member : group.getValue()) tag(tag).addOptionalTag(OreDictManager.tagLocation(member));
			addToParent(tag);
		}
	}

	private void addToParent(TagKey<Item> tag) {
		ResourceLocation parent = OreDictManager.parentTag(tag.location());
		if(parent != null) tag(TagKey.create(Registries.ITEM, parent)).addTag(tag);
	}
}
