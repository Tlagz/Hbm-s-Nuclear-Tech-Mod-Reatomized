package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineExposureChamber;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The exposure chamber turns U-238 into schrabidium with a Higgs boson capsule */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class ExposureChamberGameTests {

	@GameTest(template = "empty_16x8x16", timeoutTicks = 260)
	public static void exposureChamberMakesSchrabidium(GameTestHelper helper) {
		BlockPos core = ModBlocks.machine_exposure_chamber.get().placeMultiblock(helper.getLevel(), helper.absolutePos(new BlockPos(11, 1, 4)), Direction.NORTH);
		helper.assertTrue(core != null, "the chamber and its arm should fit");
		TileEntityMachineExposureChamber chamber = (TileEntityMachineExposureChamber) helper.getLevel().getBlockEntity(core);

		chamber.setItem(5, new ItemStack(ModItems.battery_creative.get()));
		helper.assertTrue(chamber.canPlaceItem(0, new ItemStack(ModItems.particle_higgs.get())), "the particle slot takes the Higgs capsule");
		chamber.setItem(0, new ItemStack(ModItems.particle_higgs.get()));
		chamber.setItem(3, new ItemStack(ModItems.ingot_u238.get()));

		helper.runAfterDelay(2, () -> {
			helper.assertTrue(chamber.savedParticles == 8 && chamber.getItem(0).isEmpty(), "the capsule should be loaded, 8 uses");
			helper.assertTrue(chamber.getItem(2).is(ModItems.particle_empty.get()), "the empty capsule goes into the container slot, got " + chamber.getItem(2));
		});

		helper.succeedWhen(() -> {
			helper.assertTrue(chamber.getItem(4).is(ModItems.ingot_schrabidium.get()), "the ingot should turn into schrabidium, got " + chamber.getItem(4));
			helper.assertTrue(chamber.savedParticles == 7 && chamber.getItem(3).isEmpty(), "one use of the particles, the ingot is gone");
		});
	}
}
