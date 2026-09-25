package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.FoundryOutlet;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.recipes.anvil.AnvilRecipes;
import com.hbm.inventory.recipes.anvil.AnvilSmithingMold;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMold;
import com.hbm.items.machine.ItemScraps;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityFoundryChannel;
import com.hbm.tileentity.machine.TileEntityFoundryMold;
import com.hbm.util.CrucibleUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Foundry: molds, scraps, casting, channels and outlets (absolute positions, see DecoGameTests) */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class FoundryGameTests {

	private static final int INGOT_MOLD = 2;

	@GameTest(template = "empty_8x4x8")
	public static void moldsAndScraps(GameTestHelper helper) {
		ItemMold.Mold ingot = ItemMold.getMold(ItemMold.stack(INGOT_MOLD));
		helper.assertTrue(ingot != null && ingot.size == 0 && ingot.getCost() == MaterialShapes.INGOT.q(1), "the ingot mold is a small mold holding one ingot");
		ItemStack steel = ingot.getOutput(Mats.MAT_STEEL);
		helper.assertTrue(steel != null && steel.is(ModItems.ingot_steel.get()), "steel cast in the ingot mold is a steel ingot, got " + steel);
		helper.assertTrue(ItemMold.getMold(ItemMold.stack(7)).getOutput(Mats.MAT_IRON).is(ModItems.stamp_iron_flat.get()), "the stamp mold casts iron stamps");

		ItemStack scrap = ItemScraps.create(new MaterialStack(Mats.MAT_COPPER, 40));
		MaterialStack back = ItemScraps.getMats(scrap);
		helper.assertTrue(back != null && back.material == Mats.MAT_COPPER && back.amount == 40, "scraps keep their material and amount");
		helper.assertTrue(Mats.getMaterialsFromItem(scrap).get(0).amount == 40, "scraps are made of their contents");

		// any ingot and a blank mold make an ingot mold, only the blank is used up
		AnvilSmithingMold recipe = null;
		for(var r : AnvilRecipes.smithingRecipes) if(r instanceof AnvilSmithingMold mold && mold.getSimpleOutput().is(ItemMold.get(INGOT_MOLD).get())) recipe = mold;
		helper.assertTrue(recipe != null, "the ingot mold has an anvil recipe");
		helper.assertTrue(recipe.matches(new ItemStack(Items.IRON_INGOT), new ItemStack(ModItems.mold_base.get())), "an iron ingot works as the template");
		helper.assertTrue(recipe.matches(new ItemStack(ModItems.ingot_steel.get()), new ItemStack(ModItems.mold_base.get())), "a steel ingot works as the template");
		helper.assertFalse(recipe.matches(new ItemStack(ModItems.plate_steel.get()), new ItemStack(ModItems.mold_base.get())), "plates don't make ingot molds");
		helper.assertTrue(recipe.amountConsumed(0, false) == 0 && recipe.amountConsumed(1, false) == 1, "the template stays");
		helper.succeed();
	}

	private static TileEntityFoundryMold placeMold(GameTestHelper helper, BlockPos pos) {
		helper.getLevel().setBlockAndUpdate(pos, ModBlocks.foundry_mold.get().defaultBlockState());
		TileEntityFoundryMold mold = (TileEntityFoundryMold) helper.getLevel().getBlockEntity(pos);
		mold.slots.set(0, ItemMold.stack(INGOT_MOLD));
		return mold;
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 400)
	public static void castingInMold(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 3));
		TileEntityFoundryMold mold = placeMold(helper, pos);

		// poured from above like the arc furnace's spout, the rest stays in the stack
		MaterialStack steel = new MaterialStack(Mats.MAT_STEEL, MaterialShapes.INGOT.q(3));
		MaterialStack left = CrucibleUtil.pourSingleStack(helper.getLevel(), pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5, 4, true, steel, MaterialShapes.INGOT.q(1), null);
		helper.assertTrue(mold.type == Mats.MAT_STEEL && mold.amount == MaterialShapes.INGOT.q(1), "the mold takes one ingot's worth of steel, has " + mold.amount);
		helper.assertTrue(left.amount == MaterialShapes.INGOT.q(2), "the rest is left over, " + left.amount);

		helper.succeedWhen(() -> helper.assertTrue(mold.slots.get(1).is(ModItems.ingot_steel.get()) && mold.amount == 0, "the steel should cool off into an ingot"));
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 400)
	public static void channelFeedsMoldAndOutlet(GameTestHelper helper) {
		BlockPos channelPos = helper.absolutePos(new BlockPos(3, 2, 3));
		BlockPos moldPos = channelPos.north();
		BlockPos outletPos = channelPos.east();
		BlockPos lowerMoldPos = outletPos.below();

		helper.getLevel().setBlockAndUpdate(channelPos, ModBlocks.foundry_channel.get().defaultBlockState());
		TileEntityFoundryMold mold = placeMold(helper, moldPos);
		TileEntityFoundryMold lower = placeMold(helper, lowerMoldPos);
		helper.getLevel().setBlockAndUpdate(outletPos, ModBlocks.foundry_outlet.get().defaultBlockState().setValue(FoundryOutlet.FACING, Direction.EAST));

		var channelState = helper.getLevel().getBlockState(channelPos);
		helper.assertTrue(channelState.getValue(com.hbm.blocks.machine.FoundryChannel.NORTH) && channelState.getValue(com.hbm.blocks.machine.FoundryChannel.EAST), "the channel connects to the mold and the outlet facing away");

		// two ingots of copper in the channel: one goes into the mold next to it, one through the outlet into the mold below
		TileEntityFoundryChannel channel = (TileEntityFoundryChannel) helper.getLevel().getBlockEntity(channelPos);
		channel.type = Mats.MAT_COPPER;
		channel.amount = MaterialShapes.INGOT.q(2);

		helper.succeedWhen(() -> {
			helper.assertTrue(mold.slots.get(1).is(ModItems.ingot_copper.get()), "the mold next to the channel should cast a copper ingot, has " + mold.amount);
			helper.assertTrue(lower.slots.get(1).is(ModItems.ingot_copper.get()), "the mold below the outlet should cast a copper ingot, has " + lower.amount);
		});
	}
}
