package com.hbm.items.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.items.ISubItems;
import com.hbm.items.ModDataComponents;
import com.hbm.items.ModItems;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.material.NTMMaterial.SmeltingBehavior;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Foundry scraps: solidified material of any kind and amount, what's left when a mold or channel is emptied with a
 * shovel. The original stored the material as the item damage and the amount in NBT, here both are components.
 * Liquid scraps are only display stacks for the molten outputs of recipes.
 */
public class ItemScraps extends Item implements ISubItems {

	public ItemScraps(Properties properties) {
		super(properties);
	}

	/** Every smeltable or additive material, one ingot each, like the original's sub items */
	@Override
	public List<ItemStack> getSubItems() {
		List<ItemStack> list = new ArrayList<>();
		for(NTMMaterial mat : Mats.orderedList) {
			if(mat.smeltable == SmeltingBehavior.SMELTABLE || mat.smeltable == SmeltingBehavior.ADDITIVE) {
				list.add(create(new MaterialStack(mat, MaterialShapes.INGOT.q(1))));
			}
		}
		return list;
	}

	public static boolean isLiquid(ItemStack stack) {
		return stack.getOrDefault(ModDataComponents.SCRAP_LIQUID.get(), false);
	}

	@Override
	public Component getName(ItemStack stack) {

		MaterialStack contents = getMats(stack);
		if(contents != null) {
			String matName = contents.material.getUnlocalizedName();

			if(isLiquid(stack)) {
				return Component.translatable(matName);
			} else {
				return Component.translatable(this.getDescriptionId(stack), Component.translatable(matName));
			}
		}

		return Component.literal("Foundry Scraps");
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		MaterialStack contents = getMats(stack);

		if(contents != null) {

			list.add(Component.literal(Mats.formatAmount(contents.amount, net.neoforged.fml.loading.FMLEnvironment.dist.isClient() && Screen.hasShiftDown())));

			if(isLiquid(stack) && contents.material.smeltable == SmeltingBehavior.ADDITIVE) {
				list.add(Component.literal("Additive, not castable!").withStyle(ChatFormatting.DARK_RED));
			}
		}
	}

	public static MaterialStack getMats(ItemStack stack) {

		if(!stack.is(ModItems.scraps.get())) return null;

		NTMMaterial mat = Mats.matById.get(stack.getOrDefault(ModDataComponents.SCRAP_MATERIAL.get(), -1));
		if(mat == null) return null;

		int amount = stack.getOrDefault(ModDataComponents.SCRAP_AMOUNT.get(), MaterialShapes.INGOT.q(1));
		return new MaterialStack(mat, amount);
	}

	public static ItemStack create(MaterialStack stack) {
		return create(stack, false);
	}

	public static ItemStack create(MaterialStack stack, boolean liquid) {
		if(stack.material == null) return ItemStack.EMPTY;
		ItemStack scrap = new ItemStack(ModItems.scraps.get());
		scrap.set(ModDataComponents.SCRAP_MATERIAL.get(), stack.material.id);
		scrap.set(ModDataComponents.SCRAP_AMOUNT.get(), stack.amount);
		if(liquid) scrap.set(ModDataComponents.SCRAP_LIQUID.get(), true);
		return scrap;
	}

	/** Tint: the material's solid color, or its molten color for liquid scraps */
	public static int getColor(ItemStack stack) {
		MaterialStack contents = getMats(stack);
		if(contents == null) return 0xFFFFFF;
		return isLiquid(stack) ? contents.material.moltenColor : contents.material.solidColorLight;
	}

	/** Model override: 0 solid scraps, 1 liquid, 2 liquid additive */
	public static float getModelType(ItemStack stack) {
		if(!isLiquid(stack)) return 0;
		MaterialStack contents = getMats(stack);
		return contents != null && contents.material.smeltable == SmeltingBehavior.ADDITIVE ? 2 : 1;
	}
}
