package com.hbm.inventory.recipes;

import static com.hbm.inventory.OreDictManager.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockNTMSand.EnumSandType;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.material.NTMMaterial.SmeltingBehavior;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

/**
 * Electric arc furnace recipes: a solid output (solid mode) and/or molten materials (liquid mode) per input item.
 * Besides the fixed recipes the original generated recipes for every [shape][material] ore dictionary entry, for the
 * custom smeltables of MatDistribution and for furnace recipes of ingots, ores, plates and blocks. Those need item
 * tags and the smelting recipes, so the whole list is built on first use and rebuilt after tags reload.
 *
 * TODO bedrock ore recipes (ItemBedrockOreNew), scraps in liquid mode (ItemScraps), JSON config, JEI
 */
@EventBusSubscriber(modid = RefStrings.MODID)
public class ArcFurnaceRecipes {

	public static final List<Map.Entry<AStack, ArcFurnaceRecipe>> recipeList = new ArrayList<>();
	/* quick lookup for translating input stacks into the output */
	private static final Map<ComparableStack, Optional<ArcFurnaceRecipe>> fastCacheSolid = new HashMap<>();
	private static final Map<ComparableStack, Optional<ArcFurnaceRecipe>> fastCacheLiquid = new HashMap<>();
	/* used for the recipe creation process to cache which inputs are already in use to prevent input collisions */
	private static final HashSet<ComparableStack> occupiedSolid = new HashSet<>();
	private static final HashSet<ComparableStack> occupiedLiquid = new HashSet<>();

	private static boolean built = false;

	@SubscribeEvent
	public static void onTagsUpdated(TagsUpdatedEvent event) {
		invalidate();
		Mats.clearCache();
	}

	public static synchronized void invalidate() {
		built = false;
		recipeList.clear();
		fastCacheSolid.clear();
		fastCacheLiquid.clear();
		occupiedSolid.clear();
		occupiedLiquid.clear();
	}

