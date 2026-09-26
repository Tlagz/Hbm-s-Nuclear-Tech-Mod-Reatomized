package com.hbm.inventory.recipes;

import static com.hbm.inventory.OreDictManager.*;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ItemEnums.EnumAshType;
import com.hbm.items.ItemEnums.EnumBriquetteType;
import com.hbm.items.ItemEnums.EnumChunkType;
import com.hbm.items.ItemEnums.EnumCokeType;
import com.hbm.items.ItemEnums.EnumTarType;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.util.Tuple.Pair;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

/**
 * Combination furnace recipes: one item into an optional item and an optional fluid. Exact items first, then tags.
 *
 * TODO the bedrock ore roasting recipes, JSON config, JEI
 */
public class CombinationRecipes {

	public static LinkedHashMap<ComparableStack, Pair<ItemStack, FluidStack>> stacks = new LinkedHashMap<>();
	public static LinkedHashMap<TagKey<Item>, Pair<ItemStack, FluidStack>> tags = new LinkedHashMap<>();

	private static ItemStack coke(EnumCokeType type) { return new ItemStack(ModItems.coke.get(type).get()); }
	private static Item briquette(EnumBriquetteType type) { return ModItems.briquette.get(type).get(); }
	private static Item tar(EnumTarType type) { return ModItems.oil_tar.get(type).get(); }

	private static void put(String dict, ItemStack out, FluidStack fluid) { tags.put(OreDictManager.tag(dict), new Pair<>(out, fluid)); }
	private static void put(TagKey<Item> tag, ItemStack out, FluidStack fluid) { tags.put(tag, new Pair<>(out, fluid)); }
	private static void put(ItemLike item, ItemStack out, FluidStack fluid) { stacks.put(new ComparableStack(item), new Pair<>(out, fluid)); }

	public static void registerDefaults() {
		stacks.clear();
		tags.clear();

		put(COAL.gem(),									coke(EnumCokeType.COAL), new FluidStack(Fluids.COALCREOSOTE, 100));
		put(COAL.dust(),								coke(EnumCokeType.COAL), new FluidStack(Fluids.COALCREOSOTE, 100));
		put(briquette(EnumBriquetteType.COAL),			coke(EnumCokeType.COAL), new FluidStack(Fluids.COALCREOSOTE, 150));

		put(LIGNITE.gem(),								coke(EnumCokeType.LIGNITE), new FluidStack(Fluids.COALCREOSOTE, 50));
		put(LIGNITE.dust(),								coke(EnumCokeType.LIGNITE), new FluidStack(Fluids.COALCREOSOTE, 50));
		put(briquette(EnumBriquetteType.LIGNITE),		coke(EnumCokeType.LIGNITE), new FluidStack(Fluids.COALCREOSOTE, 100));

		put(CHLOROCALCITE.dust(),						new ItemStack(ModItems.powder_calcium.get()), new FluidStack(Fluids.CHLORINE, 250));
		put(MOLYSITE.dust(),							new ItemStack(Items.IRON_INGOT), new FluidStack(Fluids.CHLORINE, 250));
		put(CINNABAR.crystal(),							new ItemStack(ModItems.sulfur.get()), new FluidStack(Fluids.MERCURY, 100));
		put(Items.GLOWSTONE_DUST,						new ItemStack(ModItems.sulfur.get()), new FluidStack(Fluids.CHLORINE, 100));
		put(SODALITE.gem(),								new ItemStack(ModItems.powder_sodium.get()), new FluidStack(Fluids.CHLORINE, 100));
		put(ModItems.chunk_ore.get(EnumChunkType.CRYOLITE).get(), new ItemStack(ModItems.powder_aluminium.get(), 1), new FluidStack(Fluids.LYE, 150));
		put(NA.dust(),									ItemStack.EMPTY, new FluidStack(Fluids.SODIUM, 100));
		put(LIMESTONE.dust(),							new ItemStack(ModItems.powder_calcium.get()), new FluidStack(Fluids.CARBONDIOXIDE, 50));

		put(ItemTags.LOGS,								new ItemStack(Items.CHARCOAL), new FluidStack(Fluids.WOODOIL, 250));
		put(ItemTags.SAPLINGS,							new ItemStack(ModItems.powder_ash.get(EnumAshType.WOOD).get()), new FluidStack(Fluids.WOODOIL, 50));
		put(briquette(EnumBriquetteType.WOOD),			new ItemStack(Items.CHARCOAL), new FluidStack(Fluids.WOODOIL, 500));

		put(tar(EnumTarType.CRUDE),						coke(EnumCokeType.PETROLEUM), null);
		put(tar(EnumTarType.CRACK),						coke(EnumCokeType.PETROLEUM), null);
		put(tar(EnumTarType.COAL),						coke(EnumCokeType.COAL), null);
		put(tar(EnumTarType.WOOD),						coke(EnumCokeType.COAL), null);

		put(Items.SUGAR_CANE,							new ItemStack(Items.SUGAR, 2), new FluidStack(Fluids.ETHANOL, 50));
		put(Blocks.CLAY,								new ItemStack(Blocks.BRICKS, 1), null);

		MainRegistry.logger.info("Combination furnace recipes: " + (stacks.size() + tags.size()));
	}

	/** The item output (a copy, empty if there is none) and the fluid output (or null), null if there's no recipe */
	public static Pair<ItemStack, FluidStack> getOutput(ItemStack stack) {

		if(stack == null || stack.isEmpty()) return null;

		Pair<ItemStack, FluidStack> out = stacks.get(new ComparableStack(stack).makeSingular());

		if(out == null) {
			for(Entry<TagKey<Item>, Pair<ItemStack, FluidStack>> entry : tags.entrySet()) {
				if(stack.is(entry.getKey())) {
					out = entry.getValue();
					break;
				}
			}
		}

		if(out == null) return null;
		return new Pair<>(out.getKey() == null ? ItemStack.EMPTY : out.getKey().copy(), out.getValue());
	}
}
