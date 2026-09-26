package com.hbm.util;

import com.hbm.packet.toclient.AuxParticlePacketNT;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/** Shortcuts for the effectNT particles that are spawned from common code */
public class ParticleUtil {

	/** Burning gas puff, sent to players in range when called on the server */
	public static void spawnGasFlame(Level world, double x, double y, double z, double mX, double mY, double mZ) {

		CompoundTag data = new CompoundTag();
		data.putString("type", "gasfire");
		data.putDouble("mX", mX);
		data.putDouble("mY", mY);
		data.putDouble("mZ", mZ);

		if(world.isClientSide) {
			data.putDouble("posX", x);
			data.putDouble("posY", y);
			data.putDouble("posZ", z);
			com.hbm.particle.ParticleEffectsNT.effectNT(data);
		} else if(world instanceof ServerLevel server) {
			AuxParticlePacketNT.sendToAllAround(server, data, x, y, z, 150);
		}
	}
}
