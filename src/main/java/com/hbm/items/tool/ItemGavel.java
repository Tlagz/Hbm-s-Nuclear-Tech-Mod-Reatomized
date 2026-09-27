package com.hbm.items.tool;

import java.util.List;

import com.hbm.main.ModSounds;
import com.hbm.potion.HbmPotion;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

/**
 * The gavels, the original's WeaponSpecial cases: wood just thunks, lead poisons, diamond takes a third of the
 * target's max health on top of the hit.
 */
public class ItemGavel extends SwordItem {

	public enum GavelType { WOOD, LEAD, DIAMOND }

	/** The original's tMatSteel: harvest level 3, 750 uses, speed 8, damage 2, enchantability 10 */
	public static final Tier STEEL = new SimpleTier(BlockTags.INCORRECT_FOR_IRON_TOOL, 750, 8.0F, 2.0F, 10, () -> Ingredient.EMPTY);

	private final GavelType type;

	public ItemGavel(Properties properties, GavelType type) {
		super(tierFor(type), properties.stacksTo(1).attributes(SwordItem.createAttributes(tierFor(type), 3, -2.4F)));
		this.type = type;
	}

	private static Tier tierFor(GavelType type) {
		return switch(type) {
		case WOOD -> Tiers.WOOD;
		case LEAD -> STEEL;
		case DIAMOND -> Tiers.DIAMOND;
		};
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity entity, LivingEntity attacker) {

		if(!entity.level().isClientSide) {
			if(type == GavelType.LEAD) {
				entity.addEffect(new MobEffectInstance(HbmPotion.lead, 15 * 20, 4));
			}

			if(type == GavelType.DIAMOND) {
				float ded = entity.getMaxHealth() / 3;
				entity.setHealth(entity.getHealth() - ded);
			}

			entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSounds.get("weapon.whack"), SoundSource.PLAYERS, 3.0F, 1.F);
		}

		return super.hurtEnemy(stack, entity, attacker);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		switch(type) {
		case WOOD -> list.add(Component.literal("Thunk!"));
		case LEAD -> list.add(Component.literal("You are hereby sentenced to lead poisoning."));
		case DIAMOND -> {
			list.add(Component.literal("The joke! It makes sense now!!"));
			list.add(Component.literal(""));
			list.add(Component.literal("Deals as much damage as it needs to.").withStyle(ChatFormatting.BLUE));
		}
		}
	}
}
