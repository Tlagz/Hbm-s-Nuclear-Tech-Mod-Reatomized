package com.hbm.particle;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;

/**
 * Client side of the original's ClientProxy.effectNT: particle effects described by an NBT compound with a "type"
 * and "posX/posY/posZ". Called directly by client side tile code or through {@link com.hbm.packet.toclient.AuxParticlePacketNT}.
 * Client only, don't touch this class from common code that runs on the server.
 *
 * TODO the other effect types, ported with the machines that use them
 */
public class ParticleEffectsNT {

	/** Sprites of {@link ModParticles#BASE}, set when the particle providers are registered */
	public static SpriteSet baseSprites;
	/** Sprites of {@link ModParticles#GAS_FLAME} (vanilla smoke) */
	public static SpriteSet gasFlameSprites;

	public static void effectNT(CompoundTag data) {

		Minecraft mc = Minecraft.getInstance();
		ClientLevel world = mc.level;
		if(world == null) return;

		RandomSource rand = world.random;
		String type = data.getString("type");
		double x = data.getDouble("posX");
		double y = data.getDouble("posY");
		double z = data.getDouble("posZ");

		// 0 = all, 1 = decreased, 2 = minimal, like the old particle setting
		int particleSetting = mc.options.particles().get() == ParticleStatus.ALL ? 0 : mc.options.particles().get() == ParticleStatus.DECREASED ? 1 : 2;

		if("tower".equals(type)) {
			if(baseSprites != null && (particleSetting == 0 || (particleSetting == 1 && rand.nextBoolean()))) {
				ParticleCoolingTower fx = new ParticleCoolingTower(world, x, y, z, baseSprites);
				fx.setLift(data.getFloat("lift"));
				fx.setBaseScale(data.getFloat("base"));
				fx.setMaxScale(data.getFloat("max"));
				fx.setLife(data.getInt("life") / (particleSetting + 1));
				if(data.contains("noWind")) fx.noWind();
				if(data.contains("strafe")) fx.setStrafe(data.getFloat("strafe"));
				if(data.contains("alpha")) fx.alphaMod(data.getFloat("alpha"));

				if(data.contains("color")) {
					Color color = new Color(data.getInt("color"));
					fx.setColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
				}

				mc.particleEngine.add(fx);
			}
		}

		if("gasfire".equals(type) && gasFlameSprites != null) {
			float scale = data.getFloat("scale");
			mc.particleEngine.add(new ParticleGasFlame(world, x, y, z, data.getDouble("mX"), data.getDouble("mY"), data.getDouble("mZ"), scale > 0 ? scale : 6.5F, gasFlameSprites));
		}

		// liquid pouring out (the drainage pipe), the original's ParticleLiquidSplash approximated with colored dust
		if("splash".equals(type) && (particleSetting == 0 || (particleSetting == 1 && rand.nextBoolean()))) {
			int color = data.getInt("color");
			org.joml.Vector3f rgb = new org.joml.Vector3f(((color >> 16) & 0xFF) / 255F, ((color >> 8) & 0xFF) / 255F, (color & 0xFF) / 255F);
			for(int i = 0; i < 3; i++) {
				world.addParticle(new net.minecraft.core.particles.DustParticleOptions(rgb, 1.5F), x + rand.nextGaussian() * 0.15, y - rand.nextFloat() * 0.5, z + rand.nextGaussian() * 0.15, 0, -0.5, 0);
			}
		}

		// TODO the other vanillaExt modes
		if("vanillaExt".equals(type) && "smoke".equals(data.getString("mode"))) {
			world.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE, x, y, z, data.getDouble("mX"), data.getDouble("mY"), data.getDouble("mZ"));
		}
	}
}
