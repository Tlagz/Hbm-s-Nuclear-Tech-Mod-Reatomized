package com.hbm.inventory.recipes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.recipes.gen.GenShredderRecipes;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

/**
 * Shredder recipes. The fixed ones are translated from the original by tools/gen_generic.py (GenShredderRecipes).
 * The original generated the rest from the ore dictionary after loading (ingot/plate/gem/crystal -> 1 dust,
 * ore -> 2 dust, block -> 9 dust...), here that runs over the item tags whenever tags are (re)loaded.
 * Anything without a recipe shreds into scrap.
 *
 * TODO JSON config (SerializableRecipe), JEI
 */
@EventBusSubscriber(modid = RefStrings.MODID)
public class ShredderRecipes {

	/** Fixed recipes, they take precedence over the tag generated ones */
	public static Map<ComparableStack, ItemStack> shredderRecipes = new HashMap<>();
	/** Generated from the item tags, rebuilt when tags reload */
	public static Map<ComparableStack, ItemStack> tagRecipes = new HashMap<>();

	public static void registerDefaults() {
		shredderRecipes.clear();
		GenShredderRecipes.register();
		MainRegistry.logger.info("Shredder recipes: " + shredderRecipes.size() + " fixed");
	}

	public static void setRecipe(ItemLike in, ItemStack out) {
		setRecipe(new ComparableStack(in), out);
	}

	public static void setRecipe(ItemStack in, ItemStack out) {
		setRecipe(new ComparableStack(in).makeSingular(), out);
	}

	public static void setRecipe(ComparableStack in, ItemStack out) {
		if(!shredderRecipes.containsKey(in)) {
			shredderRecipes.put(in, out);
		}
	}

	public static ItemStack getShredderResult(ItemStack stack) {

		if(stack == null || stack.isEmpty())
			return new ItemStack(ModItems.scrap.get());

		ComparableStack comp = new ComparableStack(stack).makeSingular();
		ItemStack sta = shredderRecipes.get(comp);
		if(sta == null) sta = tagRecipes.get(comp);

		// the original's wildcard fallback: the recipe of the plain item (ignoring damage and other components)
		if(sta == null) {
			ComparableStack plain = new ComparableStack(stack.getItem());
			sta = shredderRecipes.get(plain);
			if(sta == null) sta = tagRecipes.get(plain);
		}

		return sta == null ? new ItemStack(ModItems.scrap.get()) : sta;
	}

	/// TAG GENERATED RECIPES (the original's registerPost) ///

	@SubscribeEvent
	public static void onTagsUpdated(TagsUpdatedEvent event) {
		registerTagRecipes();
	}

	private static void put(Item in, ItemStack out) {
		ComparableStack comp = new ComparableStack(in);
		if(!shredderRecipes.containsKey(comp) && !tagRecipes.containsKey(comp)) tagRecipes.put(comp, out);
	}

	public static void registerTagRecipes() {
		tagRecipes.clear();

		// the original's fixed recipes that went over ore dict entries
		forTag(ItemTags.LOGS, item -> put(item, new ItemStack(ModItems.powder_sawdust.get(), 4)));
		forTag(ItemTags.PLANKS, item -> put(item, new ItemStack(ModItems.powder_sawdust.get(), 1)));
		forTag(ItemTags.SAPLINGS, item -> put(item, new ItemStack(Items.STICK, 1)));
		forTag(tag("c", "dusts/lapis"), item -> put(item, new ItemStack(ModItems.powder_cobalt_tiny.get(), 1)));

		BuiltInRegistries.ITEM.getTags().forEach(pair -> {
			ResourceLocation loc = pair.getFirst().location();
			String path = loc.getPath();
			int slash = path.indexOf('/');
			if(slash < 0 || path.indexOf('/', slash + 1) >= 0) return;
			String family = loc.getNamespace() + ":" + path.substring(0, slash);
			String mat = path.substring(slash + 1);

			// group tags ("Any" in the original's names) are skipped
			if(mat.startsWith("any")) return;

			HolderSet.Named<Item> matches = pair.getSecond();

			switch(family) {
			//1 ingot unit, metal (ingots, plates) or crystalline (gems, crystals)
			case "c:ingots", "c:plates", "c:gems", "hbm:crystals": generateRecipes(mat, matches, 1); break;
			//2 ingot units, any
			case "c:ores": generateRecipes(mat, matches, 2); break;
			case "c:storage_blocks": {
				Optional<Item> dust = getDustByName(mat);
				if(dust.isPresent()) {
					int count = getIngotOrGemByName(mat) ? 9 : 4;
					for(Holder<Item> holder : matches) put(holder.value(), new ItemStack(dust.get(), count));
				}
				break;
			}
			case "hbm:tiny_dusts": for(Holder<Item> holder : matches) put(holder.value(), new ItemStack(ModItems.dust_tiny.get())); break;
			case "c:dusts": for(Holder<Item> holder : matches) put(holder.value(), new ItemStack(ModItems.dust.get())); break;
			default: break;
			}
		});

		MainRegistry.logger.info("Shredder recipes: " + tagRecipes.size() + " generated from item tags");
	}

	private static void generateRecipes(String mat, HolderSet.Named<Item> matches, int outCount) {
		Optional<Item> dust = getDustByName(mat);
		if(dust.isEmpty()) return;
		for(Holder<Item> holder : matches) put(holder.value(), new ItemStack(dust.get(), outCount));
	}

	private static TagKey<Item> tag(String namespace, String path) {
		return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, path));
	}

	private static void forTag(TagKey<Item> tag, java.util.function.Consumer<Item> action) {
		BuiltInRegistries.ITEM.getTag(tag).ifPresent(set -> set.forEach(holder -> action.accept(holder.value())));
	}

	/** The first item of c:dusts/[mat], like the original's getDustByName (which returned scrap for none) */
	public static Optional<Item> getDustByName(String mat) {
		return BuiltInRegistries.ITEM.getTag(tag("c", "dusts/" + mat)).flatMap(set -> set.size() > 0 ? Optional.of(set.get(0).value()) : Optional.empty());
	}

	public static boolean getIngotOrGemByName(String mat) {
		return BuiltInRegistries.ITEM.getTag(tag("c", "ingots/" + mat)).map(set -> set.size() > 0).orElse(false)
				|| BuiltInRegistries.ITEM.getTag(tag("c", "gems/" + mat)).map(set -> set.size() > 0).orElse(false);
	}
}
