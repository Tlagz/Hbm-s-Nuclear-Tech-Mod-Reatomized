package com.hbm.blocks.machine;

import java.util.List;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.tileentity.ModTileEntities;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Steam turbine, a plain block with the turbine texture */
public class MachineTurbine extends BlockMachineTile implements ITooltipProvider {

	public MachineTurbine(Properties properties) {
		super(properties, ModTileEntities.TURBINE);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		this.addStandardInfo(list);
	}
}
