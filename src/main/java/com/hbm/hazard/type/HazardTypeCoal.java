package com.hbm.hazard.type;

import java.util.List;

import com.hbm.config.RadiationConfig;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.hazard.modifier.HazardModifier;
import com.hbm.util.ArmorUtil;
import com.hbm.util.ArmorUtil.HazardClass;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HazardTypeCoal extends HazardTypeBase {

	@Override
	public void onUpdate(LivingEntity target, float level, ItemStack stack) {

		if(RadiationConfig.disableCoal)
			return;

		if(!ArmorUtil.hasAllProtection(target, HazardClass.PARTICLE_COARSE)) {
			HbmLivingProps.incrementBlackLung(target, (int) Math.min(level * stack.getCount(), 10));
		} else {
			if(target.getRandom().nextInt(Math.max(65 - stack.getCount(), 1)) == 0) {
				ArmorUtil.damageGasMaskFilter(target, (int) level);
			}
		}
	}

	@Override
	public void updateEntity(ItemEntity item, float level) { }

	@Override
	public void addHazardInformation(Player player, List<Component> list, float level, ItemStack stack, List<HazardModifier> modifiers) {
		list.add(traitLine("trait.coal", ChatFormatting.DARK_GRAY));
	}
}
