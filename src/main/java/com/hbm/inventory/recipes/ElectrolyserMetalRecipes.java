package com.hbm.inventory.recipes;

import java.util.HashMap;
import java.util.Map.Entry;

import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.ItemEnums.EnumChunkType;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Electrolyser metal mode: a crystal (and 100mB nitric acid) into two molten metals and byproduct items.
 *
 * TODO the bedrock ore recipes (bedrock ores aren't ported), JSON config, JEI
 */
public class ElectrolyserMetalRecipes {

	public static HashMap<AStack, ElectrolysisMetalRecipe> recipes = new HashMap<>();

	private static void put(Item crystal, MaterialStack out1, MaterialStack out2, ItemStack... byproduct) {
		recipes.put(new ComparableStack(crystal), new ElectrolysisMetalRecipe(out1, out2, byproduct));
	}

	private static MaterialStack ingots(com.hbm.inventory.material.NTMMaterial mat, int n) { return new MaterialStack(mat, MaterialShapes.INGOT.q(n)); }
	private static MaterialStack nuggets(com.hbm.inventory.material.NTMMaterial mat, int n) { return new MaterialStack(mat, MaterialShapes.NUGGET.q(n)); }
	private static ItemStack lithium() { return new ItemStack(ModItems.powder_lithium_tiny.get(), 3); }

	public static void registerDefaults() {
		recipes.clear();

		put(ModItems.crystal_iron.get(), ingots(Mats.MAT_IRON, 6), ingots(Mats.MAT_TITANIUM, 2), lithium());
		put(ModItems.crystal_gold.get(), ingots(Mats.MAT_GOLD, 6), ingots(Mats.MAT_LEAD, 2), lithium(), new ItemStack(ModItems.ingot_mercury.get(), 2));
		put(ModItems.crystal_uranium.get(), ingots(Mats.MAT_URANIUM, 6), nuggets(Mats.MAT_RADIUM, 4), lithium());
		put(ModItems.crystal_thorium.get(), ingots(Mats.MAT_THORIUM, 6), ingots(Mats.MAT_URANIUM, 2), lithium());
		put(ModItems.crystal_plutonium.get(), ingots(Mats.MAT_PLUTONIUM, 6), ingots(Mats.MAT_POLONIUM, 2), lithium());
		put(ModItems.crystal_titanium.get(), ingots(Mats.MAT_TITANIUM, 6), ingots(Mats.MAT_IRON, 2), lithium());
		put(ModItems.crystal_copper.get(), ingots(Mats.MAT_COPPER, 6), nuggets(Mats.MAT_LEAD, 4), lithium(), new ItemStack(ModItems.sulfur.get(), 2));
		put(ModItems.crystal_tungsten.get(), ingots(Mats.MAT_TUNGSTEN, 6), ingots(Mats.MAT_IRON, 2), lithium());
		put(ModItems.crystal_aluminium.get(), ingots(Mats.MAT_ALUMINIUM, 2), ingots(Mats.MAT_IRON, 2), new ItemStack(ModItems.chunk_ore.get(EnumChunkType.CRYOLITE).get(), 4), lithium());
		put(ModItems.crystal_beryllium.get(), ingots(Mats.MAT_BERYLLIUM, 6), nuggets(Mats.MAT_LEAD, 4), lithium(), new ItemStack(ModItems.powder_quartz.get(), 2));
		put(ModItems.crystal_lead.get(), ingots(Mats.MAT_LEAD, 6), ingots(Mats.MAT_GOLD, 2), lithium());
		put(ModItems.crystal_schraranium.get(), nuggets(Mats.MAT_SCHRABIDIUM, 5), nuggets(Mats.MAT_URANIUM, 2), new ItemStack(ModItems.nugget_neptunium.get(), 2));
		put(ModItems.crystal_schrabidium.get(), ingots(Mats.MAT_SCHRABIDIUM, 6), ingots(Mats.MAT_PLUTONIUM, 2), lithium());
		put(ModItems.crystal_rare.get(), nuggets(Mats.MAT_ZIRCONIUM, 6), nuggets(Mats.MAT_BORON, 2), new ItemStack(ModItems.powder_desh_mix.get(), 3));
		put(ModItems.crystal_trixite.get(), ingots(Mats.MAT_PLUTONIUM, 3), ingots(Mats.MAT_COBALT, 4), new ItemStack(ModItems.powder_niobium.get(), 4), new ItemStack(ModItems.powder_nitan_mix.get(), 2));
		put(ModItems.crystal_lithium.get(), ingots(Mats.MAT_LITHIUM, 6), ingots(Mats.MAT_BORON, 2), new ItemStack(ModItems.powder_quartz.get(), 2), new ItemStack(ModItems.fluorite.get(), 2));
		put(ModItems.crystal_starmetal.get(), ingots(Mats.MAT_DURA, 4), ingots(Mats.MAT_COBALT, 4), new ItemStack(ModItems.powder_astatine.get(), 3), new ItemStack(ModItems.ingot_mercury.get(), 8));
		put(ModItems.crystal_cobalt.get(), ingots(Mats.MAT_COBALT, 3), ingots(Mats.MAT_IRON, 4), new ItemStack(ModItems.powder_copper.get(), 4), lithium());

		MainRegistry.logger.info("Electrolyser metal recipes: " + recipes.size());
	}

	public static ElectrolysisMetalRecipe getRecipe(ItemStack stack) {
		if(stack == null || stack.isEmpty()) return null;

		ElectrolysisMetalRecipe exact = recipes.get(new ComparableStack(stack).makeSingular());
		if(exact != null) return exact;

		for(Entry<AStack, ElectrolysisMetalRecipe> entry : recipes.entrySet()) {
			if(entry.getKey() instanceof OreDictStack dict && dict.matchesRecipe(stack, true)) return entry.getValue();
		}

		return null;
	}

	public static class ElectrolysisMetalRecipe {

		public MaterialStack output1;
		public MaterialStack output2;
		public ItemStack[] byproduct;
		public int duration;

		public ElectrolysisMetalRecipe(MaterialStack output1, MaterialStack output2, ItemStack... byproduct) {
			this(output1, output2, 600, byproduct);
		}

		public ElectrolysisMetalRecipe(MaterialStack output1, MaterialStack output2, int duration, ItemStack... byproduct) {
			this.output1 = output1;
			this.output2 = output2;
			this.byproduct = byproduct;
			this.duration = duration;
		}
	}
}
