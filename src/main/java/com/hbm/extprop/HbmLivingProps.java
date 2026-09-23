package com.hbm.extprop;

import java.util.ArrayList;
import java.util.List;

import com.hbm.config.RadiationConfig;
import com.hbm.config.ServerConfig;
import com.hbm.lib.ModDamageSource;
import com.hbm.lib.RefStrings;
import com.hbm.main.ModAttachments;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * Extra data on every living entity (radiation, digamma, lung diseases etc.).
 * Was an IExtendedEntityProperties, now a data attachment ({@link ModAttachments#LIVING_PROPS}).
 * The static accessors are the same as in the original.
 *
 * TODO achievements (advancements), duck exceptions, particle packet on digamma death, gas hazard HUD message
 */
public class HbmLivingProps implements INBTSerializable<CompoundTag> {

	public static final ResourceLocation DIGAMMA_MODIFIER = RefStrings.loc("digamma");

	/// VALS ///
	private float radiation;
	private float digamma;
	private int asbestos;
	public static final int maxAsbestos = 60 * 60 * 20;
	private int blacklung;
	public static final int maxBlacklung = 2 * 60 * 60 * 20;
	private float radEnv;
	private float radBuf;
	private int bombTimer;
	private int contagion;
	private int oil;
	public int fire;
	public int phosphorus;
	public int balefire;
	public int blackFire;
	private List<ContaminationEffect> contamination = new ArrayList<>();

	/// DATA ///
	public static HbmLivingProps getData(LivingEntity entity) {
		return entity.getData(ModAttachments.LIVING_PROPS);
	}

	/// RADIATION ///
	public static float getRadiation(LivingEntity entity) {
		if(!RadiationConfig.enableContamination)
			return 0;

		return getData(entity).radiation;
	}

	public static void setRadiation(LivingEntity entity, float rad) {
		if(RadiationConfig.enableContamination)
			getData(entity).radiation = rad;
	}

	public static void incrementRadiation(LivingEntity entity, float rad) {
		if(!RadiationConfig.enableContamination)
			return;

		float radiation = getData(entity).radiation + rad;

		if(radiation > 2500)
			radiation = 2500;
		if(radiation < 0)
			radiation = 0;

		setRadiation(entity, radiation);
	}

	/// RAD ENV ///
	public static float getRadEnv(LivingEntity entity) { return getData(entity).radEnv; }
	public static void setRadEnv(LivingEntity entity, float rad) { getData(entity).radEnv = rad; }

	/// RAD BUF ///
	public static float getRadBuf(LivingEntity entity) { return getData(entity).radBuf; }
	public static void setRadBuf(LivingEntity entity, float rad) { getData(entity).radBuf = rad; }

	/// CONTAMINATION ///
	public static List<ContaminationEffect> getCont(LivingEntity entity) { return getData(entity).contamination; }
	public static void addCont(LivingEntity entity, ContaminationEffect cont) { getData(entity).contamination.add(cont); }

	/// DIGAMA ///
	public static float getDigamma(LivingEntity entity) {
		return getData(entity).digamma;
	}

	public static void setDigamma(LivingEntity entity, float digamma) {

		if(entity.level().isClientSide)
			return;

		getData(entity).digamma = digamma;

		float healthMod = (float) Math.pow(0.5, digamma) - 1F;

		AttributeInstance attributeinstance = entity.getAttribute(Attributes.MAX_HEALTH);

		if(attributeinstance != null) {
			// operation 2 in 1.7.10 = multiply total
			attributeinstance.addOrReplacePermanentModifier(new AttributeModifier(DIGAMMA_MODIFIER, healthMod, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		}

		if(entity.getHealth() > entity.getMaxHealth() && entity.getMaxHealth() > 0) {
			entity.setHealth(entity.getMaxHealth());
		}

		if((entity.getMaxHealth() <= 0 || digamma >= 10.0F) && entity.isAlive()) {
			entity.setAbsorptionAmount(0);
			entity.hurt(ModDamageSource.source(entity.level(), ModDamageSource.DIGAMMA), 500F);
			entity.setHealth(0);
		}
	}

	public static void incrementDigamma(LivingEntity entity, float digamma) {

		float dRad = getDigamma(entity) + digamma;

		if(dRad > 10)
			dRad = 10;
		if(dRad < 0)
			dRad = 0;

		setDigamma(entity, dRad);
	}

	/// ASBESTOS ///
	public static int getAsbestos(LivingEntity entity) {
		if(RadiationConfig.disableAsbestos) return 0;
		return getData(entity).asbestos;
	}

	public static void setAsbestos(LivingEntity entity, int asbestos) {
		if(RadiationConfig.disableAsbestos) return;
		getData(entity).asbestos = asbestos;

		if(asbestos >= maxAsbestos) {
			getData(entity).asbestos = 0;
			entity.hurt(ModDamageSource.source(entity.level(), ModDamageSource.ASBESTOS), 1000);
		}
	}

	public static void incrementAsbestos(LivingEntity entity, int asbestos) {
		if(RadiationConfig.disableAsbestos) return;
		setAsbestos(entity, getAsbestos(entity) + asbestos);

		if(entity instanceof ServerPlayer player) {
			player.displayClientMessage(Component.translatable("info.asbestos").withStyle(ChatFormatting.RED), true);
		}
	}

	/// BLACK LUNG DISEASE ///
	public static int getBlackLung(LivingEntity entity) {
		if(RadiationConfig.disableCoal) return 0;
		return getData(entity).blacklung;
	}

	public static void setBlackLung(LivingEntity entity, int blacklung) {
		if(RadiationConfig.disableCoal) return;
		getData(entity).blacklung = blacklung;

		if(blacklung >= maxBlacklung) {
			getData(entity).blacklung = 0;
			entity.hurt(ModDamageSource.source(entity.level(), ModDamageSource.BLACKLUNG), 1000);
		}
	}

	public static void incrementBlackLung(LivingEntity entity, int blacklung) {
		if(RadiationConfig.disableCoal) return;
		setBlackLung(entity, getBlackLung(entity) + blacklung);

		if(entity instanceof ServerPlayer player) {
			player.displayClientMessage(Component.translatable("info.coaldust").withStyle(ChatFormatting.RED), true);
		}
	}

	/// TIME BOMB ///
	public static int getTimer(LivingEntity entity) { return getData(entity).bombTimer; }
	public static void setTimer(LivingEntity entity, int bombTimer) { getData(entity).bombTimer = bombTimer; }

	/// CONTAGION ///
	public static int getContagion(LivingEntity entity) {
		if(!ServerConfig.ENABLE_MKU) return 0;
		return getData(entity).contagion;
	}

	public static void setContagion(LivingEntity entity, int contagion) { getData(entity).contagion = contagion; }

	/// OIL ///
	public static int getOil(LivingEntity entity) { return getData(entity).oil; }
	public static void setOil(LivingEntity entity, int oil) { getData(entity).oil = oil; }

	/// SYNC ///
	public void serialize(FriendlyByteBuf buf) {
		buf.writeFloat(radiation);
		buf.writeFloat(digamma);
		buf.writeInt(asbestos);
		buf.writeInt(bombTimer);
		buf.writeInt(contagion);
		buf.writeInt(blacklung);
		buf.writeInt(oil);
		buf.writeFloat(radBuf);
		buf.writeInt(this.contamination.size());
		for(ContaminationEffect contaminationEffect : this.contamination) {
			contaminationEffect.serialize(buf);
		}
	}

	public void deserialize(FriendlyByteBuf buf) {
		if(buf.readableBytes() > 0) {
			radiation = buf.readFloat();
			digamma = buf.readFloat();
			asbestos = buf.readInt();
			bombTimer = buf.readInt();
			contagion = buf.readInt();
			blacklung = buf.readInt();
			oil = buf.readInt();
			radBuf = buf.readFloat();
			int size = buf.readInt();
			this.contamination.clear();
			for(int i = 0; i < size; i++) {
				this.contamination.add(ContaminationEffect.deserialize(buf));
			}
		}
	}

	/// SAVE (same keys as the original) ///
	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag props = new CompoundTag();

		props.putFloat("hfr_radiation", radiation);
		props.putFloat("hfr_digamma", digamma);
		props.putInt("hfr_asbestos", asbestos);
		props.putInt("hfr_bomb", bombTimer);
		if(ServerConfig.ENABLE_MKU) props.putInt("hfr_contagion", contagion);
		props.putInt("hfr_blacklung", blacklung);
		props.putInt("hfr_oil", oil);
		props.putInt("hfr_fire", fire);
		props.putInt("hfr_phosphorus", phosphorus);
		props.putInt("hfr_balefire", balefire);
		props.putInt("hfr_blackfire", blackFire);

		props.putInt("hfr_cont_count", this.contamination.size());

		for(int i = 0; i < this.contamination.size(); i++) {
			this.contamination.get(i).save(props, i);
		}

		return props;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag props) {
		radiation = props.getFloat("hfr_radiation");
		digamma = props.getFloat("hfr_digamma");
		asbestos = props.getInt("hfr_asbestos");
		bombTimer = props.getInt("hfr_bomb");
		if(ServerConfig.ENABLE_MKU) contagion = props.getInt("hfr_contagion");
		blacklung = props.getInt("hfr_blacklung");
		oil = props.getInt("hfr_oil");
		fire = props.getInt("hfr_fire");
		phosphorus = props.getInt("hfr_phosphorus");
		balefire = props.getInt("hfr_balefire");
		blackFire = props.getInt("hfr_blackfire");

		int cont = props.getInt("hfr_cont_count");

		this.contamination.clear();
		for(int i = 0; i < cont; i++) {
			this.contamination.add(ContaminationEffect.load(props, i));
		}
	}

	public static class ContaminationEffect {

		public float maxRad;
		public int maxTime;
		public int time;
		public boolean ignoreArmor;

		public ContaminationEffect(float rad, int time, boolean ignoreArmor) {
			this.maxRad = rad;
			this.maxTime = this.time = time;
			this.ignoreArmor = ignoreArmor;
		}

		public float getRad() {
			return maxRad * ((float) time / (float) maxTime);
		}

		public void serialize(FriendlyByteBuf buf) {
			buf.writeFloat(this.maxRad);
			buf.writeInt(this.maxTime);
			buf.writeInt(this.time);
			buf.writeBoolean(ignoreArmor);
		}

		public static ContaminationEffect deserialize(FriendlyByteBuf buf) {
			float maxRad = buf.readFloat();
			int maxTime = buf.readInt();
			int time = buf.readInt();
			boolean ignoreArmor = buf.readBoolean();
			ContaminationEffect effect = new ContaminationEffect(maxRad, maxTime, ignoreArmor);
			effect.time = time;
			return effect;
		}

		public void save(CompoundTag nbt, int index) {
			CompoundTag me = new CompoundTag();
			me.putFloat("maxRad", this.maxRad);
			me.putInt("maxTime", this.maxTime);
			me.putInt("time", this.time);
			me.putBoolean("ignoreArmor", ignoreArmor);
			nbt.put("cont_" + index, me);
		}

		/** The original read everything except maxRad from the parent tag by mistake, fixed here */
		public static ContaminationEffect load(CompoundTag nbt, int index) {
			CompoundTag me = nbt.getCompound("cont_" + index);
			float maxRad = me.getFloat("maxRad");
			int maxTime = Math.max(1, me.getInt("maxTime"));
			int time = me.getInt("time");
			boolean ignoreArmor = me.getBoolean("ignoreArmor");

			ContaminationEffect effect = new ContaminationEffect(maxRad, maxTime, ignoreArmor);
			effect.time = time;
			return effect;
		}
	}
}
