package com.hbm.datagen;

import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;

public class ModItemModelProvider extends ItemModelProvider {

	public ModItemModelProvider(PackOutput output, ExistingFileHelper efh) {
		super(output, RefStrings.MODID, efh);
	}

	@Override
	protected void registerModels() {
		flat(ModItems.ingot_uranium);
		flat(ModItems.ingot_titanium);
		flat(ModItems.ingot_steel);
		flat(ModItems.nugget_uranium);
		flat(ModItems.dosimeter);
		flat(ModItems.geiger_counter);
	}

	/** Flat item model using the original's texture location, textures/items/[name].png */
	private void flat(DeferredItem<? extends Item> item) {
		String name = item.getId().getPath();
		withExistingParent(name, mcLoc("item/generated")).texture("layer0", texture("items/" + name));
	}

	private ResourceLocation texture(String path) {
		ResourceLocation tex = modLoc(path);
		ModSpriteSourceProvider.USED_TEXTURES.add(tex);
		return tex;
	}
}
