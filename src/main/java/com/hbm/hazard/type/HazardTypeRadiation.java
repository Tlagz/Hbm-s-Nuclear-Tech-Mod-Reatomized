package com.hbm.hazard.type;

import java.util.List;

import com.hbm.hazard.modifier.HazardModifier;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** TODO reacher (tongs) reduction once the item exists */
public class HazardTypeRadiation extends HazardTypeBase {

	@Override
	public void onUpdate(LivingEntity target, float level, ItemStack stack) {

		level *= stack.getCount();

		if(level > 0) {
			float rad = level / 20F;
			ContaminationUtil.contaminate(target, HazardType.RADIATION, ContaminationType.CREATIVE, rad);
		}
	}

	@Override
	public void updateEntity(ItemEntity item, float level) { }

	@Override
	public void addHazardInformation(Player player, List<Component> list, float level, ItemStack stack, List<HazardModifier> modifiers) {

		level = HazardModifier.evalAllModifiers(stack, player, level, modifiers);

		if(level < 1e-5)
			return;

		list.add(traitLine("trait.radioactive", ChatFormatting.GREEN));
		String rad = "" + (Math.floor(level * 1000) / 1000);
		list.add(Component.literal(rad + "RAD/s").withStyle(ChatFormatting.YELLOW));

		if(stack.getCount() > 1) {
			list.add(Component.literal("Stack: " + ((Math.floor(level * 1000 * stack.getCount()) / 1000) + "RAD/s")).withStyle(ChatFormatting.YELLOW));
		}
	}
}
