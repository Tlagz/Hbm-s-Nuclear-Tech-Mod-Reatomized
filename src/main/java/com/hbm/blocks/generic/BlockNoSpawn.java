package com.hbm.blocks.generic;

import java.util.List;

import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

/** Blocks mobs can't spawn on (concrete), with a tooltip saying so */
public class BlockNoSpawn extends Block {

	public BlockNoSpawn(Properties properties) {
		super(properties.isValidSpawn((state, level, pos, type) -> false));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		list.add(Component.literal(I18nUtil.resolveKey("tile.nospawn")).withStyle(ChatFormatting.RED));
	}
}
