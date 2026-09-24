package com.hbm.inventory.recipes;

import static com.hbm.inventory.OreDictManager.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.items.ItemEnums.EnumBriquetteType;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemStamp;
import com.hbm.items.machine.ItemStamp.StampType;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Burner press recipes: ingredient + stamp type -> output.
 * Items that aren't ported yet are looked up by name and skipped.
 *
 * TODO wire recipes (material system: wire_fine per NTMMaterial), JSON config (SerializableRecipe), JEI
 */
public class PressRecipes {

	public static Map<Entry<AStack, StampType>, ItemStack> recipes = new LinkedHashMap<>();
	public static int skipped = 0;

	public static ItemStack getOutput(ItemStack ingredient, ItemStack stamp) {

		if(ingredient == null || ingredient.isEmpty() || stamp == null || stamp.isEmpty())
			return null;

		if(!(stamp.getItem() instanceof ItemStamp item))
			return null;

		StampType type = item.getStampType(stamp);

		for(Entry<Entry<AStack, StampType>, ItemStack> recipe : recipes.entrySet()) {
			if(recipe.getKey().getValue() == type && recipe.getKey().getKey().matchesRecipe(ingredient, true))
				return recipe.getValue();
		}

		return null;
	}

	public static void registerDefaults() {
		recipes.clear();
		skipped = 0;

		makeRecipe(StampType.FLAT, new OreDictStack(NETHERQUARTZ.dust()),		new ItemStack(Items.QUARTZ));
		makeRecipe(StampType.FLAT, new OreDictStack(LAPIS.dust()),				new ItemStack(Items.LAPIS_LAZULI));
		makeRecipe(StampType.FLAT, new OreDictStack(DIAMOND.dust()),			new ItemStack(Items.DIAMOND));
		makeRecipe(StampType.FLAT, new OreDictStack(EMERALD.dust()),			new ItemStack(Items.EMERALD));
		makeRecipe(StampType.FLAT, stack("biomass"),							out("biomass_compressed"));
		makeRecipe(StampType.FLAT, new OreDictStack(ANY_COKE.gem()),			out("ingot_graphite"));
		makeRecipe(StampType.FLAT, stack("meteorite_sword_reforged"),			out("meteorite_sword_hardened"));
		makeRecipe(StampType.FLAT, new ComparableStack(Items.JUNGLE_LOG),		out("ball_resin"));

		makeRecipe(StampType.FLAT, new OreDictStack(COAL.dust()),				ModItems.briquette.stack(EnumBriquetteType.COAL));
		makeRecipe(StampType.FLAT, new OreDictStack(LIGNITE.dust()),			ModItems.briquette.stack(EnumBriquetteType.LIGNITE));
		makeRecipe(StampType.FLAT, stack("powder_sawdust"),						ModItems.briquette.stack(EnumBriquetteType.WOOD));

		makeRecipe(StampType.PLATE, new OreDictStack(IRON.ingot()),				out("plate_iron"));
		makeRecipe(StampType.PLATE, new OreDictStack(GOLD.ingot()),				out("plate_gold"));
		makeRecipe(StampType.PLATE, new OreDictStack(TI.ingot()),				out("plate_titanium"));
		makeRecipe(StampType.PLATE, new OreDictStack(AL.ingot()),				out("plate_aluminium"));
		makeRecipe(StampType.PLATE, new OreDictStack(STEEL.ingot()),			out("plate_steel"));
		makeRecipe(StampType.PLATE, new OreDictStack(PB.ingot()),				out("plate_lead"));
		makeRecipe(StampType.PLATE, new OreDictStack(CU.ingot()),				out("plate_copper"));
		makeRecipe(StampType.PLATE, new OreDictStack(SA326.ingot()),			out("plate_schrabidium"));
		makeRecipe(StampType.PLATE, new OreDictStack(CMB.ingot()),				out("plate_combine_steel"));
		makeRecipe(StampType.PLATE, new OreDictStack(GUNMETAL.ingot()),			out("plate_gunmetal"));
		makeRecipe(StampType.PLATE, new OreDictStack(WEAPONSTEEL.ingot()),		out("plate_weaponsteel"));
		makeRecipe(StampType.PLATE, new OreDictStack(BIGMT.ingot()),			out("plate_saturnite"));
		makeRecipe(StampType.PLATE, new OreDictStack(DURA.ingot()),				out("plate_dura_steel"));

		// TODO casings (EnumCasingType), silicon circuits (circuit), printed pages (page_of_) once those items are ported

		MainRegistry.logger.info("Press recipes: " + recipes.size() + " registered, " + skipped + " skipped (items not ported)");
	}

	/** An item that may not be ported yet, null if missing */
	private static Item item(String name) {
		return BuiltInRegistries.ITEM.getOptional(RefStrings.loc(name)).orElse(null);
	}

	private static ItemStack out(String name) {
		Item item = item(name);
		return item == null ? null : new ItemStack(item);
	}

	private static AStack stack(String name) {
		Item item = item(name);
		return item == null ? null : new ComparableStack(item);
	}

	public static void makeRecipe(StampType type, AStack in, ItemStack out) {
		if(in == null || out == null || out.isEmpty()) {
			skipped++;
			return;
		}
		recipes.put(Map.entry(in, type), out);
	}
}
