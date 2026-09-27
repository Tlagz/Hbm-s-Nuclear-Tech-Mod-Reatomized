package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineDiFurnace;
import com.hbm.inventory.recipes.BlastFurnaceRecipes;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityDiFurnace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

/** The alloy furnace: recipes in either order, fuel, the extension and the per-slot input sides */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class DiFurnaceGameTests {

	@GameTest(template = "empty_8x4x8")
	public static void alloyRecipesMatchEitherOrder(GameTestHelper helper) {
		ItemStack iron = new ItemStack(Items.IRON_INGOT);
		ItemStack coal = new ItemStack(Items.COAL);
		helper.assertTrue(BlastFurnaceRecipes.getOutput(iron, coal).is(ModItems.ingot_steel.get()), "iron and coal make steel");
		helper.assertTrue(BlastFurnaceRecipes.getOutput(coal, iron).is(ModItems.ingot_steel.get()), "in either order");
		helper.assertTrue(BlastFurnaceRecipes.getOutput(new ItemStack(ModItems.ingot_copper.get()), new ItemStack(Items.REDSTONE)).getCount() == 2, "copper and redstone make two red copper");
		helper.assertTrue(BlastFurnaceRecipes.getOutput(iron, iron).isEmpty(), "iron and iron make nothing");
		helper.succeed();
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 200)
	public static void extensionTriplesSpeed(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 3));
		helper.getLevel().setBlockAndUpdate(pos, ModBlocks.machine_difurnace_off.get().defaultBlockState());
		helper.getLevel().setBlockAndUpdate(pos.above(), ModBlocks.machine_difurnace_extension.get().defaultBlockState());
		TileEntityDiFurnace furnace = (TileEntityDiFurnace) helper.getLevel().getBlockEntity(pos);

		helper.assertTrue(helper.getLevel().getBlockState(pos).getValue(MachineDiFurnace.EXTENDED), "the furnace should notice the extension");

		// items go in through the extension, all sides default to the top
		IItemHandler handler = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, pos.above(), Direction.UP);
		helper.assertTrue(handler != null, "the extension should hand out the furnace's inventory");
		handler.insertItem(0, new ItemStack(Items.IRON_INGOT, 2), false);
		handler.insertItem(1, new ItemStack(Items.COAL, 2), false);
		handler.insertItem(2, new ItemStack(Items.COAL, 4), false);
		helper.assertTrue(furnace.getItem(0).getCount() == 2 && furnace.getItem(2).getCount() == 4, "inputs and fuel should be in the furnace");

		// 400 progress per alloy, 3 per tick with the extension
		helper.runAfterDelay(140, () -> {
			helper.assertTrue(furnace.getItem(3).is(ModItems.ingot_steel.get()) && furnace.getItem(3).getCount() == 1, "one steel after 140 ticks, got " + furnace.getItem(3));
			helper.assertTrue(helper.getLevel().getBlockState(pos).getValue(MachineDiFurnace.LIT), "the furnace should be lit");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void inputSidesAreConfigurable(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 3));
		helper.getLevel().setBlockAndUpdate(pos, ModBlocks.machine_difurnace_off.get().defaultBlockState());
		TileEntityDiFurnace furnace = (TileEntityDiFurnace) helper.getLevel().getBlockEntity(pos);

		furnace.sideFuel = (byte) Direction.NORTH.get3DDataValue();
		helper.assertFalse(furnace.canInsertItem(2, new ItemStack(Items.COAL), Direction.UP), "fuel from the top is refused now");
		helper.assertTrue(furnace.canInsertItem(2, new ItemStack(Items.COAL), Direction.NORTH), "fuel from the north is accepted");
		helper.assertTrue(furnace.canInsertItem(0, new ItemStack(Items.IRON_INGOT), Direction.UP), "the upper input still takes items from the top");
		helper.assertFalse(furnace.canInsertItem(3, new ItemStack(Items.IRON_INGOT), Direction.UP), "the output takes nothing");
		helper.succeed();
	}
}
