package com.hbm.blocks;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;

/**
 * One variant of a former metadata block. 1.7.10's BlockEnumMulti was a single block with an enum per metadata value,
 * with the flattening every enum value is its own block ("lightstone" + TILE -> hbm:lightstone_tile).
 * The translation key stays the original one ("block.hbm.lightstone.tile") so the existing lang entries apply.
 */
public class BlockEnumMulti extends Block {

	private final String descriptionId;

	public BlockEnumMulti(Properties properties, String descriptionId) {
		super(properties);
		this.descriptionId = descriptionId;
	}

	@Override
	public String getDescriptionId() {
		return descriptionId;
	}

	/** Creates the block of one variant, for BlockEnumMulti subclasses of the original */
	@FunctionalInterface
	public interface VariantFactory<E> {
		Block create(Properties properties, String descriptionId, E value);
	}

	/** All variants of one original multi block, e.g. ModBlocks.lightstone.get(LightstoneType.TILE) */
	public static class Variants<E extends Enum<E>> {

		public final String name;
		public final Class<E> theEnum;
		private final Map<E, DeferredBlock<? extends Block>> blocks;

		public Variants(String name, Class<E> theEnum) {
			this.name = name;
			this.theEnum = theEnum;
			this.blocks = new EnumMap<>(theEnum);
		}

		public void put(E value, DeferredBlock<? extends Block> block) {
			blocks.put(value, block);
		}

		public DeferredBlock<? extends Block> get(E value) {
			return blocks.get(value);
		}

		public ItemStack stack(E value, int count) {
			return new ItemStack(get(value).get(), count);
		}

		public ItemStack stack(E value) {
			return stack(value, 1);
		}

		public Collection<DeferredBlock<? extends Block>> values() {
			return blocks.values();
		}

		/** All variants, for registering them under one dictionary key */
		public Object[] all() {
			return blocks.values().toArray();
		}

		/** Any variant, what the original matched when a recipe used the plain block (metadata wildcard) */
		public Ingredient any() {
			return Ingredient.of(blocks.values().stream().map(block -> new ItemStack(block.get())));
		}

		public static String variantName(String name, Enum<?> value) {
			return name + "_" + value.name().toLowerCase(Locale.US);
		}
	}
}
