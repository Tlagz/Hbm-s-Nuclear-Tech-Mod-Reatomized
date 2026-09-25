package com.hbm.inventory.material;

import static com.hbm.inventory.material.MaterialShapes.*;
import static com.hbm.inventory.material.Mats.*;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockEnums.EnumStoneType;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.ItemEnums.EnumAshType;
import com.hbm.items.ItemEnums.EnumCasingType;
import com.hbm.items.ItemEnums.EnumChunkType;
import com.hbm.items.ModItems;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

/**
 * How much of which material items contain when smelted in the crucible or the arc furnace, for items that aren't
 * [shape][material] ore dictionary entries (those are recognized by Mats.getMaterialsFromItem on their own).
 * Amounts are in quanta (1/72 of an ingot).
 *
 * TODO JSON config (SerializableRecipe, hbmCrucibleSmelting.json)
 */
public class MatDistribution {

	public static void registerDefaults() {
		Mats.materialEntries.clear();
		Mats.materialOreEntries.clear();
		Mats.clearCache();

		//vanilla crap
		registerOre("stone", MAT_STONE, BLOCK.q(1));
		registerOre("cobblestone", MAT_STONE, BLOCK.q(1));
		registerEntry(Blocks.OBSIDIAN, MAT_OBSIDIAN, BLOCK.q(1));
		registerEntry(Blocks.RAIL, MAT_IRON, INGOT.q(6, 16));
		registerEntry(Blocks.POWERED_RAIL, MAT_GOLD, INGOT.q(6, 6), MAT_REDSTONE, DUST.q(1, 6));
		registerEntry(Blocks.DETECTOR_RAIL, MAT_IRON, INGOT.q(6, 6), MAT_REDSTONE, DUST.q(1, 6));
		registerEntry(Items.MINECART, MAT_IRON, INGOT.q(5));

		//castables
		registerEntry(ModItems.blade_titanium.get(),			MAT_TITANIUM,		INGOT.q(3));
		registerEntry(ModItems.blade_tungsten.get(),			MAT_TUNGSTEN,		INGOT.q(3));
		registerEntry(ModItems.blades_steel.get(),				MAT_STEEL,			INGOT.q(4));
		registerEntry(ModItems.blades_titanium.get(),			MAT_TITANIUM, 		INGOT.q(4));
		registerEntry(ModItems.stamp_stone_flat.get(),			MAT_STONE,			INGOT.q(3));
		registerEntry(ModItems.stamp_iron_flat.get(),			MAT_IRON,			INGOT.q(3));
		registerEntry(ModItems.stamp_steel_flat.get(),			MAT_STEEL,			INGOT.q(3));
		registerEntry(ModItems.stamp_titanium_flat.get(),		MAT_TITANIUM,		INGOT.q(3));
		registerEntry(ModItems.stamp_obsidian_flat.get(),		MAT_OBSIDIAN,		INGOT.q(3));
		registerEntry(ModItems.pipes_steel.get(),				MAT_STEEL,			BLOCK.q(3));

		registerEntry(ModItems.casing.stack(EnumCasingType.SMALL),			MAT_GUNMETAL,		PLATE.q(1, 4));
		registerEntry(ModItems.casing.stack(EnumCasingType.SMALL_STEEL),	MAT_WEAPONSTEEL,	PLATE.q(1, 4));
		registerEntry(ModItems.casing.stack(EnumCasingType.LARGE),			MAT_GUNMETAL,		PLATE.q(1, 2));
		registerEntry(ModItems.casing.stack(EnumCasingType.LARGE_STEEL),	MAT_WEAPONSTEEL,	PLATE.q(1, 2));
		registerEntry(ModItems.chunk_ore.stack(EnumChunkType.CRYOLITE), MAT_ALUMINIUM, INGOT.q(1), MAT_SODIUM, INGOT.q(1));

		//actual ores
		registerOre(OreDictManager.IRON.ore(), MAT_IRON, INGOT.q(2), MAT_TITANIUM, NUGGET.q(3), MAT_STONE, QUART.q(1));
		registerOre(OreDictManager.TI.ore(), MAT_TITANIUM, INGOT.q(2), MAT_IRON, NUGGET.q(3), MAT_STONE, QUART.q(1));
		registerOre(OreDictManager.W.ore(), MAT_TUNGSTEN, INGOT.q(2), MAT_STONE, QUART.q(1));
		registerOre(OreDictManager.AL.ore(), MAT_ALUMINIUM, INGOT.q(2), MAT_SODIUM, NUGGET.q(3), MAT_STONE, QUART.q(1));

		registerOre(OreDictManager.COAL.ore(), MAT_CARBON, GEM.q(3), MAT_STONE, QUART.q(1));
		registerOre(OreDictManager.GOLD.ore(), MAT_GOLD, INGOT.q(2), MAT_LEAD, NUGGET.q(3), MAT_STONE, QUART.q(1));
		registerOre(OreDictManager.U.ore(), MAT_URANIUM, INGOT.q(2), MAT_LEAD, NUGGET.q(3), MAT_STONE, QUART.q(1));
		for(String name : OreDictManager.TH232.mats) registerOre(ONLY_ORE.name() + name, MAT_THORIUM, INGOT.q(2), MAT_URANIUM, NUGGET.q(3), MAT_STONE, QUART.q(1));
		registerOre(OreDictManager.CU.ore(), MAT_COPPER, INGOT.q(2), MAT_STONE, QUART.q(1));
		registerOre(OreDictManager.PB.ore(), MAT_LEAD, INGOT.q(2), MAT_GOLD, NUGGET.q(1), MAT_STONE, QUART.q(1));
		registerOre(OreDictManager.BE.ore(), MAT_BERYLLIUM, INGOT.q(2), MAT_STONE, QUART.q(1));
		registerOre(OreDictManager.CO.ore(), MAT_COBALT, INGOT.q(1), MAT_STONE, QUART.q(1));
		registerOre(OreDictManager.REDSTONE.ore(), MAT_REDSTONE, INGOT.q(4), MAT_STONE, QUART.q(1));

		registerOre(OreDictManager.HEMATITE.ore(), MAT_HEMATITE, INGOT.q(1));
		registerOre(OreDictManager.MALACHITE.ore(), MAT_MALACHITE, INGOT.q(6));

		registerEntry(ModBlocks.stone_resource.stack(EnumStoneType.LIMESTONE), MAT_FLUX, DUST.q(10));
		registerEntry(ModItems.powder_flux.get(), MAT_FLUX, DUST.q(1));
		registerEntry(Items.CHARCOAL, MAT_CARBON, NUGGET.q(3));

		registerEntry(ModItems.powder_ash.stack(EnumAshType.WOOD), MAT_CARBON, NUGGET.q(1));
		registerEntry(ModItems.powder_ash.stack(EnumAshType.COAL), MAT_CARBON, NUGGET.q(2));
		registerEntry(ModItems.powder_ash.stack(EnumAshType.MISC), MAT_CARBON, NUGGET.q(1));
	}

	public static void registerEntry(Object key, Object... matDef) {
		ComparableStack comp = null;

		if(key instanceof ItemLike like) comp = new ComparableStack(like);
		if(key instanceof ItemStack stack) comp = new ComparableStack(stack).makeSingular();
		if(key instanceof ComparableStack stack) comp = stack;

		if(comp == null) return;
		if(matDef.length % 2 == 1) return;

		List<MaterialStack> stacks = new ArrayList<>();

		for(int i = 0; i < matDef.length; i += 2) {
			stacks.add(new MaterialStack((NTMMaterial) matDef[i], (int) matDef[i + 1]));
		}

		if(stacks.isEmpty()) return;

		Mats.materialEntries.put(comp, stacks);
	}

	public static void registerOre(String key, Object... matDef) {
		if(matDef.length % 2 == 1) return;

		List<MaterialStack> stacks = new ArrayList<>();

		for(int i = 0; i < matDef.length; i += 2) {
			stacks.add(new MaterialStack((NTMMaterial) matDef[i], (int) matDef[i + 1]));
		}

		if(stacks.isEmpty()) return;

		Mats.materialOreEntries.put(key, stacks);
	}
}
