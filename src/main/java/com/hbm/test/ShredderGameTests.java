package com.hbm.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.ShredderRecipes;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineShredder;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The shredder: fixed and tag generated recipes, blade wear */
@GameTestHolder(RefStrings.MODID)
@PrefixGameTestTemplate(false)
public class ShredderGameTests {

	@GameTest(template = "empty_8x4x8")
	public static void shredderRecipesFromTags(GameTestHelper helper) {
		helper.assertTrue(ShredderRecipes.tagRecipes.size() > 50, "the tag recipes should be generated, " + ShredderRecipes.tagRecipes.size());

		ItemStack iron = ShredderRecipes.getShredderResult(new ItemStack(Items.IRON_ORE));
		helper.assertTrue(iron.is(ModItems.powder_iron.get()) && iron.getCount() == 2, "vanilla iron ore should shred into 2 iron powder, got " + iron);
		ItemStack uranium = ShredderRecipes.getShredderResult(new ItemStack(ModBlocks.ore_uranium.get()));
		helper.assertTrue(uranium.is(ModItems.powder_uranium.get()) && uranium.getCount() == 2, "uranium ore should shred into 2 uranium powder, got " + uranium);
		ItemStack ingot = ShredderRecipes.getShredderResult(new ItemStack(ModItems.ingot_steel.get()));
		helper.assertTrue(ingot.is(ModItems.powder_steel.get()) && ingot.getCount() == 1, "an ingot gives one powder, got " + ingot);
		ItemStack gravel = ShredderRecipes.getShredderResult(new ItemStack(Items.COBBLESTONE));
		helper.assertTrue(gravel.is(Items.GRAVEL), "the fixed recipes turn cobblestone into gravel, got " + gravel);
		ItemStack scrap = ShredderRecipes.getShredderResult(new ItemStack(Items.BOOK));
		helper.assertTrue(scrap.is(ModItems.scrap.get()), "anything else becomes scrap, got " + scrap);
		helper.succeed();
	}

	@GameTest(template = "empty_8x4x8", timeoutTicks = 200)
	public static void shredderDoublesOreAndWearsBlades(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		helper.setBlock(pos, ModBlocks.machine_shredder.get());
		TileEntityMachineShredder shredder = (TileEntityMachineShredder) helper.getBlockEntity(pos);

		shredder.setItem(29, new ItemStack(ModItems.battery_creative.get()));
		shredder.setItem(0, new ItemStack(Items.IRON_ORE, 2));
		helper.assertFalse(shredder.canProcess(), "no blades, no shredding");

		shredder.setItem(27, new ItemStack(ModItems.blades_steel.get()));
		shredder.setItem(28, new ItemStack(ModItems.blades_steel.get()));

		helper.succeedWhen(() -> {
			int powder = 0;
			for(int i = 9; i < 27; i++) if(shredder.getItem(i).is(ModItems.powder_iron.get())) powder += shredder.getItem(i).getCount();
			helper.assertTrue(powder == 4, "2 iron ore should become 4 iron powder, got " + powder);
			helper.assertTrue(shredder.getItem(27).getDamageValue() == 2 && shredder.getItem(28).getDamageValue() == 2, "every cycle wears both blades");
		});
	}
}
