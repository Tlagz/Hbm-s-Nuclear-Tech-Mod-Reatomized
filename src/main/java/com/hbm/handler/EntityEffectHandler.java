package com.hbm.handler;

import java.util.List;
import java.util.Random;

import com.hbm.config.RadiationConfig;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.extprop.HbmLivingProps.ContaminationEffect;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.ModSounds;
import com.hbm.packet.toclient.ExtPropPacket;
import com.hbm.util.ArmorUtil;
import com.hbm.util.ArmorUtil.HazardClass;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Per-tick effects on living entities: radiation sickness, contamination, digamma, lung diseases.
 *
 * TODO ported so far: radiation, contamination, digamma, lung diseases.
 *  Missing: crater biomes, time bomb (needs explosions), contagion (MKU), oil, pollution effects, temperature (fire types),
 *  dashing/plinking, faux ladders, HbmPlayerProps shield, particle effects (vomit, sweat, radiation fog),
 *  nuclear creeper / quackos transformations, advancements.
 */
public class EntityEffectHandler {

	public static void onUpdate(LivingEntity entity) {

		if(entity.tickCount % 20 == 0) {
			HbmLivingProps.setRadBuf(entity, HbmLivingProps.getRadEnv(entity));
			HbmLivingProps.setRadEnv(entity, 0);
		}

		// fake players (e.g. other mods' machines, GameTest mock players) have no channel to send to
		if(entity instanceof ServerPlayer player) {
			ExtPropPacket packet = ExtPropPacket.of(HbmLivingProps.getData(player));
			if(com.hbm.packet.PacketDispatcher.canReceive(player, packet)) PacketDistributor.sendToPlayer(player, packet);
		}

		handleContamination(entity);
		handleRadiationEffect(entity);
		handleRadiationFX(entity);
		handleLungDisease(entity);
	}

	private static void handleContamination(LivingEntity entity) {

		if(entity.level().isClientSide)
			return;

		List<ContaminationEffect> contamination = HbmLivingProps.getCont(entity);

		contamination.removeIf(con -> {
			ContaminationUtil.contaminate(entity, HazardType.RADIATION, con.ignoreArmor ? ContaminationType.RAD_BYPASS : ContaminationType.CREATIVE, con.getRad());
			con.time--;
			return con.time <= 0;
		});
	}

