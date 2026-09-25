package com.hbm.items.tool;

import com.hbm.blocks.IToolable;
import com.hbm.blocks.IToolable.ToolType;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

/**
 * Screwdrivers and hand drills: used on IToolable blocks, and as crafting tools that stay in the crafting grid with one
 * durability less (the original's ItemCraftingDegradation). A durability of 0 means unbreakable.
 */
public class ItemTooling extends Item {

	public final ToolType type;

	public ItemTooling(Properties properties, ToolType type, int durability) {
		super(durability > 0 ? properties.durability(durability) : properties.stacksTo(1));
		this.type = type;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Block b = context.getLevel().getBlockState(context.getClickedPos()).getBlock();
		if(b instanceof IToolable toolable) {
			var hit = context.getClickLocation().subtract(context.getClickedPos().getX(), context.getClickedPos().getY(), context.getClickedPos().getZ());
			if(toolable.onScrew(context.getLevel(), context.getPlayer(), context.getClickedPos(), context.getClickedFace(), (float) hit.x, (float) hit.y, (float) hit.z, this.type)) {
				if(context.getPlayer() != null && !context.getLevel().isClientSide) {
					context.getItemInHand().hurtAndBreak(1, context.getPlayer(), EquipmentSlot.MAINHAND);
				}
				return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
			}
		}
		return InteractionResult.PASS;
	}

	@Override
	public boolean hasCraftingRemainingItem(ItemStack stack) {
		return true;
	}

	/** The tool stays in the grid, worn down by one use, until it breaks */
	@Override
	public ItemStack getCraftingRemainingItem(ItemStack stack) {
		if(!stack.isDamageableItem()) return stack.copyWithCount(1);
		ItemStack copy = stack.copyWithCount(1);
		copy.setDamageValue(copy.getDamageValue() + 1);
		return copy.getDamageValue() >= copy.getMaxDamage() ? ItemStack.EMPTY : copy;
	}
}
