package com.hbm.hazard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.hbm.hazard.modifier.HazardModifier;
import com.hbm.hazard.transformer.HazardTransformerBase;
import com.hbm.hazard.type.HazardTypeBase;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Assigns hazards (radiation, heat, asbestos...) to items and applies them to whoever carries them.
 * The original's ore dictionary map is now a tag map. Metadata-specific stacks are gone with the flattening,
 * every former meta variant is its own item.
 */
public class HazardSystem {

	/*
	 * Map for tags, always evaluated first. Avoid registering HazardData with 'doesOverride', as internal order is based on the item's tags.
	 */
	public static final Map<TagKey<Item>, HazardData> tagMap = new HashMap<>();
	/*
	 * Map for items.
	 */
	public static final Map<Item, HazardData> itemMap = new HashMap<>();
	/*
	 * For items that should, for whichever reason, be completely exempt from the hazard system.
	 */
	public static final Set<Item> itemBlacklist = new HashSet<>();
	public static final Set<TagKey<Item>> tagBlacklist = new HashSet<>();
	/*
	 * List of hazard transformers, called in order before and after unrolling all the HazardEntries.
	 */
	public static final List<HazardTransformerBase> trafos = new ArrayList<>();

	/** Registers hazard data for a tag, an item/block or a supplier of those (deferred registry objects) */
	@SuppressWarnings("unchecked")
	public static void register(Object o, HazardData data) {

		if(o instanceof TagKey<?> tag)
			tagMap.put((TagKey<Item>) tag, data);
		else if(o instanceof ItemLike item)
			itemMap.put(item.asItem(), data);
		else if(o instanceof Supplier<?> supplier)
			register(supplier.get(), data);
		else if(o instanceof com.hbm.items.ItemEnumMulti.Variants<?> variants) // the original registered all metas of the item
			for(Object variant : variants.values()) register(variant, data);
		else
			throw new IllegalArgumentException("Can't register hazards for " + o);
	}

	/** Prevents the item or tag from returning any HazardData */
	@SuppressWarnings("unchecked")
	public static void blacklist(Object o) {

		if(o instanceof TagKey<?> tag)
			tagBlacklist.add((TagKey<Item>) tag);
		else if(o instanceof ItemLike item)
			itemBlacklist.add(item.asItem());
		else if(o instanceof Supplier<?> supplier)
			blacklist(supplier.get());
	}

	public static boolean isItemBlacklisted(ItemStack stack) {

		if(itemBlacklist.contains(stack.getItem()))
			return true;

		for(TagKey<Item> tag : tagBlacklist) {
			if(stack.is(tag)) return true;
		}

		return false;
	}

	/**
	 * Will return a full list of applicable HazardEntries for this stack.
	 * <br><br>ORDER:
	 * <ol>
	 * <li>tags
	 * <li>item
	 * </ol>
	 *
	 * "Applicable" means that entries that are overridden or excluded via mutex are not in this list.
	 * Entries that are marked as "overriding" will delete all fetched entries that came before it.
	 * Entries that use mutex will prevent subsequent entries from being considered, shall they collide.
	 */
	public static List<HazardEntry> getHazardsFromStack(ItemStack stack) {

		if(stack.isEmpty() || isItemBlacklisted(stack)) {
			return new ArrayList<>();
		}

		List<HazardData> chronological = new ArrayList<>();

		/// TAGS ///
		if(!tagMap.isEmpty()) {
			stack.getTags().forEach(tag -> {
				HazardData data = tagMap.get(tag);
				if(data != null) chronological.add(data);
			});
		}

		/// ITEM ///
		HazardData itemData = itemMap.get(stack.getItem());
		if(itemData != null)
			chronological.add(itemData);

		List<HazardEntry> entries = new ArrayList<>();

		for(HazardTransformerBase trafo : trafos) {
			trafo.transformPre(stack, entries);
		}

		int mutex = 0;

		for(HazardData data : chronological) {
			//if the current data is marked as an override, purge all previous data
			if(data.doesOverride)
				entries.clear();

			if((data.getMutex() & mutex) == 0) {
				entries.addAll(data.entries);
				mutex = mutex | data.getMutex();
			}
		}

		for(HazardTransformerBase trafo : trafos) {
			trafo.transformPost(stack, entries);
		}

		return entries;
	}

	public static float getHazardLevelFromStack(ItemStack stack, HazardTypeBase hazard) {
		List<HazardEntry> entries = getHazardsFromStack(stack);

		for(HazardEntry entry : entries) {
			if(entry.type == hazard) {
				return HazardModifier.evalAllModifiers(stack, null, entry.baseLevel, entry.mods);
			}
		}

		return 0F;
	}

	/** Will grab and iterate through all assigned hazards of the given stack and apply their effects to the holder. */
	public static void applyHazards(ItemStack stack, LivingEntity entity) {
		List<HazardEntry> hazards = getHazardsFromStack(stack);

		for(HazardEntry hazard : hazards) {
			hazard.applyHazard(stack, entity);
		}
	}

	/** Will apply the effects of all carried items, including the armor inventory. */
	public static void updatePlayerInventory(Player player) {

		for(ItemStack stack : player.getInventory().items) {
			if(!stack.isEmpty()) applyHazards(stack, player);
		}

		for(ItemStack stack : player.getInventory().armor) {
			if(!stack.isEmpty()) applyHazards(stack, player);
		}

		for(ItemStack stack : player.getInventory().offhand) {
			if(!stack.isEmpty()) applyHazards(stack, player);
		}
	}

	public static void updateLivingInventory(LivingEntity entity) {

		for(EquipmentSlot slot : EquipmentSlot.values()) {
			ItemStack stack = entity.getItemBySlot(slot);

			if(!stack.isEmpty()) {
				applyHazards(stack, entity);
			}
		}
	}

	public static void updateDroppedItem(ItemEntity entity) {

		ItemStack stack = entity.getItem();

		if(entity.isRemoved() || stack.isEmpty()) return;

		List<HazardEntry> hazards = getHazardsFromStack(stack);
		for(HazardEntry entry : hazards) {
			entry.type.updateEntity(entity, HazardModifier.evalAllModifiers(stack, null, entry.baseLevel, entry.mods));
		}
	}

	/** Client only, adds the hazard lines to an item's tooltip */
	public static void addFullTooltip(ItemStack stack, Player player, List<Component> list) {

		List<HazardEntry> hazards = getHazardsFromStack(stack);

		for(HazardEntry hazard : hazards) {
			hazard.type.addHazardInformation(player, list, hazard.baseLevel, stack, hazard.mods);
		}
	}
}
