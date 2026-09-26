package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityCrucible;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The crucible melting items and alloying them */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class CrucibleGameTests {

	@GameTest(template = "empty_8x4x8", timeoutTicks = 600)
	public static void crucibleMakesSteel(GameTestHelper helper) {
		BlockPos a = helper.absolutePos(BlockPos.ZERO), b = helper.absolutePos(new BlockPos(7, 0, 7));
		BlockPos placed = new BlockPos(Math.min(a.getX(), b.getX()) + 4, a.getY() + 1, Math.min(a.getZ(), b.getZ()) + 3);
		BlockPos core = ModBlocks.machine_crucible.get().placeMultiblock(helper.getLevel(), placed, Direction.NORTH);
		helper.assertTrue(core != null, "the crucible should fit");
		TileEntityCrucible crucible = (TileEntityCrucible) helper.getLevel().getBlockEntity(core);

		// items fall into the crucible: the core is only the half block floor
		double top = helper.getLevel().getBlockState(core).getCollisionShape(helper.getLevel(), core, CollisionContext.empty()).max(Direction.Axis.Y);
		helper.assertTrue(top == 0.5D, "the crucible's floor is half a block high, not " + top);

		crucible.recipe = "crucible.steel";
		crucible.setItem(1, new ItemStack(Items.IRON_INGOT));
		for(int i = 2; i <= 4; i++) crucible.setItem(i, new ItemStack(Items.CHARCOAL));
		crucible.setItem(5, new ItemStack(ModItems.powder_flux.get()));

		// no heater in the test, the heat is kept up by hand
		helper.onEachTick(() -> crucible.heat = TileEntityCrucible.maxHeat);

		helper.succeedWhen(() -> {
			int steel = TileEntityCrucible.getQuantaFromType(crucible.recipeStack, Mats.MAT_STEEL);
			helper.assertTrue(steel >= 3 * 16, "iron, charcoal and flux should alloy into steel, has " + steel);
		});
	}
}
