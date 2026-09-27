package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineRadiolysis;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The RTG and radiolysis chamber: power, cracking water with the heat, sterilization */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class RadiolysisGameTests {

	private static TileEntityMachineRadiolysis place(GameTestHelper helper) {
		BlockPos core = ModBlocks.machine_radiolysis.get().placeMultiblock(helper.getLevel(), helper.absolutePos(new BlockPos(3, 1, 3)), Direction.NORTH);
		TileEntityMachineRadiolysis radiolysis = (TileEntityMachineRadiolysis) helper.getLevel().getBlockEntity(core);
		// lead pellets are the hottest, 600 heat each
		radiolysis.setItem(0, new ItemStack(ModItems.pellet_rtg_lead.get()));
		return radiolysis;
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 40)
	public static void radiolysisCracksWater(GameTestHelper helper) {
		TileEntityMachineRadiolysis radiolysis = place(helper);
		radiolysis.tanks[0].setTankType(Fluids.WATER);
		radiolysis.tanks[0].setFill(2_000);

		helper.runAfterDelay(1, () -> {
			helper.assertTrue(radiolysis.heat == 600 && radiolysis.getPower() == 6_000, "600 heat make 6000HE per tick, has " + radiolysis.getPower());
			helper.assertTrue(radiolysis.tanks[1].getTankType() == Fluids.PEROXIDE && radiolysis.tanks[2].getTankType() == Fluids.HYDROGEN, "water splits into peroxide and hydrogen");
		});

		// that hot it cracks every 5 ticks
		helper.runAfterDelay(30, () -> {
			helper.assertTrue(radiolysis.tanks[1].getFill() >= 400 && radiolysis.tanks[2].getFill() >= 100, "several cracks should have happened, peroxide " + radiolysis.tanks[1].getFill());
			helper.assertTrue(radiolysis.tanks[0].getFill() <= 1_500, "100mB water per crack");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 120)
	public static void radiolysisSterilizes(GameTestHelper helper) {
		TileEntityMachineRadiolysis radiolysis = place(helper);

		CompoundTag tag = new CompoundTag();
		tag.putBoolean("ntmContagion", true);
		ItemStack infected = new ItemStack(Items.IRON_INGOT, 2);
		infected.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		radiolysis.setItem(12, infected);

		helper.succeedWhen(() -> {
			ItemStack out = radiolysis.getItem(13);
			helper.assertTrue(out.is(Items.IRON_INGOT) && out.getCount() == 1, "one ingot should be sterilized, got " + out);
			helper.assertTrue(out.get(DataComponents.CUSTOM_DATA) == null || !out.get(DataComponents.CUSTOM_DATA).copyTag().getBoolean("ntmContagion"), "the contagion should be gone");
			helper.assertTrue(radiolysis.getItem(12).getCount() == 1, "one infected ingot left");
		});
	}
}
