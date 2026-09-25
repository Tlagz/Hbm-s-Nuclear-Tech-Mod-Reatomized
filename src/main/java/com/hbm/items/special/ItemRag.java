package com.hbm.items.special;

import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** Cloth rag: turns damp when dropped into water, right-click turns one into a piss rag */
public class ItemRag extends Item {

	public ItemRag(Properties properties) {
		super(properties);
	}

	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entityItem) {

		if(!entityItem.level().isClientSide) {

			if(entityItem.level().getFluidState(BlockPos.containing(entityItem.getX(), entityItem.getY(), entityItem.getZ())).is(FluidTags.WATER)) {
				entityItem.setItem(new ItemStack(ModItems.rag_damp.get(), stack.getCount()));
				return true;
			}
		}
		return false;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		stack.shrink(1);
		player.getInventory().add(new ItemStack(ModItems.rag_piss.get()));
		return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		for(String line : I18nUtil.resolveKeyArray("item.rag.desc")) {
			list.add(Component.literal(line));
		}
	}
}
