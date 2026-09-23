package com.hbm.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Armor protection checks.
 *
 * TODO these all depend on armor items that aren't ported yet (hazmat, faraday, FAU, robes, gas mask filters),
 *  until then nothing protects and the checks return false.
 */
public class ArmorUtil {

	public static boolean checkForFaraday(Player player) { return false; }
	public static boolean checkForHazmat(Player player) { return false; }
	public static boolean checkForHaz2(Player player) { return false; }
	public static boolean checkForDigamma(Player player) { return false; }
	public static boolean checkForDigamma2(Player player) { return false; }

	/** Whether the entity wears full protection (gas mask with filter) against the given hazard class */
	public static boolean hasAllProtection(LivingEntity entity, HazardClass hazard) { return false; }

	public static void damageGasMaskFilter(LivingEntity entity, int damage) { }

	/** From the original's ArmorRegistry */
	public enum HazardClass {
		GAS_LUNG("hazard.gasChlorine"),				//also attacks eyes -> no half mask
		GAS_MONOXIDE("hazard.gasMonoxide"),			//only affects lungs
		GAS_INERT("hazard.gasInert"),				//SA
		PARTICLE_COARSE("hazard.particleCoarse"),	//only affects lungs
		PARTICLE_FINE("hazard.particleFine"),		//only affects lungs
		BACTERIA("hazard.bacteria"),				//no half masks
		GAS_BLISTERING("hazard.corrosive"),			//corrosive substance, also attacks skin
		SAND("hazard.sand"),						//blinding sand particles
		LIGHT("hazard.light");						//blinding light

		public final String lang;

		private HazardClass(String lang) {
			this.lang = lang;
		}
	}
}
