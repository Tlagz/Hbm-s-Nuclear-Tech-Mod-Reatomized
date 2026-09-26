package com.hbm.inventory.recipes;

import com.hbm.blocks.BlockEnums.EnumStoneType;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.items.ModItems;

import net.minecraft.world.item.ItemStack;

/**
 * Crucible alloying recipes, picked with the recipe selector. Materials that belong to the selected recipe go into
 * the recipe stack, everything else into the waste stack.
 *
 * IMPORTANT: crucibles do not have stack size checks for the recipe's result, meaning that they can overflow if the
 * resulting stacks are bigger than the input stacks, so make sure that material doesn't "expand".
 *
 * TODO JSON config (hbmCrucible.json), the NEI smelting and mold displays for JEI
 */
public class CrucibleRecipes extends GenericRecipes<CrucibleRecipe> {

	public static final CrucibleRecipes INSTANCE = new CrucibleRecipes();

	@Override public int inputItemLimit() { return 0; }
	@Override public int inputFluidLimit() { return 0; }
	@Override public int outputItemLimit() { return 0; }
	@Override public int outputFluidLimit() { return 0; }
	@Override public boolean hasDuration() { return false; }
	@Override public boolean hasPower() { return false; }

	@Override public CrucibleRecipe instantiateRecipe(String name) { return new CrucibleRecipe(name); }

	@Override
	public void registerDefaults() {

		int n = MaterialShapes.NUGGET.q(1);
		int i = MaterialShapes.INGOT.q(1);

		this.register(new CrucibleRecipe("crucible.steel").setup(20, new ItemStack(ModItems.ingot_steel.get()))
				.inputs(new MaterialStack(Mats.MAT_IRON, n * 2), new MaterialStack(Mats.MAT_CARBON, n * 3), new MaterialStack(Mats.MAT_FLUX, n))
				.outputs(new MaterialStack(Mats.MAT_STEEL, n * 2)));

		this.register(new CrucibleRecipe("crucible.hematite").setup(6, ModBlocks.stone_resource.stack(EnumStoneType.HEMATITE))
				.inputs(new MaterialStack(Mats.MAT_HEMATITE, i * 2), new MaterialStack(Mats.MAT_FLUX, n * 2))
				.outputs(new MaterialStack(Mats.MAT_IRON, i), new MaterialStack(Mats.MAT_SLAG, n * 3)));

		this.register(new CrucibleRecipe("crucible.malachite").setup(6, ModBlocks.stone_resource.stack(EnumStoneType.MALACHITE))
				.inputs(new MaterialStack(Mats.MAT_MALACHITE, i * 2), new MaterialStack(Mats.MAT_FLUX, n * 2))
				.outputs(new MaterialStack(Mats.MAT_COPPER, i), new MaterialStack(Mats.MAT_SLAG, n * 3)));

		this.register(new CrucibleRecipe("crucible.redcopper").setup(2, new ItemStack(ModItems.ingot_red_copper.get()))
				.inputs(new MaterialStack(Mats.MAT_COPPER, n), new MaterialStack(Mats.MAT_REDSTONE, n))
				.outputs(new MaterialStack(Mats.MAT_MINGRADE, n * 2)));

		this.register(new CrucibleRecipe("crucible.hss").setup(9, new ItemStack(ModItems.ingot_dura_steel.get()))
				.inputs(new MaterialStack(Mats.MAT_STEEL, n * 5), new MaterialStack(Mats.MAT_TUNGSTEN, n * 3), new MaterialStack(Mats.MAT_COBALT, n * 1))
				.outputs(new MaterialStack(Mats.MAT_DURA, n * 9)));

		this.register(new CrucibleRecipe("crucible.ferro").setup(3, new ItemStack(ModItems.ingot_ferrouranium.get()))
				.inputs(new MaterialStack(Mats.MAT_STEEL, n * 2), new MaterialStack(Mats.MAT_U238, n))
				.outputs(new MaterialStack(Mats.MAT_FERRO, n * 3)));

		this.register(new CrucibleRecipe("crucible.tcalloy").setup(9, new ItemStack(ModItems.ingot_tcalloy.get()))
				.inputs(new MaterialStack(Mats.MAT_STEEL, n * 8), new MaterialStack(Mats.MAT_TECHNETIUM, n))
				.outputs(new MaterialStack(Mats.MAT_TCALLOY, i)));

		this.register(new CrucibleRecipe("crucible.cdalloy").setup(9, new ItemStack(ModItems.ingot_cdalloy.get()))
				.inputs(new MaterialStack(Mats.MAT_STEEL, n * 8), new MaterialStack(Mats.MAT_CADMIUM, n))
				.outputs(new MaterialStack(Mats.MAT_CDALLOY, i)));

		this.register(new CrucibleRecipe("crucible.bbronze").setup(9, new ItemStack(ModItems.ingot_bismuth_bronze.get()))
				.inputs(new MaterialStack(Mats.MAT_COPPER, n * 8), new MaterialStack(Mats.MAT_BISMUTH, n), new MaterialStack(Mats.MAT_FLUX, n * 3))
				.outputs(new MaterialStack(Mats.MAT_BBRONZE, i), new MaterialStack(Mats.MAT_SLAG, n * 3)));

		this.register(new CrucibleRecipe("crucible.abronze").setup(9, new ItemStack(ModItems.ingot_arsenic_bronze.get()))
				.inputs(new MaterialStack(Mats.MAT_COPPER, n * 8), new MaterialStack(Mats.MAT_ARSENIC, n), new MaterialStack(Mats.MAT_FLUX, n * 3))
				.outputs(new MaterialStack(Mats.MAT_ABRONZE, i), new MaterialStack(Mats.MAT_SLAG, n * 3)));

		this.register(new CrucibleRecipe("crucible.cmb").setup(3, new ItemStack(ModItems.ingot_combine_steel.get()))
				.inputs(new MaterialStack(Mats.MAT_MAGTUNG, n * 6), new MaterialStack(Mats.MAT_MUD, n * 3))
				.outputs(new MaterialStack(Mats.MAT_CMB, i)));

		this.register(new CrucibleRecipe("crucible.magtung").setup(3, new ItemStack(ModItems.ingot_magnetized_tungsten.get()))
				.inputs(new MaterialStack(Mats.MAT_TUNGSTEN, i), new MaterialStack(Mats.MAT_SCHRABIDIUM, n * 1))
				.outputs(new MaterialStack(Mats.MAT_MAGTUNG, i)));

		this.register(new CrucibleRecipe("crucible.bscco").setup(3, new ItemStack(ModItems.ingot_bscco.get()))
				.inputs(new MaterialStack(Mats.MAT_BISMUTH, n * 2), new MaterialStack(Mats.MAT_STRONTIUM, n * 2), new MaterialStack(Mats.MAT_CALCIUM, n * 2), new MaterialStack(Mats.MAT_COPPER, n * 3))
				.outputs(new MaterialStack(Mats.MAT_BSCCO, i)));
	}
}
