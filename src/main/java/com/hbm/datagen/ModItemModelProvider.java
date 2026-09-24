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
		ModItems.FLAT_MODELS.forEach(this::flat);
		ModItems.LAYERED_MODELS.forEach(this::layered);
		blueprints();
	}

	/** Blueprint texture by pool type (ItemBlueprints.poolType): regular, discover, secret, 528 */
	private void blueprints() {
		String[] suffixes = { "_discover", "_secret", "_528" };
		var base = withExistingParent("blueprints", mcLoc("item/generated")).texture("layer0", texture("items/blueprints"));
		for(int i = 0; i < suffixes.length; i++) {
			var variant = withExistingParent("blueprints" + suffixes[i], mcLoc("item/generated")).texture("layer0", texture("items/blueprints" + suffixes[i]));
			base.override().predicate(modLoc("pool"), i + 1).model(variant).end();
		}
	}

	/** Flat item model using the original's texture location, textures/items/[name].png */
	private void flat(DeferredItem<?> item, String texture) {
		String name = item.getId().getPath();
		withExistingParent(name, mcLoc("item/generated")).texture("layer0", texture(texture));
	}

	/** Flat model with one layer per texture, the original's render passes */
	private void layered(DeferredItem<?> item, String[] textures) {
		var builder = withExistingParent(item.getId().getPath(), mcLoc("item/generated"));
		for(int i = 0; i < textures.length; i++) builder.texture("layer" + i, texture(textures[i]));
	}

	private ResourceLocation texture(String path) {
		ResourceLocation tex = modLoc(path);
		ModSpriteSourceProvider.USED_TEXTURES.add(tex);
		return tex;
	}
}
