package com.hbm.potion;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.lib.ModDamageSource;
import com.hbm.lib.RefStrings;
import com.hbm.main.ModSounds;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * NTM's status effects. Icons are in textures/mob_effect (cut from the original's sheet by tools/SplitPotionIcons.java).
 *
 * TODO taint trails (taint block), bang particles + cheese drop, tainted creeper/crab exceptions
 */
public class HbmPotion extends MobEffect {

	public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, RefStrings.MODID);

	public static final DeferredHolder<MobEffect, HbmPotion> taint = register("taint", MobEffectCategory.HARMFUL, 0x800080);
	public static final DeferredHolder<MobEffect, HbmPotion> radiation = register("radiation", MobEffectCategory.HARMFUL, 0x84C128);
	public static final DeferredHolder<MobEffect, HbmPotion> bang = register("bang", MobEffectCategory.HARMFUL, 0x111111);
	public static final DeferredHolder<MobEffect, HbmPotion> mutation = register("mutation", MobEffectCategory.BENEFICIAL, 0x800080);
	public static final DeferredHolder<MobEffect, HbmPotion> radx = register("radx", MobEffectCategory.BENEFICIAL, 0xBB4B00);
	public static final DeferredHolder<MobEffect, HbmPotion> lead = register("lead", MobEffectCategory.HARMFUL, 0x767682);
	public static final DeferredHolder<MobEffect, HbmPotion> radaway = register("radaway", MobEffectCategory.BENEFICIAL, 0xBB4B00);
	public static final DeferredHolder<MobEffect, HbmPotion> phosphorus = register("phosphorus", MobEffectCategory.HARMFUL, 0xFFFF00);
	public static final DeferredHolder<MobEffect, HbmPotion> stability = register("stability", MobEffectCategory.BENEFICIAL, 0xD0D0D0);
	public static final DeferredHolder<MobEffect, HbmPotion> potionsickness = register("potionsickness", MobEffectCategory.BENEFICIAL, 0xff8080);
	public static final DeferredHolder<MobEffect, HbmPotion> death = register("death", MobEffectCategory.BENEFICIAL, 0x111111);

	private final String name;

	public HbmPotion(String name, MobEffectCategory category, int color) {
		super(category, color);
		this.name = name;
	}

	private static DeferredHolder<MobEffect, HbmPotion> register(String name, MobEffectCategory category, int color) {
		return EFFECTS.register(name, () -> new HbmPotion(name, category, color));
	}

	private boolean is(DeferredHolder<MobEffect, HbmPotion> holder) {
		return this.name.equals(holder.getId().getPath());
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int level) {

		if(entity.level().isClientSide) return true;

		if(is(taint)) {
			if(entity.getRandom().nextInt(40) == 0)
				entity.hurt(ModDamageSource.source(entity.level(), ModDamageSource.TAINT), (level + 1));
		}
		if(is(radiation)) {
			ContaminationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, (float) (level + 1F) * 0.05F);
		}
		if(is(radaway)) {
			HbmLivingProps.incrementRadiation(entity, -(level + 1));
		}
		if(is(bang)) {
			entity.hurt(ModDamageSource.source(entity.level(), ModDamageSource.BANG), 1000);
			entity.setHealth(0.0F);

			if(!(entity instanceof Player))
				entity.discard();

			entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSounds.get("weapon.laserBang"), SoundSource.PLAYERS, 100.0F, 1.0F);
		}
		if(is(lead)) {
			entity.hurt(ModDamageSource.source(entity.level(), ModDamageSource.LEAD), (level + 1));
		}
		if(is(phosphorus)) {
			entity.igniteForSeconds(1);
		}

		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {

		if(is(taint)) {
			return duration % 2 == 0;
		}

		if(is(radiation) || is(radaway) || is(phosphorus)) {
			return true;
		}

		if(is(bang)) {
			return duration <= 10;
		}

		if(is(lead)) {
			return duration % 60 == 0;
		}

		return false;
	}
}
