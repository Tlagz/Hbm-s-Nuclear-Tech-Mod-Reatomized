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
		// the recolored autogen textures are written by MaterialTextureProvider in the same run
		for(var set : ModItems.AUTOGEN) for(var mat : set.materials()) {
			if(set.isGenerated(mat)) existingFileHelper.trackGenerated(modLoc(set.texture(mat)), TEXTURE);
		}
		ModItems.FLAT_MODELS.forEach(this::flat);
		ModItems.LAYERED_MODELS.forEach(this::layered);
		ModItems.ITEM_RENDERED.forEach(this::rendered);
		blueprints();
	}

	/** Items drawn by the NTM item renderer, display transforms like the tile rendered blocks' items */
	private void rendered(DeferredItem<?> item) {
		getBuilder(item.getId().getPath()).parent(new net.neoforged.neoforge.client.model.generators.ModelFile.UncheckedModelFile("builtin/entity")).transforms()
				.transform(net.minecraft.world.item.ItemDisplayContext.GROUND).translation(0, 3, 0).scale(0.25F).end()
				.transform(net.minecraft.world.item.ItemDisplayContext.FIXED).scale(0.5F).end()
				.transform(net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(75, 45, 0).translation(0, 2.5F, 0).scale(0.375F).end()
				.transform(net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_LEFT_HAND).rotation(75, 45, 0).translation(0, 2.5F, 0).scale(0.375F).end()
				.transform(net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, 45, 0).scale(0.4F).end()
				.transform(net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 225, 0).scale(0.4F).end();
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
