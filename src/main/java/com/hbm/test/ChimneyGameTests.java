package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityAshpit;
import com.hbm.tileentity.machine.TileEntityChimneyBase;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Smokestacks swallow smoke and drop its ash into an ashpit below */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class ChimneyGameTests {

	@GameTest(template = "empty_12x20x12")
	public static void brickChimneyCollectsFlyAsh(GameTestHelper helper) {
		BlockPos ashCore = ModBlocks.machine_ashpit.get().placeMultiblock(helper.getLevel(), helper.absolutePos(new BlockPos(5, 1, 5)), Direction.NORTH);
		helper.assertTrue(ashCore != null, "the ashpit should fit");
		BlockPos core = ModBlocks.chimney_brick.get().placeMultiblock(helper.getLevel(), ashCore.above().relative(Direction.NORTH, 1), Direction.NORTH);
		helper.assertTrue(core != null && core.equals(ashCore.above()), "the chimney should sit right on the ashpit, core " + core + " ashpit " + ashCore);

		TileEntityChimneyBase chimney = (TileEntityChimneyBase) helper.getLevel().getBlockEntity(core);
		TileEntityAshpit ashpit = (TileEntityAshpit) helper.getLevel().getBlockEntity(ashCore);

		helper.assertTrue(chimney.transferFluid(Fluids.SMOKE, 0, 500) == 0, "chimneys take any amount of smoke");
		helper.assertTrue(chimney.transferFluid(Fluids.WATER, 0, 500) == 500, "but nothing else");
		helper.assertTrue(chimney.onTicks == 20, "smoke makes the chimney puff");

		helper.runAfterDelay(2, () -> {
			helper.assertTrue(ashpit.ashLevelFly == 500 && ashpit.ashLevelSoot == 0, "brick chimneys catch fly ash only, fly " + ashpit.ashLevelFly + " soot " + ashpit.ashLevelSoot);
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x28x8", timeoutTicks = 60)
	public static void industrialChimneyAlsoCatchesSoot(GameTestHelper helper) {
		BlockPos ashCore = ModBlocks.machine_ashpit.get().placeMultiblock(helper.getLevel(), helper.absolutePos(new BlockPos(5, 1, 5)), Direction.NORTH);
		BlockPos core = ModBlocks.chimney_industrial.get().placeMultiblock(helper.getLevel(), ashCore.above().relative(Direction.NORTH, 1), Direction.NORTH);
		helper.assertTrue(core != null, "the industrial chimney should fit");

		TileEntityChimneyBase chimney = (TileEntityChimneyBase) helper.getLevel().getBlockEntity(core);
		TileEntityAshpit ashpit = (TileEntityAshpit) helper.getLevel().getBlockEntity(ashCore);

		// through a port on the side of the base, like an exhaust pipe would
		BlockPos port = core.relative(Direction.EAST);
		helper.assertTrue(helper.getLevel().getBlockEntity(port) instanceof com.hbm.tileentity.TileEntityProxyCombo, "the base's sides are ports");
		((api.hbm.fluidmk2.IFluidReceiverMK2) helper.getLevel().getBlockEntity(port)).transferFluid(Fluids.SMOKE_POISON, 0, 300);

		helper.runAfterDelay(2, () -> {
			helper.assertTrue(ashpit.ashLevelFly == 300 && ashpit.ashLevelSoot == 300, "industrial chimneys catch both, fly " + ashpit.ashLevelFly + " soot " + ashpit.ashLevelSoot);
			helper.assertTrue(chimney.onTicks > 0, "it's puffing");
			helper.succeed();
		});
	}
}
