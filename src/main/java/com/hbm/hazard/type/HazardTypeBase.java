package com.hbm.hazard.type;

import java.util.List;

import com.hbm.hazard.modifier.HazardModifier;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class HazardTypeBase {

	/**
	 * Does the thing. Called by HazardEntry.applyHazard
	 * @param target the holder
	 * @param level the final level after calculating all the modifiers
	 * @param stack the stack that is being updated
	 */
	public abstract void onUpdate(LivingEntity target, float level, ItemStack stack);

	/** Updates the hazard for dropped items. Used for things like explosive and hydroactive items. */
	public abstract void updateEntity(ItemEntity item, float level);

	/**
	 * Adds item tooltip info. Client only.
	 * @param level the base level, mods are passed separately
	 */
	public abstract void addHazardInformation(Player player, List<Component> list, float level, ItemStack stack, List<HazardModifier> modifiers);

	/** "[Trait name]" tooltip line */
	protected static Component traitLine(String key, ChatFormatting color) {
		return Component.literal("[").append(Component.translatable(key)).append("]").withStyle(color);
	}
}
