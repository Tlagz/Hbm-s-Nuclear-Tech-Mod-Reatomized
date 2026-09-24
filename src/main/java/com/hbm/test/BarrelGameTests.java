package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.network.FluidDuctStandard;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModDataComponents;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineWoodBurner;
import com.hbm.tileentity.machine.storage.TileEntityBarrel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Barrels and the fluid network: fluid actually moving through pipes */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class BarrelGameTests {

	private static TileEntityBarrel barrel(GameTestHelper helper, BlockPos pos, short mode, int fill) {
		helper.getLevel().setBlockAndUpdate(pos, ModBlocks.barrel_steel.get().defaultBlockState());
		TileEntityBarrel barrel = (TileEntityBarrel) helper.getLevel().getBlockEntity(pos);
		barrel.tank.setTankType(Fluids.DIESEL);
		barrel.tank.setFill(fill);
		barrel.mode = mode;
		return barrel;
	}

	private static void pipeLine(GameTestHelper helper, BlockPos from, Direction dir, int length) {
		for(int i = 0; i < length; i++) helper.getLevel().setBlockAndUpdate(from.relative(dir, i), ModBlocks.fluid_duct_neo.get().defaultBlockState());
		for(int i = 0; i < length; i++) FluidDuctStandard.setType(helper.getLevel(), from.relative(dir, i), Fluids.DIESEL);
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 200)
	public static void fluidFlowsBetweenBarrels(GameTestHelper helper) {
		BlockPos start = helper.absolutePos(new BlockPos(1, 1, 3));
		TileEntityBarrel source = barrel(helper, start, (short) 2, 8000);
		pipeLine(helper, start.east(), Direction.EAST, 4);
		TileEntityBarrel target = barrel(helper, start.east(5), (short) 0, 0);

		helper.succeedWhen(() -> {
			helper.assertTrue(target.tank.getFill() == 8000, "all diesel should arrive, target: " + target.tank.getFill() + ", source: " + source.tank.getFill());
			helper.assertTrue(source.tank.getFill() == 0, "source should be empty, has " + source.tank.getFill());
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 200)
	public static void disabledBarrelDoesNotReceive(GameTestHelper helper) {
		BlockPos start = helper.absolutePos(new BlockPos(1, 1, 3));
		TileEntityBarrel source = barrel(helper, start, (short) 2, 8000);
		pipeLine(helper, start.east(), Direction.EAST, 2);
		TileEntityBarrel target = barrel(helper, start.east(3), (short) 3, 0);

		helper.runAtTickTime(100, () -> {
			helper.assertTrue(target.tank.getFill() == 0 && source.tank.getFill() == 8000, "a disabled barrel must not receive, target: " + target.tank.getFill());
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 300)
	public static void barrelFeedsWoodBurnerThroughPipe(GameTestHelper helper) {
		// wood burner facing north, fluid port 2 blocks behind the core
		BlockPos core = helper.absolutePos(new BlockPos(3, 1, 1));
		ModBlocks.machine_wood_burner.get().placeMultiblock(helper.getLevel(), core, Direction.NORTH);
		TileEntityMachineWoodBurner burner = (TileEntityMachineWoodBurner) helper.getLevel().getBlockEntity(core);
		burner.tank.setTankType(Fluids.DIESEL);
		burner.liquidBurn = true;
		burner.isOn = true;

		pipeLine(helper, core.south(2), Direction.SOUTH, 2);
		TileEntityBarrel source = barrel(helper, core.south(4), (short) 2, 16000);

		helper.succeedWhen(() -> {
			helper.assertTrue(source.tank.getFill() < 16000, "barrel should supply the burner, has " + source.tank.getFill());
			helper.assertTrue(burner.power > 0, "burner should make power from piped diesel");
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void barrelKeepsFluidAsItem(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
		TileEntityBarrel barrel = barrel(helper, pos, (short) 1, 5000);

		// what the loot table copies to the drop
		ItemStack drop = new ItemStack(ModBlocks.barrel_steel.get());
		drop.applyComponents(barrel.collectComponents());
		helper.assertTrue(drop.has(ModDataComponents.PERSISTENT.get()), "dropped barrel should carry its fluid");

		// placing it again restores tank and mode
		BlockPos other = pos.east(2);
		helper.getLevel().setBlockAndUpdate(other, ModBlocks.barrel_steel.get().defaultBlockState());
		TileEntityBarrel placed = (TileEntityBarrel) helper.getLevel().getBlockEntity(other);
		placed.applyComponentsFromItemStack(drop);
		helper.assertTrue(placed.tank.getTankType() == Fluids.DIESEL && placed.tank.getFill() == 5000 && placed.mode == 1,
				"placed barrel should have 5000mB diesel in buffer mode, has " + placed.tank.getFill() + " " + placed.tank.getTankType().getName() + " mode " + placed.mode);
		helper.succeed();
	}
}
