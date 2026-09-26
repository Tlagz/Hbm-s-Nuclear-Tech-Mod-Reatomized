package com.hbm.test;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.inventory.recipes.LiquefactionRecipes;
import com.hbm.inventory.recipes.SolidificationRecipes;
import com.hbm.items.ItemEnums.EnumTarType;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.oil.TileEntityMachineLiquefactor;
import com.hbm.tileentity.machine.oil.TileEntityMachineSolidifier;
import com.hbm.util.Tuple.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Liquefactor and solidifier (only the core block, placed directly) */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class LiquefactorGameTests {

	@SuppressWarnings("unchecked")
	private static <T> T place(GameTestHelper helper, Block block) {
		BlockPos core = helper.absolutePos(new BlockPos(3, 1, 3));
		helper.getLevel().setBlockAndUpdate(core, block.defaultBlockState().setValue(BlockDummyable.META, Direction.NORTH.get3DDataValue() + BlockDummyable.offset));
		return (T) helper.getLevel().getBlockEntity(core);
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 100)
	public static void liquefactorMeltsIce(GameTestHelper helper) {
		TileEntityMachineLiquefactor liq = place(helper, ModBlocks.machine_liquefactor.get());
		liq.setPower(TileEntityMachineLiquefactor.maxPower);
		liq.setItem(0, new ItemStack(Items.ICE, 3));

		helper.runAfterDelay(65, () -> {
			// 60 ticks at 250 HE each for 1000mB water per ice
			helper.assertTrue(liq.tank.getTankType() == Fluids.WATER && liq.tank.getFill() == 1_000, "one ice into a bucket of water, got " + liq.tank.getFill());
			helper.assertTrue(liq.getItem(0).getCount() == 2, "one ice used");
			helper.assertTrue(liq.power <= TileEntityMachineLiquefactor.maxPower - 60 * 250, "250 HE per tick");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void liquefactionLookup(GameTestHelper helper) {
		FluidStack coal = LiquefactionRecipes.getOutput(new ItemStack(Items.COAL));
		helper.assertTrue(coal != null && coal.type == Fluids.COALOIL && coal.fill == 250, "coal (tag) into 250mB coal oil");
		FluidStack log = LiquefactionRecipes.getOutput(new ItemStack(Items.SPRUCE_LOG));
		helper.assertTrue(log != null && log.type == Fluids.MUG, "logs into mug root beer");
		FluidStack fish = LiquefactionRecipes.getOutput(new ItemStack(Items.SALMON));
		helper.assertTrue(fish != null && fish.type == Fluids.FISHOIL, "fish into fish oil");
		FluidStack tar = LiquefactionRecipes.getOutput(new ItemStack(ModItems.oil_tar.get(EnumTarType.CRACK).get()));
		helper.assertTrue(tar != null && tar.type == Fluids.BITUMEN && tar.fill == 100, "crack tar into 100mB bitumen");
		// cooked beef: 8 food * 0.8 saturation mod * 2 * 10
		FluidStack beef = LiquefactionRecipes.getOutput(new ItemStack(Items.COOKED_BEEF));
		helper.assertTrue(beef != null && beef.type == Fluids.SALIENT && beef.fill == 128, "unlisted food into salient green, got " + (beef == null ? null : beef.fill));
		helper.assertTrue(LiquefactionRecipes.getOutput(new ItemStack(Items.DIRT)) == null, "dirt does nothing");
		helper.succeed();
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 100)
	public static void solidifierFreezesWater(GameTestHelper helper) {
		TileEntityMachineSolidifier sol = place(helper, ModBlocks.machine_solidifier.get());
		sol.setPower(TileEntityMachineSolidifier.maxPower);
		sol.tank.setTankType(Fluids.WATER);
		sol.tank.setFill(2_500);

		helper.runAfterDelay(65, () -> {
			helper.assertTrue(sol.getItem(0).is(Items.ICE) && sol.getItem(0).getCount() == 1, "a bucket of water into ice");
			helper.assertTrue(sol.tank.getFill() == 1_500, "1000mB used, " + sol.tank.getFill() + " left");
			helper.succeed();
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void solidFuelFromHeatEnergy(GameTestHelper helper) {
		Pair<Integer, ItemStack> diesel = SolidificationRecipes.getOutput(Fluids.DIESEL);
		long heat = Fluids.DIESEL.getTrait(FT_Flammable.class).getHeatEnergy();
		int mB = (int) (1_440_000L * 1000L * 1.25D / heat);
		if(mB > 10_000) mB -= (mB % 1000); else if(mB > 1_000) mB -= (mB % 100); else if(mB > 100) mB -= (mB % 10);
		helper.assertTrue(diesel != null && diesel.getValue().is(ModItems.solid_fuel.get()) && diesel.getKey() == Math.max(mB, 1), "diesel into solid fuel by heat energy, got " + (diesel == null ? null : diesel.getKey()) + " expected " + mB);
		Pair<Integer, ItemStack> oil = SolidificationRecipes.getOutput(Fluids.OIL);
		helper.assertTrue(oil != null && oil.getKey() == 200 && oil.getValue().is(ModItems.oil_tar.get(EnumTarType.CRUDE).get()), "200mB oil into crude tar");
		helper.assertTrue(SolidificationRecipes.getOutput(Fluids.BALEFIRE).getValue().is(ModItems.solid_fuel_bf.get()), "balefire into balefire fuel");
		helper.succeed();
	}
}
