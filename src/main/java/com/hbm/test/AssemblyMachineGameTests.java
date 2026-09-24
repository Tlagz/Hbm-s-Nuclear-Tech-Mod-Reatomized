package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.AssemblyMachineRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBlueprints;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineAssemblyMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The assembly machine and the generic recipe system: recipe selection, input filtering, blueprint pools, auto switch */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class AssemblyMachineGameTests {

	private static TileEntityMachineAssemblyMachine place(GameTestHelper helper) {
		BlockPos core = ModBlocks.machine_assembly_machine.get().placeMultiblock(helper.getLevel(), helper.absolutePos(new BlockPos(4, 1, 3)), Direction.NORTH);
		TileEntityMachineAssemblyMachine assembler = (TileEntityMachineAssemblyMachine) helper.getLevel().getBlockEntity(core);
		assembler.setItem(0, new ItemStack(ModItems.battery_creative.get()));
		return assembler;
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 200)
	public static void assemblerMakesHazmatCloth(GameTestHelper helper) {
		helper.assertTrue(AssemblyMachineRecipes.INSTANCE.recipeOrderedList.size() > 50, "the translated recipes should be registered");
		TileEntityMachineAssemblyMachine assembler = place(helper);

		helper.assertFalse(assembler.canPlaceItem(4, new ItemStack(ModItems.powder_lead.get())), "without a recipe the inputs take nothing");

		// what the recipe selector sends when it closes
		CompoundTag data = new CompoundTag();
		data.putInt("index", 0);
		data.putString("selection", "ass.hazcloth");
		assembler.receiveControl(data);
		helper.assertTrue(assembler.assemblerModule.getRecipeName().equals("ass.hazcloth"), "the selector should set the recipe");

		helper.assertTrue(assembler.canPlaceItem(4, new ItemStack(ModItems.powder_lead.get())), "slot 4 takes the lead dust");
		helper.assertFalse(assembler.canPlaceItem(4, new ItemStack(Items.STRING)), "slot 4 doesn't take the string");

		assembler.setItem(4, new ItemStack(ModItems.powder_lead.get(), 8));
		assembler.setItem(5, new ItemStack(Items.STRING, 16));

		helper.succeedWhen(() -> {
			helper.assertTrue(assembler.getItem(16).is(ModItems.hazmat_cloth.get()) && assembler.getItem(16).getCount() == 8, "two crafts should make 8 hazmat cloth, output: " + assembler.getItem(16));
			helper.assertTrue(assembler.getItem(4).isEmpty() && assembler.getItem(5).isEmpty(), "the inputs should be used up");
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 200)
	public static void platesNeedBlueprintAndAutoSwitch(GameTestHelper helper) {
		TileEntityMachineAssemblyMachine assembler = place(helper);
		assembler.assemblerModule.setRecipe("ass.platesteel", false);

		helper.runAtTickTime(5, () -> {
			helper.assertTrue(assembler.assemblerModule.getRecipeName().equals("null"), "the pooled plate recipe needs the blueprint, is " + assembler.assemblerModule.getRecipeName());

			assembler.setItem(1, ItemBlueprints.make(GenericRecipes.POOL_PREFIX_ALT + "plates"));
			assembler.assemblerModule.setRecipe("ass.platesteel", false);
			// copper in a steel plate recipe: the plates auto switch group changes the recipe
			assembler.setItem(4, new ItemStack(ModItems.ingot_copper.get(), 2));
		});

		helper.succeedWhen(() -> {
			helper.assertTrue(assembler.assemblerModule.getRecipeName().equals("ass.platecopper"), "the recipe should switch to copper plates, is " + assembler.assemblerModule.getRecipeName());
			helper.assertTrue(assembler.getItem(16).is(ModItems.plate_copper.get()), "the assembler should make copper plates, output: " + assembler.getItem(16));
		});
	}
}
