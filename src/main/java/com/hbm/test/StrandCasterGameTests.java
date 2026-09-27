package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMold;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineStrandCaster;
import com.hbm.util.CrucibleUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Strand caster (the whole multiblock, molten metal poured into the funnel) */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class StrandCasterGameTests {

	private static final int INGOT_MOLD = 2;

	@GameTest(template = "empty_8x12x8", timeoutTicks = 60)
	public static void strandCasterCastsIngots(GameTestHelper helper) {
		BlockPos core = ModBlocks.machine_strand_caster.get().placeMultiblock(helper.getLevel(), helper.absolutePos(new BlockPos(3, 1, 0)), Direction.NORTH);
		TileEntityMachineStrandCaster caster = (TileEntityMachineStrandCaster) helper.getLevel().getBlockEntity(core);

		caster.setItem(0, new ItemStack(ItemMold.get(INGOT_MOLD).get()));
		caster.water.setFill(10_000);

		// nine ingots of steel poured onto the funnel fill it up, a full funnel casts right away
		MaterialStack steel = new MaterialStack(Mats.MAT_STEEL, MaterialShapes.INGOT.q(12));
		BlockPos top = core.above(2);
		MaterialStack left = CrucibleUtil.pourSingleStack(helper.getLevel(), top.getX() + 0.5, top.getY() + 3, top.getZ() + 0.5, 6, true, steel, MaterialShapes.INGOT.q(12), new CrucibleUtil.Impact());

		helper.assertTrue(left != null && left.amount == MaterialShapes.INGOT.q(3), "the funnel takes nine ingots worth, got " + (left == null ? 0 : left.amount));
		helper.assertTrue(caster.amount == MaterialShapes.INGOT.q(9) && caster.type == Mats.MAT_STEEL, "nine ingots of steel in the funnel");

		helper.runAfterDelay(2, () -> {
			helper.assertTrue(caster.getItem(1).is(ModItems.ingot_steel.get()) && caster.getItem(1).getCount() == 9, "nine steel ingots cast, got " + caster.getItem(1));
			helper.assertTrue(caster.amount == 0 && caster.steam.getFill() > 0 && caster.water.getFill() < 10_000, "the water turns into spent steam");
			helper.succeed();
		});
	}
}
