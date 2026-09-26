package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.CentrifugeRecipes;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineCentrifuge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Centrifuge recipes and processing (absolute positions) */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class CentrifugeGameTests {

	@GameTest(template = "empty_8x4x8")
	public static void centrifugeRecipesFromTags(GameTestHelper helper) {
		// iron ore through its ore tag, a crystal as a plain item
		ItemStack[] iron = CentrifugeRecipes.getOutput(new ItemStack(Items.DEEPSLATE_IRON_ORE));
		helper.assertTrue(iron != null && iron.length == 4 && iron[0].is(ModItems.powder_iron.get()) && iron[3].is(Items.GRAVEL), "deepslate iron ore should centrifuge through the iron ore tag");
		ItemStack[] coal = CentrifugeRecipes.getOutput(new ItemStack(ModItems.crystal_coal.get()));
		helper.assertTrue(coal != null && coal[0].is(ModItems.powder_coal.get()) && coal[0].getCount() == 3, "coal crystals give 3 coal powder per slot");
		helper.assertTrue(CentrifugeRecipes.getOutput(new ItemStack(Items.STICK)) == null, "no recipe for sticks");
		helper.succeed();
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 300)
	public static void centrifugeProcessesOre(GameTestHelper helper) {
		BlockPos a = helper.absolutePos(BlockPos.ZERO), b = helper.absolutePos(new BlockPos(7, 0, 7));
		BlockPos pos = new BlockPos(Math.min(a.getX(), b.getX()) + 3, a.getY() + 1, Math.min(a.getZ(), b.getZ()) + 3);
		BlockPos core = ModBlocks.machine_centrifuge.get().placeMultiblock(helper.getLevel(), pos, Direction.NORTH);
		TileEntityMachineCentrifuge centrifuge = (TileEntityMachineCentrifuge) helper.getLevel().getBlockEntity(core);

		centrifuge.setItem(0, new ItemStack(Items.IRON_ORE, 2));
		helper.onEachTick(() -> centrifuge.setPower(TileEntityMachineCentrifuge.maxPower));

		helper.succeedWhen(() -> {
			// 200 ticks per item without upgrades
			helper.assertTrue(centrifuge.getItem(0).getCount() == 1, "one ore should be done, left: " + centrifuge.getItem(0));
			for(int i = 2; i <= 4; i++) helper.assertTrue(centrifuge.getItem(i).is(ModItems.powder_iron.get()), "iron powder in slot " + i);
			helper.assertTrue(centrifuge.getItem(5).is(Items.GRAVEL), "gravel in the last slot");
		});
	}
}
