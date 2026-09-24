package com.hbm.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.hbm.items.ModItems;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Recipe ingredients of NTM's machines (press, assembler, chemical plant...).
 * 1.21 differences: metadata variants are separate items (ItemEnumMulti), other "metadata" like a canister's
 * fluid are data components. A ComparableStack matches the item and the components it was created with
 * (which also covers the original's NBTStack), an OreDictStack matches the tag of the ore dict key.
 */
public class RecipesCommon {

	public static abstract class AStack implements Comparable<AStack> {

		public int stacksize;

		/**
		 * Whether the supplied itemstack is applicable for a recipe (e.g. anvils). Slightly different from {@code isApplicable}.
		 * @param stack the ItemStack to check
		 * @param ignoreSize whether size should be ignored entirely or if the ItemStack needs to be >at least< the same size as this' size
		 */
		public abstract boolean matchesRecipe(ItemStack stack, boolean ignoreSize);

		public abstract AStack copy();
		public abstract AStack copy(int stacksize);

		/** All stacks this ingredient accepts (the original's NEI/JEI helper) */
		public abstract List<ItemStack> extractForNEI();

		public ItemStack extractForCyclingDisplay(int cycle) {
			List<ItemStack> list = extractForNEI();
			cycle *= 50;

			if(list.isEmpty()) return new ItemStack(ModItems.nothing.get());
			return list.get((int) (System.currentTimeMillis() % (cycle * list.size()) / cycle));
		}
	}

	public static class ComparableStack extends AStack {

		public Item item;
		/** Components the matched stack must have, e.g. the fluid of a fluid container (empty for plain items) */
		public DataComponentPatch components = DataComponentPatch.EMPTY;

		public ComparableStack(ItemStack stack) {
			if(stack == null || stack.isEmpty()) {
				this.item = ModItems.nothing.get();
				this.stacksize = 1;
				return;
			}
			this.item = stack.getItem();
			this.stacksize = stack.getCount();
			this.components = stack.getComponentsPatch();
		}

		public ComparableStack(ItemLike item) {
			this(item, 1);
		}

		public ComparableStack(ItemLike item, int stacksize) {
			this.item = item == null ? ModItems.nothing.get() : item.asItem();
			this.stacksize = stacksize;
		}

		public ComparableStack makeSingular() {
			stacksize = 1;
			return this;
		}

		public ItemStack toStack() {
			ItemStack stack = new ItemStack(item, stacksize);
			stack.applyComponents(components);
			return stack;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + BuiltInRegistries.ITEM.getKey(item).hashCode();
			result = prime * result + components.hashCode();
			result = prime * result + stacksize;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(obj == null || getClass() != obj.getClass()) return false;
			ComparableStack other = (ComparableStack) obj;
			return item == other.item && stacksize == other.stacksize && components.equals(other.components);
		}

		@Override
		public int compareTo(AStack stack) {

			//if compared with an ODStack, the CStack will take priority
			if(stack instanceof OreDictStack) return 1;

			if(stack instanceof ComparableStack comp) {
				// stacks with component requirements are more specific (the original's NBTStack took priority)
				if(!components.isEmpty() && comp.components.isEmpty()) return 1;
				if(components.isEmpty() && !comp.components.isEmpty()) return -1;
				return Integer.compare(BuiltInRegistries.ITEM.getId(item), BuiltInRegistries.ITEM.getId(comp.item));
			}

			return 0;
		}

		@Override
		public ComparableStack copy() {
			return copy(stacksize);
		}

		@Override
		public ComparableStack copy(int stacksize) {
			ComparableStack copy = new ComparableStack(item, stacksize);
			copy.components = this.components;
			return copy;
		}

		@Override
		public boolean matchesRecipe(ItemStack stack, boolean ignoreSize) {

			if(stack == null || stack.isEmpty()) return false;
			if(stack.getItem() != this.item) return false;
			if(!ignoreSize && stack.getCount() < this.stacksize) return false;

			// all components of this stack need to be present with the same value, extra ones are ignored
			for(var entry : components.entrySet()) {
				if(entry.getValue().isPresent()) {
					if(!Objects.equals(stack.get(entry.getKey()), entry.getValue().get())) return false;
				} else if(stack.has(entry.getKey())) {
					return false;
				}
			}

			return true;
		}

		@Override
		public List<ItemStack> extractForNEI() {
			return List.of(this.toStack());
		}

		@Override
		public String toString() {
			return this.stacksize + "x" + BuiltInRegistries.ITEM.getKey(item) + (components.isEmpty() ? "" : components.toString());
		}
	}

	public static class OreDictStack extends AStack {

		public String name;

		public OreDictStack(String name) {
			this.name = name;
			this.stacksize = 1;
		}

		public OreDictStack(String name, int stacksize) {
			this(name);
			this.stacksize = stacksize;
		}

		public TagKey<Item> tag() {
			return OreDictManager.tag(name);
		}

		public List<ItemStack> toStacks() {
			List<ItemStack> list = new ArrayList<>();
			for(var holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag())) list.add(new ItemStack(holder));
			return list;
		}

		@Override
		public int compareTo(AStack stack) {

			if(stack instanceof OreDictStack comp) {
				return name.compareTo(comp.name);
			}

			//if compared with a CStack, the ODStack will yield
			if(stack instanceof ComparableStack) return -1;

			return 0;
		}

		@Override
		public OreDictStack copy() {
			return new OreDictStack(name, stacksize);
		}

		@Override
		public OreDictStack copy(int stacksize) {
			return new OreDictStack(name, stacksize);
		}

		@Override
		public boolean matchesRecipe(ItemStack stack, boolean ignoreSize) {

			if(stack == null || stack.isEmpty())
				return false;

			if(!ignoreSize && stack.getCount() < this.stacksize)
				return false;

			return stack.is(tag());
		}

		@Override
		public List<ItemStack> extractForNEI() {
			List<ItemStack> ores = new ArrayList<>();
			for(ItemStack stack : toStacks()) ores.add(stack.copyWithCount(this.stacksize));
			return ores;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + this.stacksize;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(obj == null || getClass() != obj.getClass()) return false;
			OreDictStack other = (OreDictStack) obj;
			return Objects.equals(name, other.name) && stacksize == other.stacksize;
		}

		@Override
		public String toString() {
			return this.stacksize + "x" + this.name;
		}
	}
}
