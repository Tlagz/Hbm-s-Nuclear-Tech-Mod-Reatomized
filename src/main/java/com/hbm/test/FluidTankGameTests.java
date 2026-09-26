package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModDataComponents;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.storage.TileEntityBarrel;
import com.hbm.tileentity.machine.storage.TileEntityMachineFluidTank;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Fluid tank: filling through the corner ports, modes, rupture, keeping the contents (absolute positions) */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class FluidTankGameTests {

	private static TileEntityMachineFluidTank tank(GameTestHelper helper) {
		BlockPos a = helper.absolutePos(BlockPos.ZERO), b = helper.absolutePos(new BlockPos(7, 0, 7));
		BlockPos core = new BlockPos(Math.min(a.getX(), b.getX()) + 3, a.getY() + 1, Math.min(a.getZ(), b.getZ()) + 3);
		helper.assertTrue(core.equals(ModBlocks.machine_fluidtank.get().placeMultiblock(helper.getLevel(), core.north(), Direction.NORTH)), "the tank should fit");
		TileEntityMachineFluidTank tank = (TileEntityMachineFluidTank) helper.getLevel().getBlockEntity(core);
		tank.tank.setTankType(Fluids.DIESEL);
		return tank;
	}

	private static TileEntityBarrel barrel(GameTestHelper helper, BlockPos pos, short mode, int fill) {
		helper.getLevel().setBlockAndUpdate(pos, ModBlocks.barrel_steel.get().defaultBlockState());
		TileEntityBarrel barrel = (TileEntityBarrel) helper.getLevel().getBlockEntity(pos);
		barrel.tank.setTankType(Fluids.DIESEL);
		barrel.tank.setFill(fill);
		barrel.mode = mode;
		return barrel;
	}

	@GameTest(template = "empty_8x4x8")
	public static void tankFillsFromBarrel(GameTestHelper helper) {
		TileEntityMachineFluidTank tank = tank(helper);
		// facing north the tank is 5 wide along x, the ports two blocks out along z are outside, next to the corner blocks
		TileEntityBarrel source = barrel(helper, tank.getBlockPos().offset(1, 0, 2), (short) 2, 16_000);

		helper.succeedWhen(() -> helper.assertTrue(tank.tank.getFill() == 16_000 && source.tank.getFill() == 0, "all diesel should go into the tank, has " + tank.tank.getFill()));
	}

	@GameTest(template = "empty_8x4x8")
	public static void tankProvidesInMode2(GameTestHelper helper) {
		TileEntityMachineFluidTank tank = tank(helper);
		tank.tank.setFill(10_000);
		tank.mode = 2;
		TileEntityBarrel target = barrel(helper, tank.getBlockPos().offset(-1, 0, -2), (short) 0, 0);

		helper.succeedWhen(() -> helper.assertTrue(target.tank.getFill() == 10_000, "the tank should empty into the barrel, barrel has " + target.tank.getFill()));
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 100)
	public static void explosionRupturesTank(GameTestHelper helper) {
		TileEntityMachineFluidTank tank = tank(helper);
		tank.tank.setFill(100_000);
		BlockPos core = tank.getBlockPos();

		// an explosion strong enough to blow the tank's blocks calls onBlockExploded on each of them
		net.minecraft.world.level.Explosion explosion = new net.minecraft.world.level.Explosion(helper.getLevel(), null, core.getX() + 0.5, core.getY() + 1.5, core.getZ() + 0.5, 8F, false, net.minecraft.world.level.Explosion.BlockInteraction.DESTROY);
		for(BlockPos part : new BlockPos[] {core, core.above(), core.east(2), core.west().above(2)}) {
			ModBlocks.machine_fluidtank.get().onBlockExploded(helper.getLevel().getBlockState(part), helper.getLevel(), part, explosion);
		}

		helper.assertTrue(tank.hasExploded && tank.onFire, "diesel is flammable, the ruptured tank burns");
		helper.assertTrue(helper.getLevel().getBlockState(core).is(ModBlocks.machine_fluidtank.get()), "the first explosion leaves the tank standing");
		helper.succeedWhen(() -> helper.assertTrue(tank.tank.getFill() < 100_000, "the ruptured tank should leak, has " + tank.tank.getFill()));
	}

	@GameTest(template = "empty_8x4x8")
	public static void droppedTankKeepsContents(GameTestHelper helper) {
		TileEntityMachineFluidTank tank = tank(helper);
		tank.tank.setFill(42_000);

		ItemStack drop = new ItemStack(ModBlocks.machine_fluidtank.get());
		drop.applyComponents(tank.collectComponents());
		helper.assertTrue(drop.has(ModDataComponents.PERSISTENT.get()), "a full tank drops with its contents");
		helper.assertTrue(drop.get(ModDataComponents.PERSISTENT.get()).copyTag().getCompound("tank").isEmpty() == false || drop.get(ModDataComponents.PERSISTENT.get()).copyTag().toString().contains("42000"), "the contents are in the item: " + drop.get(ModDataComponents.PERSISTENT.get()));
		helper.succeed();
	}

	@GameTest(template = "empty_8x12x8")
	public static void bat9000FillsThroughSidePort(GameTestHelper helper) {
		BlockPos a = helper.absolutePos(BlockPos.ZERO), b = helper.absolutePos(new BlockPos(7, 0, 7));
		BlockPos core = new BlockPos(Math.min(a.getX(), b.getX()) + 3, a.getY() + 1, Math.min(a.getZ(), b.getZ()) + 3);
		helper.assertTrue(core.equals(ModBlocks.machine_bat9000.get().placeMultiblock(helper.getLevel(), core.north(2), Direction.NORTH)), "the BAT should fit");
		com.hbm.tileentity.machine.storage.TileEntityMachineBAT9000 bat = (com.hbm.tileentity.machine.storage.TileEntityMachineBAT9000) helper.getLevel().getBlockEntity(core);
		bat.tank.setTankType(Fluids.DIESEL);
		helper.assertTrue(bat.tank.getMaxFill() == 2_048_000, "2,048,000mB");

		// three out next to the pillar at x+1
		TileEntityBarrel source = barrel(helper, core.offset(1, 0, 3), (short) 2, 16_000);
		helper.succeedWhen(() -> helper.assertTrue(bat.tank.getFill() == 16_000, "the diesel should go into the BAT, has " + bat.tank.getFill()));
	}

	@GameTest(template = "empty_8x12x8")
	public static void orbusFillsFromBelow(GameTestHelper helper) {
		BlockPos a = helper.absolutePos(BlockPos.ZERO), b = helper.absolutePos(new BlockPos(7, 0, 7));
		BlockPos core = new BlockPos(Math.min(a.getX(), b.getX()) + 3, a.getY() + 1, Math.min(a.getZ(), b.getZ()) + 3);
		helper.assertTrue(core.equals(ModBlocks.machine_orbus.get().placeMultiblock(helper.getLevel(), core.north(), Direction.NORTH)), "the orbus should fit");
		com.hbm.tileentity.machine.storage.TileEntityMachineOrbus orbus = (com.hbm.tileentity.machine.storage.TileEntityMachineOrbus) helper.getLevel().getBlockEntity(core);
		orbus.tank.setTankType(Fluids.DIESEL);

		TileEntityBarrel source = barrel(helper, core.below(), (short) 2, 16_000);
		helper.succeedWhen(() -> helper.assertTrue(orbus.tank.getFill() == 16_000, "the diesel should go into the orbus from below, has " + orbus.tank.getFill()));
	}
}
