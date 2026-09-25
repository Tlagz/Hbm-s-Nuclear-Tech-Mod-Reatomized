package com.hbm.crafting;

import static com.hbm.crafting.RecipeBase.addRecipeAuto;

import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.ModItems;

/**
 * Crafting recipes of the original's CraftingManager that loop over the materials, which gen_recipes.py doesn't
 * translate. The LBSM simple crafting wires (3 ingots -> 24 wires) are left out, LBSM is off by default.
 */
public class MaterialRecipes {

	public static void register() {
		for(NTMMaterial mat : Mats.orderedList) {
			if(mat.autogen.contains(MaterialShapes.BOLT)) for(String name : mat.names) addRecipeAuto(ModItems.bolt.stack(mat, 16), new Object[] { "#", "#", '#', MaterialShapes.INGOT.name() + name });
		}
	}
}
