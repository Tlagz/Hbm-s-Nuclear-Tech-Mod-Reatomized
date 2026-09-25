package com.hbm.test;

import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.recipes.ArcFurnaceRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemArcElectrode;
import com.hbm.items.machine.ItemArcElectrode.EnumElectrodeType;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineArcFurnaceLarge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The material lookup (MatDistribution, ore dictionary shapes) and the electric arc furnace */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class ArcFurnaceGameTests {

	private static int amountOf(List<MaterialStack> stacks, com.hbm.inventory.material.NTMMaterial material) {
		return stacks.stream().filter(s -> s.material == material).mapToInt(s -> s.amount).sum();
	}

	@GameTest(template = "empty_8x4x8")
	public static void materialLookup(GameTestHelper helper) {
		List<MaterialStack> ingot = Mats.getMaterialsFromItem(new ItemStack(ModItems.ingot_steel.get()));
		helper.assertTrue(amountOf(ingot, Mats.MAT_STEEL) == MaterialShapes.INGOT.q(1), "a steel ingot is one ingot of steel, got " + ingot.size() + " stacks");
		List<MaterialStack> ore = Mats.getMaterialsFromItem(new ItemStack(Items.IRON_ORE));
		helper.assertTrue(amountOf(ore, Mats.MAT_IRON) == MaterialShapes.INGOT.q(2) && amountOf(ore, Mats.MAT_TITANIUM) == MaterialShapes.NUGGET.q(3), "iron ore holds two ingots of iron and some titanium");
		List<MaterialStack> blades = Mats.getMaterialsFromItem(new ItemStack(ModItems.blades_steel.get()));
		helper.assertTrue(amountOf(blades, Mats.MAT_STEEL) == MaterialShapes.INGOT.q(4), "steel blades are made of 4 steel ingots (MatDistribution)");
		helper.assertTrue(Mats.getMaterialsFromItem(new ItemStack(Items.STICK)).isEmpty(), "sticks aren't made of any material");

		var solid = ArcFurnaceRecipes.getOutput(new ItemStack(Items.SAND), false, helper.getLevel());
		helper.assertTrue(solid != null && solid.solidOutput.is(ModItems.nugget_silicon.get()), "sand melts into silicon");
		var liquid = ArcFurnaceRecipes.getOutput(new ItemStack(ModItems.ingot_steel.get()), true, helper.getLevel());
		helper.assertTrue(liquid != null && liquid.fluidOutput[0].material == Mats.MAT_STEEL, "steel ingots melt into molten steel (generated from the ingot tag)");
		var furnace = ArcFurnaceRecipes.getOutput(new ItemStack(Items.IRON_ORE), false, helper.getLevel());
		helper.assertTrue(furnace != null && furnace.solidOutput.is(Items.IRON_INGOT), "ores smelt like in a furnace (generated from the smelting recipes)");
		helper.succeed();
	}

	@GameTest(template = "empty_8x12x8", timeoutTicks = 900)
	public static void arcFurnaceMakesSilicon(GameTestHelper helper) {
		// absolute coordinates: the core ends up 2 north of the placed block, 5x5 around it plus the back section one
		// more block north (the spout points south, towards the placing player)
		BlockPos a = helper.absolutePos(BlockPos.ZERO), b = helper.absolutePos(new BlockPos(7, 0, 7));
		BlockPos placed = new BlockPos(Math.min(a.getX(), b.getX()) + 3, a.getY() + 1, Math.min(a.getZ(), b.getZ()) + 5);
		BlockPos core = ModBlocks.machine_arc_furnace.get().placeMultiblock(helper.getLevel(), placed, Direction.SOUTH);
		helper.assertTrue(core != null, "the arc furnace should fit");
		TileEntityMachineArcFurnaceLarge arc = (TileEntityMachineArcFurnaceLarge) helper.getLevel().getBlockEntity(core);
		arc.setItem(3, new ItemStack(ModItems.battery_creative.get()));
		for(int i = 0; i < 3; i++) arc.setItem(i, ModItems.arc_electrode.stack(EnumElectrodeType.GRAPHITE));
		arc.setItem(25, new ItemStack(Items.SAND, 3));

		// the lid opens, loads one sand per grid slot, closes and melts them
		helper.succeedWhen(() -> {
			int silicon = 0;
			for(int i = 5; i < 25; i++) if(arc.getItem(i).is(ModItems.nugget_silicon.get())) silicon += arc.getItem(i).getCount();
			helper.assertTrue(silicon == 3, "3 sand should melt into 3 silicon nuggets, got " + silicon);
			helper.assertTrue(ItemArcElectrode.getDurability(arc.getItem(0)) == 1, "every batch wears the electrodes down");
		});
	}
}