	private static synchronized void build(Level level) {
		if(built) return;
		invalidate();

		register(new OreDictStack(KEY_SAND),			new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon.get()))		.fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.NUGGET.q(1))));
		register(new ComparableStack(Items.FLINT),		new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon.get(), 4))	.fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.INGOT.q(1, 2))));
		register(new OreDictStack(QUARTZ.gem()),		new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon.get(), 3))	.fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.NUGGET.q(3))));
		register(new OreDictStack(QUARTZ.dust()),		new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon.get(), 3))	.fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.NUGGET.q(3))));
		register(new OreDictStack(QUARTZ.block()),		new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon.get(), 12))	.fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.NUGGET.q(12))));
		register(new OreDictStack(FIBER.ingot()),		new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon.get(), 4))	.fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.INGOT.q(1, 2))));
		register(new OreDictStack(FIBER.block()),		new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon.get(), 40))	.fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.INGOT.q(9, 2))));
		register(new OreDictStack(ASBESTOS.ingot()),	new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon.get(), 4))	.fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.INGOT.q(1, 2))));
		register(new OreDictStack(ASBESTOS.dust()),		new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon.get(), 4))	.fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.INGOT.q(1, 2))));
		register(new OreDictStack(ASBESTOS.block()),	new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon.get(), 40))	.fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.INGOT.q(9, 2))));

		register(new ComparableStack(ModBlocks.sand_mix.get(EnumSandType.QUARTZ).get()), new ArcFurnaceRecipe().solid(new ItemStack(ModBlocks.glass_quartz.get())));
		register(new OreDictStack(BORAX.dust()), new ArcFurnaceRecipe().solid(new ItemStack(ModItems.powder_boron_tiny.get(), 3)).fluid(new MaterialStack(Mats.MAT_BORON, MaterialShapes.NUGGET.q(3))));

		// Autogen for simple single type items
		for(NTMMaterial material : Mats.orderedList) {
			int in = material.convIn;
			int out = material.convOut;
			NTMMaterial convert = material.smeltsInto;
			if(convert.smeltable == SmeltingBehavior.SMELTABLE) {
				for(MaterialShapes shape : MaterialShapes.allShapes) {
					if(!shape.noAutogen) {
						String name = shape.name() + material.names[0];
						if(tagHasItems(name)) {
							ArcFurnaceRecipe recipe = new ArcFurnaceRecipe();
							recipe.fluid(new MaterialStack(convert, (int) (shape.q(1) * out / in)));
							register(new OreDictStack(name), recipe);
						}
					}
				}
			}
		}

		// Autogen for custom smeltables
		for(Map.Entry<String, List<MaterialStack>> entry : Mats.materialOreEntries.entrySet()) {
			addCustomSmeltable(new OreDictStack(entry.getKey()), entry.getValue());
		}
		for(Map.Entry<ComparableStack, List<MaterialStack>> entry : Mats.materialEntries.entrySet()) {
			addCustomSmeltable(entry.getKey(), entry.getValue());
		}

		// Autogen for furnace recipes
		if(level != null) registerFurnaceSmeltables(level.getRecipeManager(), level.registryAccess());

		built = true;
	}

	private static boolean tagHasItems(String dictKey) {
		TagKey<Item> tag = com.hbm.inventory.OreDictManager.tag(dictKey);
		return BuiltInRegistries.ITEM.getTag(tag).map(set -> set.size() > 0).orElse(false);
	}

	/** The original's arcSmeltable: everything registered as an ingot, ore, plate or block, and a few vanilla items */
	public static boolean isArcSmeltable(ItemStack stack) {
		return stack.is(Tags.Items.INGOTS) || stack.is(Tags.Items.ORES) || stack.is(Tags.Items.STORAGE_BLOCKS) || stack.is(PLATES)
				|| stack.is(Items.BRICK) || stack.is(Items.NETHER_BRICK);
	}

	private static final TagKey<Item> PLATES = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "plates"));

	public static void registerFurnaceSmeltables(RecipeManager recipes, net.minecraft.core.HolderLookup.Provider registries) {
		for(RecipeHolder<SmeltingRecipe> holder : recipes.getAllRecipesFor(RecipeType.SMELTING)) {
			SmeltingRecipe smelting = holder.value();
			ItemStack output = smelting.getResultItem(registries);
			if(output.isEmpty() || smelting.getIngredients().isEmpty()) continue;

			for(ItemStack input : smelting.getIngredients().get(0).getItems()) {
				if(isArcSmeltable(input) || isArcSmeltable(output)) {
					ArcFurnaceRecipe recipe = new ArcFurnaceRecipe();
					recipe.solid(output.copy());
					register(new ComparableStack(input).makeSingular(), recipe);
				}
			}
		}
	}

	public static void register(AStack input, ArcFurnaceRecipe output) {
		List<ItemStack> inputs = input.extractForNEI();
		for(ItemStack stack : inputs) {
			ComparableStack compStack = new ComparableStack(stack).makeSingular();
			if(output.solidOutput != null) if(occupiedSolid.contains(compStack)) return;
			if(output.fluidOutput != null) if(occupiedLiquid.contains(compStack)) return;
		}
		recipeList.add(Map.entry(input, output));
		for(ItemStack stack : inputs) {
			ComparableStack compStack = new ComparableStack(stack).makeSingular();
			if(output.solidOutput != null) occupiedSolid.add(compStack);
			if(output.fluidOutput != null) occupiedLiquid.add(compStack);
		}
	}

	private static void addCustomSmeltable(AStack astack, List<MaterialStack> mats) {
		List<MaterialStack> smeltables = new ArrayList<>();
		for(MaterialStack mat : mats) {
			if(mat.material.smeltable == SmeltingBehavior.SMELTABLE) {
				smeltables.add(mat);
			}
		}
		if(smeltables.isEmpty()) return;
		ArcFurnaceRecipe recipe = new ArcFurnaceRecipe();
		recipe.fluid(smeltables.toArray(new MaterialStack[0]));
		register(astack, recipe);
	}

	/** The recipe for the item in the given mode, null if there is none. The level provides the smelting recipes the list is built from. */
	public static synchronized ArcFurnaceRecipe getOutput(ItemStack stack, boolean liquid, Level level) {

		if(stack.isEmpty()) return null;
		if(!built) build(level);

		ComparableStack cacheKey = new ComparableStack(stack).makeSingular();
		Map<ComparableStack, Optional<ArcFurnaceRecipe>> cache = liquid ? fastCacheLiquid : fastCacheSolid;
		Optional<ArcFurnaceRecipe> cached = cache.get(cacheKey);
		if(cached != null) return cached.orElse(null);

		for(Map.Entry<AStack, ArcFurnaceRecipe> entry : recipeList) {
			if(entry.getKey().matchesRecipe(stack, true)) {
				ArcFurnaceRecipe rec = entry.getValue();
				if((liquid && rec.fluidOutput != null) || (!liquid && rec.solidOutput != null)) {
					cache.put(cacheKey, Optional.of(rec));
					return rec;
				}
			}
		}

		cache.put(cacheKey, Optional.empty());
		return null;
	}

	public static class ArcFurnaceRecipe {

		public MaterialStack[] fluidOutput;
		public ItemStack solidOutput;

		public ArcFurnaceRecipe fluid(MaterialStack... outputs) {
			this.fluidOutput = outputs;
			return this;
		}

		public ArcFurnaceRecipe fluidNull(MaterialStack... outputs) {
			List<MaterialStack> mat = new ArrayList<>();
			for(MaterialStack stack : outputs) if(stack != null) mat.add(stack);
			if(!mat.isEmpty()) this.fluidOutput = mat.toArray(new MaterialStack[0]);
			return this;
		}

		public ArcFurnaceRecipe solid(ItemStack output) {
			this.solidOutput = output;
			return this;
		}
	}
}
