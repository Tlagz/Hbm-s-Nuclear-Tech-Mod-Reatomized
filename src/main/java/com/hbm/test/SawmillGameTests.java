package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.projectile.EntitySawblade;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityHeaterFirebox;
import com.hbm.tileentity.machine.TileEntitySawmill;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The Stirling sawmill on a firebox (absolute positions like the heat tests) */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class SawmillGameTests {

	private static BlockPos center(GameTestHelper helper, int y) {
		BlockPos a = helper.absolutePos(BlockPos.ZERO), b = helper.absolutePos(new BlockPos(7, 0, 7));
		return new BlockPos(Math.min(a.getX(), b.getX()) + 3, a.getY() + y, Math.min(a.getZ(), b.getZ()) + 2);
	}

	private static TileEntitySawmill place(GameTestHelper helper, TileEntityHeaterFirebox[] firebox) {
		BlockPos fire = ModBlocks.heater_firebox.get().placeMultiblock(helper.getLevel(), center(helper, 1), Direction.NORTH);
		firebox[0] = (TileEntityHeaterFirebox) helper.getLevel().getBlockEntity(fire);
		BlockPos core = ModBlocks.machine_sawmill.get().placeMultiblock(helper.getLevel(), center(helper, 2), Direction.NORTH);
		helper.assertTrue(core.below().equals(fire), "the sawmill sits on the firebox's core");
		return (TileEntitySawmill) helper.getLevel().getBlockEntity(core);
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 100)
	public static void sawmillCutsLogsIntoPlanks(GameTestHelper helper) {
		TileEntityHeaterFirebox[] firebox = new TileEntityHeaterFirebox[1];
		TileEntitySawmill sawmill = place(helper, firebox);

		helper.assertTrue(sawmill.canPlaceItem(0, new ItemStack(Items.OAK_LOG)), "logs go in");
		helper.assertFalse(sawmill.canPlaceItem(0, new ItemStack(Items.COBBLESTONE)), "cobble doesn't");
		sawmill.setItem(0, new ItemStack(Items.OAK_LOG));

		// about 200 TU/t, 20 progress per tick, 600 per log
		helper.onEachTick(() -> firebox[0].heatEnergy = 2_000);
		helper.succeedWhen(() -> {
			helper.assertTrue(sawmill.getItem(1).is(Items.OAK_PLANKS) && sawmill.getItem(1).getCount() == 6, "one log should become 6 planks, got " + sawmill.getItem(1));
			helper.assertTrue(sawmill.getItem(0).isEmpty(), "the log is used up");
		});
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 500)
	public static void sawmillOverspeedThrowsBlade(GameTestHelper helper) {
		TileEntityHeaterFirebox[] firebox = new TileEntityHeaterFirebox[1];
		TileEntitySawmill sawmill = place(helper, firebox);

		// way above 300 TU/t
		helper.onEachTick(() -> firebox[0].heatEnergy = 10_000);
		helper.succeedWhen(() -> {
			helper.assertFalse(sawmill.hasBlade, "the blade should fly off after 300 ticks of overspeed");
			AABB area = new AABB(sawmill.getBlockPos()).inflate(40);
			helper.assertFalse(helper.getLevel().getEntitiesOfClass(EntitySawblade.class, area).isEmpty(), "there should be a flying sawblade");
		});
	}

	@GameTest(template = "empty_8x4x8")
	public static void sawmillSticksAndSaplings(GameTestHelper helper) {
		TileEntityHeaterFirebox[] firebox = new TileEntityHeaterFirebox[1];
		TileEntitySawmill sawmill = place(helper, firebox);
		helper.assertTrue(sawmill.getOutput(new ItemStack(Items.STICK)).is(ModItems.powder_sawdust.get()), "sticks become sawdust");
		helper.assertTrue(sawmill.getOutput(new ItemStack(Items.OAK_PLANKS)).getCount() == 6, "planks become 6 sticks");
		helper.assertTrue(sawmill.getOutput(new ItemStack(Items.BIRCH_SAPLING)).is(Items.STICK), "saplings become a stick");
		helper.succeed();
	}
}
