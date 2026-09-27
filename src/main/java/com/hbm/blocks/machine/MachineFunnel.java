package com.hbm.blocks.machine;

import java.util.List;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.tileentity.ModTileEntities;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Combinator funnel, drawn with the original's funnel.obj as a block model */
public class MachineFunnel extends BlockMachineTile implements ITooltipProvider {

	public MachineFunnel(Properties properties) {
		super(properties.noOcclusion(), ModTileEntities.FUNNEL);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		this.addStandardInfo(list);
	}
}
