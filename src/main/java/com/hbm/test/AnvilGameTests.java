package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.container.ContainerAnvil;
import com.hbm.inventory.recipes.anvil.AnvilRecipes;
import com.hbm.inventory.recipes.anvil.AnvilRecipes.AnvilConstructionRecipe;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.util.InventoryUtil;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The NTM anvil's construction and smithing recipes */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class AnvilGameTests {

	@GameTest(template = "empty_8x4x8")
	public static void constructFireboxFromInventory(GameTestHelper helper) {
		helper.assertTrue(AnvilRecipes.getConstruction().size() > 30, "the translated construction recipes should be registered, " + AnvilRecipes.getConstruction().size());

		AnvilConstructionRecipe firebox = AnvilRecipes.getConstruction().stream()
				.filter(r -> r.output.get(0).stack.is(ModBlocks.heater_firebox.get().asItem())).findFirst().orElse(null);
		helper.assertTrue(firebox != null, "there should be an anvil recipe for the firebox");
		helper.assertTrue(firebox.isTierValid(2) && !firebox.isTierValid(1), "the firebox needs a tier 2 anvil");

		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.getInventory().add(new ItemStack(Items.FURNACE));
		player.getInventory().add(new ItemStack(ModItems.plate_steel.get(), 8));
		player.getInventory().add(new ItemStack(ModItems.ingot_copper.get(), 7));

		helper.assertFalse(InventoryUtil.doesPlayerHaveAStacks(player, firebox.input, true), "7 copper ingots are not enough");
		helper.assertTrue(player.getInventory().countItem(ModItems.plate_steel.get()) == 8, "a failed attempt must not take anything");

		player.getInventory().add(new ItemStack(ModItems.ingot_copper.get(), 1));
		helper.assertTrue(InventoryUtil.doesPlayerHaveAStacks(player, firebox.input, true), "furnace, 8 steel plates and 8 copper ingots make a firebox");
		InventoryUtil.giveChanceStacksToPlayer(player, firebox.output);

		helper.assertTrue(player.getInventory().countItem(ModBlocks.heater_firebox.get().asItem()) == 1, "the player should get the firebox");
		helper.assertTrue(player.getInventory().countItem(ModItems.plate_steel.get()) == 0 && player.getInventory().countItem(Items.FURNACE) == 0, "the ingredients should be used up");
		helper.succeed();
	}

	@GameTest(template = "empty_8x4x8")
	public static void smithIronAnvilIntoSteelAnvil(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		ContainerAnvil anvil = new ContainerAnvil(0, player.getInventory(), 1);

		anvil.input.setItem(0, new ItemStack(ModBlocks.anvil_iron.get()));
		anvil.input.setItem(1, new ItemStack(ModItems.ingot_steel.get(), 9));
		helper.assertTrue(anvil.output.getItem(0).isEmpty(), "9 steel ingots are not enough");

		anvil.input.setItem(1, new ItemStack(ModItems.ingot_steel.get(), 12));
		helper.assertTrue(anvil.output.getItem(0).is(ModBlocks.anvil_steel.get().asItem()), "iron anvil + 10 steel should make a steel anvil, got " + anvil.output.getItem(0));

		// taking the result consumes 1 anvil and 10 ingots
		anvil.slots.get(2).onTake(player, anvil.output.getItem(0));
		helper.assertTrue(anvil.input.getItem(0).isEmpty() && anvil.input.getItem(1).getCount() == 2, "should consume the anvil and 10 ingots, left " + anvil.input.getItem(1));
		helper.succeed();
	}
}
