package com.hbm.items;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * One variant of a former metadata item. 1.7.10's ItemEnumMulti was a single item with an enum per damage value,
 * with the flattening every enum value is its own item ("coke" + COAL -> hbm:coke_coal).
 * The translation key stays the original one ("item.hbm.coke.coal") so the existing lang entries apply.
 */
public class ItemEnumMulti extends Item {

	private final String descriptionId;

	public ItemEnumMulti(Properties properties, String descriptionId) {
		super(properties);
		this.descriptionId = descriptionId;
	}

	@Override
	public String getDescriptionId() {
		return descriptionId;
	}

	/** All variants of one original multi item, e.g. ModItems.coke.get(EnumCokeType.COAL) */
	public static class Variants<E extends Enum<E>> {

		public final String name;
		public final Class<E> theEnum;
		private final Map<E, DeferredItem<? extends Item>> items;

		public Variants(String name, Class<E> theEnum) {
			this.name = name;
			this.theEnum = theEnum;
			this.items = new EnumMap<>(theEnum);
		}

		void put(E value, DeferredItem<? extends Item> item) {
			items.put(value, item);
		}

		public DeferredItem<? extends Item> get(E value) {
			return items.get(value);
		}

		/** Equivalent of the original's stackFromEnum */
		public ItemStack stack(E value, int count) {
			return new ItemStack(get(value).get(), count);
		}

		public ItemStack stack(E value) {
			return stack(value, 1);
		}

		/** All variants, in enum order (DictFrame.fromAll in the original) */
		public Object[] all() {
			return items.values().toArray();
		}

		public Collection<DeferredItem<? extends Item>> values() {
			return items.values();
		}

		/** Registry name of a variant */
		public static String variantName(String name, Enum<?> value) {
			return name + "_" + value.name().toLowerCase(Locale.US);
		}
	}
}
