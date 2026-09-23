package com.hbm.hazard.type;

import java.util.List;

import com.hbm.config.RadiationConfig;
import com.hbm.hazard.modifier.HazardModifier;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** TODO reacher (tongs) protection once the item exists */
public class HazardTypeHot extends HazardTypeBase {

	@Override
	public void onUpdate(LivingEntity target, float level, ItemStack stack) {

		if(RadiationConfig.disableHot)
			return;

		if(!target.isInWaterRainOrBubble() && level > 0)
			target.igniteForSeconds((int) Math.ceil(level));
	}

	@Override
	public void updateEntity(ItemEntity item, float level) { }

	@Override
	public void addHazardInformation(Player player, List<Component> list, float level, ItemStack stack, List<HazardModifier> modifiers) {

		level = HazardModifier.evalAllModifiers(stack, player, level, modifiers);

		if(level > 0)
			list.add(traitLine("trait.hot", ChatFormatting.GOLD));
	}
}
