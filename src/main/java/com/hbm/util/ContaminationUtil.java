package com.hbm.util;

import java.util.ArrayList;
import java.util.List;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.handler.HazmatRegistry;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.util.ArmorUtil.HazardClass;

import api.hbm.entity.IRadiationImmune;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ContaminationUtil {

	/** Calculates how much radiation can be applied to this entity by calculating resistance */
	public static float calculateRadiationMod(LivingEntity entity) {

		if(entity instanceof Player player) {
			float koeff = 10.0F;
			return (float) Math.pow(koeff, -HazmatRegistry.getResistance(player));
		}

		return 1;
	}

	public static float getRads(Entity e) {

		if(!(e instanceof LivingEntity entity))
			return 0.0F;

		if(isRadImmune(e))
			return 0.0F;

		return HbmLivingProps.getRadiation(entity);
	}

	/** TODO nuclear creepers, quackos (entities not ported yet), mutation potion */
	public static final List<Class<?>> immuneEntities = new ArrayList<>(List.of(
			MushroomCow.class,
			Zombie.class,
			AbstractSkeleton.class,
			Ocelot.class,
			IRadiationImmune.class));

	public static boolean isRadImmune(Entity e) {

		Class<?> entityClass = e.getClass();

		for(Class<?> clazz : immuneEntities) {
			if(clazz.isAssignableFrom(entityClass)) return true;
		}

		return false;
	}

	/** Players get a grace period after (re)spawning, like in the original */
	private static boolean isProtectedPlayer(Entity e) {
		return e instanceof Player player && (player.isCreative() || player.tickCount < 200);
	}

	/// ASBESTOS ///
	public static void applyAsbestos(Entity e, int i) {

		if(!(e instanceof LivingEntity entity))
			return;

		if(isProtectedPlayer(e))
			return;

		if(ArmorUtil.hasAllProtection(entity, HazardClass.PARTICLE_FINE))
			ArmorUtil.damageGasMaskFilter(entity, i);
		else
			HbmLivingProps.incrementAsbestos(entity, i);
	}

	/// DIGAMMA ///
	public static void applyDigammaData(Entity e, float f) {

		if(!(e instanceof LivingEntity entity))
			return;

		if(e instanceof Ocelot)
			return;

		if(isProtectedPlayer(e))
			return;

		if(!(entity instanceof Player player && ArmorUtil.checkForDigamma(player)))
			HbmLivingProps.incrementDigamma(entity, f);
	}

	public static void applyDigammaDirect(Entity e, float f) {

		if(!(e instanceof LivingEntity entity))
			return;

		if(e instanceof IRadiationImmune)
			return;

		if(e instanceof Player player && player.isCreative())
			return;

		HbmLivingProps.incrementDigamma(entity, f);
	}

	public static float getDigamma(Entity e) {
		return e instanceof LivingEntity entity ? HbmLivingProps.getDigamma(entity) : 0F;
	}

	public static void printGeigerData(Player player) {

		Level world = player.level();

		double eRad = ((int) (HbmLivingProps.getRadiation(player) * 10)) / 10D;
		double rads = ((int) (ChunkRadiationManager.proxy.getRadiation(world, player.blockPosition()) * 10)) / 10D;
		double env = ((int) (HbmLivingProps.getRadBuf(player) * 10D)) / 10D;

		double res = ((int) (10000D - ContaminationUtil.calculateRadiationMod(player) * 10000D)) / 100D;
		double resKoeff = ((int) (HazmatRegistry.getResistance(player) * 100D)) / 100D;

		ChatFormatting radPrefix;
		if(eRad < 200) radPrefix = ChatFormatting.GREEN;
		else if(eRad < 400) radPrefix = ChatFormatting.YELLOW;
		else if(eRad < 600) radPrefix = ChatFormatting.GOLD;
		else if(eRad < 800) radPrefix = ChatFormatting.RED;
		else if(eRad < 1000) radPrefix = ChatFormatting.DARK_RED;
		else radPrefix = ChatFormatting.DARK_GRAY;

		ChatFormatting resPrefix = resKoeff > 0 ? ChatFormatting.GREEN : ChatFormatting.WHITE;

		player.sendSystemMessage(Component.literal("===== ☢ ").append(Component.translatable("geiger.title")).append(" ☢ =====").withStyle(ChatFormatting.GOLD));
		player.sendSystemMessage(Component.translatable("geiger.chunkRad").append(Component.literal(" " + rads + " RAD/s").withStyle(getPrefixFromRad(rads))).withStyle(ChatFormatting.YELLOW));
		player.sendSystemMessage(Component.translatable("geiger.envRad").append(Component.literal(" " + env + " RAD/s").withStyle(getPrefixFromRad(env))).withStyle(ChatFormatting.YELLOW));
		player.sendSystemMessage(Component.translatable("geiger.playerRad").append(Component.literal(" " + eRad + " RAD").withStyle(radPrefix)).withStyle(ChatFormatting.YELLOW));
		player.sendSystemMessage(Component.translatable("geiger.playerRes").append(Component.literal(" " + res + "% (" + resKoeff + ")").withStyle(resPrefix)).withStyle(ChatFormatting.YELLOW));
	}

	public static void printDosimeterData(Player player) {

		double env = ((int) (HbmLivingProps.getRadBuf(player) * 10D)) / 10D;
		boolean limit = false;

		if(env > 3.6D) {
			env = 3.6D;
			limit = true;
		}

		player.sendSystemMessage(Component.literal("===== ☢ ").append(Component.translatable("geiger.title.dosimeter")).append(" ☢ =====").withStyle(ChatFormatting.GOLD));
		player.sendSystemMessage(Component.translatable("geiger.envRad").append(Component.literal(" " + (limit ? ">" : "") + env + " RAD/s").withStyle(getPrefixFromRad(env))).withStyle(ChatFormatting.YELLOW));
	}

	public static ChatFormatting getPrefixFromRad(double rads) {
		if(rads == 0) return ChatFormatting.GREEN;
		if(rads < 1) return ChatFormatting.YELLOW;
		if(rads < 10) return ChatFormatting.GOLD;
		if(rads < 100) return ChatFormatting.RED;
		if(rads < 1000) return ChatFormatting.DARK_RED;
		return ChatFormatting.DARK_GRAY;
	}

	public static void printDiagnosticData(Player player) {

		double digamma = ((int) (HbmLivingProps.getDigamma(player) * 100)) / 100D;
		double halflife = ((int) ((1D - Math.pow(0.5, digamma)) * 10000)) / 100D;

		player.sendSystemMessage(Component.literal("===== Ϝ ").append(Component.translatable("digamma.title")).append(" Ϝ =====").withStyle(ChatFormatting.DARK_PURPLE));
		player.sendSystemMessage(Component.translatable("digamma.playerDigamma").append(Component.literal(" " + digamma + " DRX").withStyle(ChatFormatting.RED)).withStyle(ChatFormatting.LIGHT_PURPLE));
		player.sendSystemMessage(Component.translatable("digamma.playerHealth").append(Component.literal(" " + halflife + "%").withStyle(ChatFormatting.RED)).withStyle(ChatFormatting.LIGHT_PURPLE));
		player.sendSystemMessage(Component.translatable("digamma.playerRes").append(Component.literal(" N/A").withStyle(ChatFormatting.BLUE)).withStyle(ChatFormatting.LIGHT_PURPLE));
	}

	public static enum HazardType {
		RADIATION,
		DIGAMMA
	}

	public static enum ContaminationType {
		FARADAY,			//preventable by metal armor
		HAZMAT,				//preventable by hazmat
		HAZMAT2,			//preventable by heavy hazmat
		DIGAMMA,			//preventable by fau armor or stability
		DIGAMMA2,			//preventable by robes
		CREATIVE,			//preventable by creative mode, for rad calculation armor piece bonuses still apply
		RAD_BYPASS,			//same as creative but will not apply radiation resistance calculation
		NONE				//not preventable
	}

	/** Applies radiation or digamma, respecting armor, creative mode and immunities. Returns whether anything was applied. */
	public static boolean contaminate(LivingEntity entity, HazardType hazard, ContaminationType cont, float amount) {

		if(hazard == HazardType.RADIATION) {
			float radEnv = HbmLivingProps.getRadEnv(entity);
			HbmLivingProps.setRadEnv(entity, radEnv + amount);
		}

		if(entity instanceof Player player) {

			switch(cont) {
			case FARADAY:	if(ArmorUtil.checkForFaraday(player)) return false; break;
			case HAZMAT:	if(ArmorUtil.checkForHazmat(player)) return false; break;
			case HAZMAT2:	if(ArmorUtil.checkForHaz2(player)) return false; break;
			case DIGAMMA:	if(ArmorUtil.checkForDigamma(player)) return false; if(ArmorUtil.checkForDigamma2(player)) return false; break;
			case DIGAMMA2:	if(ArmorUtil.checkForDigamma2(player)) return false; break;
			default: break;
			}

			if(player.isCreative() && cont != ContaminationType.NONE && cont != ContaminationType.DIGAMMA2)
				return false;

			if(player.tickCount < 200)
				return false;
		}

		if(hazard == HazardType.RADIATION && isRadImmune(entity))
			return false;

		switch(hazard) {
		case RADIATION: HbmLivingProps.incrementRadiation(entity, amount * (cont == ContaminationType.RAD_BYPASS ? 1 : calculateRadiationMod(entity))); break;
		case DIGAMMA: HbmLivingProps.incrementDigamma(entity, amount); break;
		}

		return true;
	}
}