	/** Handles entity transformation via radiation and applied potion effects */
	private static void handleRadiationEffect(LivingEntity entity) {

		if(!entity.isAlive()) return;
		if(entity.level().isClientSide) return;
		if(entity instanceof Player player && player.isCreative()) return;

		Level world = entity.level();
		Random rand = new Random(); // the original used world.rand, the modern level random isn't meant for this many calls

		float eRad = HbmLivingProps.getRadiation(entity);

		/// TRANSFORMATIONS ///
		if(entity.getClass() == Creeper.class && eRad >= 200 && entity.getHealth() > 0) {
			// TODO turns into a nuclear creeper 1 in 3 times once that entity exists
			entity.hurt(ModDamageSource.source(world, ModDamageSource.RADIATION), 100F);
			return;
		} else if(entity instanceof Cow cow && !(entity instanceof MushroomCow) && eRad >= 50) {
			cow.convertTo(EntityType.MOOSHROOM, false);
			return;
		} else if(entity instanceof Villager villager && eRad >= 500) {
			villager.convertTo(EntityType.ZOMBIE, false);
			return;
		}

		if(eRad < 200 || ContaminationUtil.isRadImmune(entity)) return;
		if(eRad > 2500) HbmLivingProps.setRadiation(entity, 2500);

		/// EFFECTS ///
		if(eRad >= 1000) {

			entity.hurt(ModDamageSource.source(world, ModDamageSource.RADIATION), 1000F);
			HbmLivingProps.setRadiation(entity, 0);

			if(entity.getHealth() > 0 && !(entity instanceof Player)) {
				entity.setHealth(0);
				entity.die(ModDamageSource.source(world, ModDamageSource.RADIATION));
			}

		} else if(eRad >= 800) {
			if(rand.nextInt(300) == 0) entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 5 * 30, 0));
			if(rand.nextInt(300) == 0) entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10 * 20, 2));
			if(rand.nextInt(300) == 0) entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 10 * 20, 2));
			if(rand.nextInt(500) == 0) entity.addEffect(new MobEffectInstance(MobEffects.POISON, 3 * 20, 2));
			if(rand.nextInt(700) == 0) entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 3 * 20, 1));

		} else if(eRad >= 600) {
			if(rand.nextInt(300) == 0) entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 5 * 30, 0));
			if(rand.nextInt(300) == 0) entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10 * 20, 2));
			if(rand.nextInt(300) == 0) entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 10 * 20, 2));
			if(rand.nextInt(500) == 0) entity.addEffect(new MobEffectInstance(MobEffects.POISON, 3 * 20, 1));

		} else if(eRad >= 400) {
			if(rand.nextInt(300) == 0) entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 5 * 30, 0));
			if(rand.nextInt(500) == 0) entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5 * 20, 0));
			if(rand.nextInt(300) == 0) entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 5 * 20, 1));

		} else if(eRad >= 200) {
			if(rand.nextInt(300) == 0) entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 5 * 20, 0));
			if(rand.nextInt(500) == 0) entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 5 * 20, 0));
		}
	}

	/** Handles contamination from the chunk, the dimension as well as effects related to radiation sickness */
	private static void handleRadiationFX(LivingEntity entity) {

		Level world = entity.level();

		if(!world.isClientSide) {

			if(ContaminationUtil.isRadImmune(entity)) return;

			float rad = ChunkRadiationManager.proxy.getRadiation(world, entity.blockPosition());

			if(world.dimension() == Level.NETHER && RadiationConfig.hellRad > 0 && rad < RadiationConfig.hellRad)
				rad = (float) RadiationConfig.hellRad;

			if(rad > 0) ContaminationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, rad / 20F);

			if(entity instanceof Player player && player.isCreative()) return;

			Random rand = new Random(entity.getId());

			int r600 = rand.nextInt(600);
			int r1200 = rand.nextInt(1200);
			long time = world.getGameTime();

			if(HbmLivingProps.getRadiation(entity) > 600) {

				if((time + r600) % 600 == 1 && canVomit(entity)) {
					world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSounds.get("player.vomit"), SoundSource.PLAYERS, 1.0F, 1.0F);
					entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 19));
				}

			} else if(HbmLivingProps.getRadiation(entity) > 200 && (time + r1200) % 1200 == 1 && canVomit(entity)) {
				world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSounds.get("player.vomit"), SoundSource.PLAYERS, 1.0F, 1.0F);
				entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 19));
			}
		}
	}

	private static boolean canVomit(LivingEntity entity) {
		return entity.getType().getCategory() != MobCategory.WATER_CREATURE;
	}

	private static void handleLungDisease(LivingEntity entity) {

		if(entity.level().isClientSide)
			return;

		if(entity instanceof Player player && player.isCreative()) {
			HbmLivingProps.setBlackLung(entity, 0);
			HbmLivingProps.setAsbestos(entity, 0);
			return;
		} else {

			int bl = HbmLivingProps.getBlackLung(entity);

			if(bl > 0 && bl < HbmLivingProps.maxBlacklung * 0.5)
				HbmLivingProps.setBlackLung(entity, HbmLivingProps.getBlackLung(entity) - 1);
		}

		double blacklung = Math.min(HbmLivingProps.getBlackLung(entity), HbmLivingProps.maxBlacklung);
		double asbestos = Math.min(HbmLivingProps.getAsbestos(entity), HbmLivingProps.maxAsbestos);
		double soot = PollutionHandler.getPollution(entity.level(), BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ()), PollutionType.SOOT);

		if(!(entity instanceof Player)) soot = 0;

		if(ArmorUtil.hasAllProtection(entity, HazardClass.PARTICLE_COARSE)) soot = 0;

		boolean coughs = blacklung / HbmLivingProps.maxBlacklung > 0.25D || asbestos / HbmLivingProps.maxAsbestos > 0.25D || soot > 30;

		if(!coughs)
			return;

		double blacklungDelta = 1D - (blacklung / (double) HbmLivingProps.maxBlacklung);
		double asbestosDelta = 1D - (asbestos / (double) HbmLivingProps.maxAsbestos);
		double sootDelta = 1D - Math.min(soot / 100, 1D);

		double total = 1 - (blacklungDelta * asbestosDelta);

		Level world = entity.level();

		if(total > 0.75D) {
			entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 2));
		}

		if(total > 0.95D) {
			entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
		}

		total = 1 - (blacklungDelta * asbestosDelta * sootDelta);
		int freq = Math.max((int) (1000 - 950 * total), 20);

		if(world.getGameTime() % freq == entity.getId() % freq) {
			world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSounds.get("player.cough"), SoundSource.PLAYERS, 1.0F, 1.0F);
		}
	}
}
