package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemWasteShort.WasteClass;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineRadGen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Radiation-powered engine: queues burn radioactive items for power and give back the decayed version */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class RadGenGameTests {

	private static TileEntityMachineRadGen place(GameTestHelper helper) {
		BlockPos core = ModBlocks.machine_radgen.get().placeMultiblock(helper.getLevel(), helper.absolutePos(new BlockPos(8, 1, 8)), Direction.NORTH);
		helper.assertTrue(core != null, "the radgen should fit");
		return (TileEntityMachineRadGen) helper.getLevel().getBlockEntity(core);
	}

	@GameTest(template = "empty_16x8x16")
	public static void radgenBurnsWasteIntoDepletedWaste(GameTestHelper helper) {
		TileEntityMachineRadGen radgen = place(helper);
		radgen.setItem(0, ModItems.nuclear_waste_short_tiny.stack(WasteClass.PLUTONIUM239, 2));
		radgen.setItem(1, new ItemStack(ModItems.scrap_nuclear.get()));

		helper.runAfterDelay(2, () -> {
			helper.assertTrue(radgen.isOn, "it should be running");
			helper.assertTrue(radgen.getItem(0).getCount() == 1, "one waste pile goes into the queue at a time");
			helper.assertTrue(radgen.production[0] == 150 && radgen.production[1] == 50, "tiny short-lived waste makes 150 HE/t, nuclear scrap 50");
			helper.assertTrue(radgen.getPower() >= 200, "power adds up, has " + radgen.getPower());
			// skip to the end of the queues
			radgen.progress[0] = radgen.maxProgress[0] - 1;
			radgen.progress[1] = radgen.maxProgress[1] - 1;
		});

		helper.runAfterDelay(4, () -> {
			helper.assertTrue(radgen.getItem(12).is(ModItems.nuclear_waste_short_depleted_tiny.get(WasteClass.PLUTONIUM239).get()), "decayed waste of the same class comes out, got " + radgen.getItem(12));
			helper.assertTrue(radgen.getItem(13).isEmpty(), "nuclear scrap leaves nothing behind");
			helper.assertTrue(radgen.getItem(0).isEmpty() && radgen.maxProgress[0] > 0, "the second pile got loaded");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_16x8x16")
	public static void radgenRejectsNonFuel(GameTestHelper helper) {
		TileEntityMachineRadGen radgen = place(helper);
		helper.assertTrue(!radgen.isItemValidForSlot(0, new ItemStack(net.minecraft.world.item.Items.DIRT)), "dirt isn't fuel");
		helper.assertTrue(radgen.isItemValidForSlot(0, new ItemStack(ModItems.gem_rad.get())), "radioactive gems are");
		helper.assertTrue(!radgen.isItemValidForSlot(12, new ItemStack(ModItems.gem_rad.get())), "but not into output slots");
		helper.succeed();
	}
}
