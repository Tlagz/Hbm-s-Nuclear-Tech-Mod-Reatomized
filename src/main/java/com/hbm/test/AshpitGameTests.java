package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ItemEnums.EnumAshType;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityAshpit;
import com.hbm.tileentity.machine.TileEntityFireboxBase;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The ashpit under a firebox collects ash from the burnt fuel and turns it into ash items */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class AshpitGameTests {

	@GameTest(template = "empty_8x4x8", timeoutTicks = 40)
	public static void ashpitCollectsFireboxAsh(GameTestHelper helper) {
		// the ashpit's core is one block south of where it's placed
		BlockPos ashCore = helper.absolutePos(new BlockPos(4, 1, 4));
		BlockPos placed = ModBlocks.machine_ashpit.get().placeMultiblock(helper.getLevel(), ashCore.north(), Direction.NORTH);
		helper.assertTrue(ashCore.equals(placed), "the ashpit core should be at " + ashCore + ", is " + placed);
		TileEntityAshpit ashpit = (TileEntityAshpit) helper.getLevel().getBlockEntity(ashCore);

		// the firebox's core right on top
		BlockPos fireCore = null;
		for(BlockPos candidate : new BlockPos[] {ashCore.above(), ashCore.above().north(), ashCore.above().south()}) {
			if(fireCore != null) break;
			BlockPos core = ModBlocks.heater_firebox.get().placeMultiblock(helper.getLevel(), candidate, Direction.NORTH);
			if(core != null && !core.equals(ashCore.above())) {
				helper.getLevel().destroyBlock(core, false);
				continue;
			}
			fireCore = core;
		}
		helper.assertTrue(fireCore != null, "the firebox should sit right on the ashpit");
		TileEntityFireboxBase firebox = (TileEntityFireboxBase) helper.getLevel().getBlockEntity(fireCore);
		firebox.setItem(0, new ItemStack(Items.COAL, 2));

		// one coal burns for 2000 ticks, that's exactly one ash item's worth
		helper.runAfterDelay(5, () -> {
			helper.assertTrue(ashpit.getItem(0).is(ModItems.powder_ash.get(EnumAshType.COAL).get()) && ashpit.getItem(0).getCount() == 1, "burning one coal should make a coal ash item, got " + ashpit.getItem(0));
			helper.assertTrue(ashpit.ashLevelCoal == 0 && ashpit.isFull, "the ash level goes down by the threshold, the pit shows ash inside");

			ashpit.addAsh(EnumAshType.WOOD, 1_000);
		});
		helper.runAfterDelay(8, () -> {
			helper.assertTrue(ashpit.ashLevelWood == 1_000 && ashpit.getItem(1).isEmpty(), "half a threshold of wood ash doesn't make an item yet");
			helper.succeed();
		});
	}
}
