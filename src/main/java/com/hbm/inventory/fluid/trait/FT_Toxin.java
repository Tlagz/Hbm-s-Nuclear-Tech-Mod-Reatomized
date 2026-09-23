package com.hbm.inventory.fluid.trait;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.lib.ModDamageSource;
import com.hbm.lib.RefStrings;
import com.hbm.util.ArmorUtil;
import com.hbm.util.ArmorUtil.HazardClass;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class FT_Toxin extends FluidTrait {
	
	public List<ToxinEntry> entries = new ArrayList();
	
	public FT_Toxin addEntry(ToxinEntry entry) {
		entries.add(entry);
		return this;
	}
	
	@Override
	public void addInfoHidden(List<String> info) {
		info.add(ChatFormatting.LIGHT_PURPLE + "[" + I18nUtil.resolveKey("hbmfluid.trait.toxin") + "]");
		
		for(ToxinEntry entry : entries) {
			entry.addInfo(info);
		}
	}
	
	public void affect(LivingEntity entity, double intensity) {
		
		for(ToxinEntry entry : entries) {
			entry.poison(entity, intensity);
		}
	}

	public static abstract class ToxinEntry {
		
		public HazardClass clazz;
		public boolean fullBody = false;
		
		public ToxinEntry(HazardClass clazz, boolean fullBody) {
			this.clazz = clazz;
			this.fullBody = fullBody;
		}
		
		public boolean isProtected(LivingEntity entity) {
			
			boolean hasMask = clazz == null;
			boolean hasSuit = !fullBody;
			
			if(clazz != null && ArmorUtil.hasAllProtection(entity, clazz)) {
				ArmorUtil.damageGasMaskFilter(entity, 1);
				hasMask = true;
			}
			
			if(fullBody && entity instanceof Player player && ArmorUtil.checkForHazmat(player)) {
				hasSuit = true;
			}
			
			return hasMask && hasSuit;
		}

		public abstract void poison(LivingEntity entity, double intensity);
		public abstract void addInfo(List<String> info);
	}

	public static class ToxinDirectDamage extends ToxinEntry {

		public ResourceKey<DamageType> damage;
		public float amount;
		public int delay;
		
		public ToxinDirectDamage(ResourceKey<DamageType> damage, float amount, int delay, HazardClass clazz, boolean fullBody) {
			super(clazz, fullBody);
			this.damage = damage;
			this.amount = amount;
			this.delay = delay;
		}

		@Override
		public void poison(LivingEntity entity, double intensity) {
			
			if(isProtected(entity)) return;
			
			if(delay == 0 || entity.level().getGameTime() % delay == 0) {
				entity.hurt(ModDamageSource.source(entity.level(), damage), (float) (amount * intensity));
			}
		}

		@Override
		public void addInfo(List<String> info) {
			info.add(ChatFormatting.YELLOW + "- " + I18nUtil.resolveKey(clazz.lang) + (fullBody ? ChatFormatting.RED + " (" + I18nUtil.resolveKey("hbmfluid.trait.hazmat") + ")" : "") + ": " + ChatFormatting.YELLOW + String.format(Locale.US, "%,.1f", amount * 20 / delay) + " " + I18nUtil.resolveKey("hbmfluid.trait.perDamage"));
		}
	}

	public static class ToxinEffects extends ToxinEntry {

		public List<MobEffectInstance> effects = new ArrayList();
		
		public ToxinEffects(HazardClass clazz, boolean fullBody) {
			super(clazz, fullBody);
		}
		
		public ToxinEffects add(MobEffectInstance... effs) {
			for(MobEffectInstance eff : effs) this.effects.add(eff);
			return this;
		}

		@Override
		public void poison(LivingEntity entity, double intensity) {
			
			if(isProtected(entity)) return;
			
			for(MobEffectInstance eff : effects) {
				entity.addEffect(new MobEffectInstance(eff.getEffect(), (int) (eff.getDuration() * intensity), eff.getAmplifier()));
			}
		}

		@Override
		public void addInfo(List<String> info) {
			info.add(ChatFormatting.YELLOW + "- " + I18nUtil.resolveKey(clazz.lang) + (fullBody ? ChatFormatting.RED + " (" + I18nUtil.resolveKey("hbmfluid.trait.hazmat") + ")" + ChatFormatting.YELLOW : "") + ":");
			
			for(MobEffectInstance eff : effects) {
				info.add(ChatFormatting.YELLOW + "   - " + I18nUtil.resolveKey(eff.getDescriptionId()) + (eff.getAmplifier() > 0 ? " " + I18nUtil.resolveKey("potion.potency." + eff.getAmplifier()).trim() : "") + " " + StringUtil.formatTickDuration(eff.getDuration(), 20F));
			}
		}
	}
	
	/** Damage types used to be plain message ids ("cloud"), those are assumed to be hbm's */
	private static ResourceLocation parseId(String id) {
		return id.contains(":") ? ResourceLocation.parse(id) : RefStrings.loc(id.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.US));
	}

	@Override public void serializeJSON(JsonWriter writer) throws IOException {
		
		writer.name("entries").beginArray();
		
		for(ToxinEntry entry : entries) {
			writer.beginObject();

			if(entry instanceof ToxinDirectDamage) {
				ToxinDirectDamage e = (ToxinDirectDamage) entry;
				writer.name("type").value("directdamage");
				writer.name("amount").value(e.amount);
				writer.name("source").value(e.damage.location().toString());
				writer.name("delay").value(e.delay);
				writer.name("hazmat").value(e.fullBody);
				writer.name("masktype").value(e.clazz.name());
			}
			if(entry instanceof ToxinEffects) {
				ToxinEffects e = (ToxinEffects) entry;
				writer.name("type").value("effects");
				writer.name("effects").beginArray();
				writer.setIndent("");
				for(MobEffectInstance effect : e.effects) {
					writer.beginArray();
					writer.value(effect.getEffect().unwrapKey().map(k -> k.location().toString()).orElse("")).value(effect.getDuration()).value(effect.getAmplifier()).value(effect.isAmbient());
					writer.endArray();
				}
				writer.endArray();
				writer.setIndent("  ");
				writer.name("hazmat").value(e.fullBody);
				writer.name("masktype").value(e.clazz.name());
			}
			
			writer.endObject();
		}
		
		writer.endArray();
	}
	
	@Override public void deserializeJSON(JsonObject obj) {
		JsonArray array = obj.get("entries").getAsJsonArray();
		
		for(int i = 0; i < array.size(); i++) {
			JsonObject entry = array.get(i).getAsJsonObject();
			String name = entry.get("type").getAsString();
			
			if(name.equals("directdamage")) {
				ToxinDirectDamage e = new ToxinDirectDamage(
						ResourceKey.create(Registries.DAMAGE_TYPE, parseId(entry.get("source").getAsString())),
						entry.get("amount").getAsFloat(),
						entry.get("delay").getAsInt(),
						HazardClass.valueOf(entry.get("masktype").getAsString()),
						entry.get("hazmat").getAsBoolean()
						);
				this.entries.add(e);
			}
			
			if(name.equals("effects")) {
				ToxinEffects e = new ToxinEffects(
						HazardClass.valueOf(entry.get("masktype").getAsString()),
						entry.get("hazmat").getAsBoolean()
						);
				JsonArray effects = entry.get("effects").getAsJsonArray();
				for(int j = 0; j < effects.size(); j++) {
					JsonArray effect = effects.get(j).getAsJsonArray();
					// effects are saved by registry name now (the original used numeric potion IDs)
					Holder<MobEffect> type = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse(effect.get(0).getAsString())).orElse(null);
					if(type == null) continue;
					MobEffectInstance potion = new MobEffectInstance(type, effect.get(1).getAsInt(), effect.get(2).getAsInt(), effect.get(3).getAsBoolean(), true);
					e.effects.add(potion);
				}
				this.entries.add(e);
			}
		}
	}
}
