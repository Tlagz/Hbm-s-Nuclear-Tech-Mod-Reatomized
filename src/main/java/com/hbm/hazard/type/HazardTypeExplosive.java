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
import net.minecraft.world.level.Level;

public class HazardTypeExplosive extends HazardTypeBase {

	@Override
	public void onUpdate(LivingEntity target, float level, ItemStack stack) {

		if(RadiationConfig.disableExplosive)
			return;

		if(!target.level().isClientSide && target.isOnFire() && stack.getCount() > 0) {
			stack.setCount(0);
			target.level().explode(null, target.getX(), target.getEyeY(), target.getZ(), level, false, Level.ExplosionInteraction.TNT);
		}
	}

	@Override
	public void updateEntity(ItemEntity item, float level) {

		if(RadiationConfig.disableExplosive)
			return;

		if(item.isOnFire()) {
			item.discard();
			item.level().explode(null, item.getX(), item.getY() + item.getBbHeight() * 0.5, item.getZ(), level, false, Level.ExplosionInteraction.TNT);
		}
	}

	@Override
	public void addHazardInformation(Player player, List<Component> list, float level, ItemStack stack, List<HazardModifier> modifiers) {
		list.add(traitLine("trait.explosive", ChatFormatting.RED));
	}
}
