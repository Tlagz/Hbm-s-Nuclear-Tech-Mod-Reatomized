package com.hbm.items.special;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.NTMMaterial;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Items generated for every material that lists the shape in its autogen (wires, bolts, cast plates, gun parts...).
 * The original had one item with the material ID as metadata, here every material gets its own item
 * ("wire_fine_copper"), grouped by {@link AutogenItems}. The name is the shape's translation with the material's name.
 *
 * Textures: a hand-made override if the original had one, otherwise the shape's grey texture recolored to the
 * material's solid colors at datagen time (MaterialTextureProvider, the original remapped it at runtime), or the
 * grey texture tinted with the molten color if the material has only one solid color.
 */
public class ItemAutogen extends Item {

	public final MaterialShapes shape;
	public final NTMMaterial mat;
	private final String descriptionId;

	public ItemAutogen(Properties properties, MaterialShapes shape, NTMMaterial mat, String descriptionId) {
		super(properties);
		this.shape = shape;
		this.mat = mat;
		this.descriptionId = descriptionId;
	}

	@Override
	public String getDescriptionId() {
		return descriptionId;
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable(descriptionId, Component.translatable(mat.getUnlocalizedName()));
	}

	/** One shape's items, the original's single ItemAutogen */
	public static class AutogenItems {

		public final String name;
		public final MaterialShapes shape;
		/** The grey base texture, e.g. items/wire_fine */
		public final String baseTexture;
		private final Map<NTMMaterial, String> textureOverrides;
		private final Map<NTMMaterial, DeferredItem<ItemAutogen>> items = new LinkedHashMap<>();

		public AutogenItems(String name, MaterialShapes shape, String baseTexture, Map<NTMMaterial, String> textureOverrides) {
			this.name = name;
			this.shape = shape;
			this.baseTexture = baseTexture;
			this.textureOverrides = textureOverrides;
		}

		public void put(NTMMaterial mat, DeferredItem<ItemAutogen> item) {
			items.put(mat, item);
		}

		public DeferredItem<ItemAutogen> get(NTMMaterial mat) {
			return items.get(mat);
		}

		/** The original's new ItemStack(item, count, mat.id), empty if the material doesn't have this shape */
		public ItemStack stack(NTMMaterial mat, int count) {
			DeferredItem<ItemAutogen> item = items.get(mat);
			return item == null ? ItemStack.EMPTY : new ItemStack(item.get(), count);
		}

		public ItemStack stack(NTMMaterial mat) {
			return stack(mat, 1);
		}

		public Collection<NTMMaterial> materials() {
			return items.keySet();
		}

		public Collection<DeferredItem<ItemAutogen>> values() {
			return items.values();
		}

		/** Registry name of a material's item */
		public static String itemName(String name, NTMMaterial mat) {
			return name + "_" + mat.registryName();
		}

		/** Recolored at datagen time: no override and the material has a light and a dark color */
		public boolean isGenerated(NTMMaterial mat) {
			return !textureOverrides.containsKey(mat) && mat.solidColorLight != mat.solidColorDark;
		}

		/** Grey texture tinted with the molten color at runtime */
		public boolean isTinted(NTMMaterial mat) {
			return !textureOverrides.containsKey(mat) && mat.solidColorLight == mat.solidColorDark;
		}

		/** The item's texture path (items/...) */
		public String texture(NTMMaterial mat) {
			String override = textureOverrides.get(mat);
			if(override != null) return "items/" + override;
			if(isGenerated(mat)) return "items/autogen/" + itemName(name, mat);
			return baseTexture;
		}
	}
}
