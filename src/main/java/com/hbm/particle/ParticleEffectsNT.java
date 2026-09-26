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
	}
}
