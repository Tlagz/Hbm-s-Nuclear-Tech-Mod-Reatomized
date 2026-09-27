package com.hbm.packet.toclient;

import com.hbm.lib.RefStrings;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server side trigger for {@link com.hbm.particle.ParticleEffectsNT#effectNT}: the effect compound and its position */
public record AuxParticlePacketNT(CompoundTag data, double x, double y, double z) implements CustomPacketPayload {

	public static final Type<AuxParticlePacketNT> TYPE = new Type<>(RefStrings.loc("aux_particle"));

	public static final StreamCodec<RegistryFriendlyByteBuf, AuxParticlePacketNT> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.COMPOUND_TAG, AuxParticlePacketNT::data,
			ByteBufCodecs.DOUBLE, AuxParticlePacketNT::x,
			ByteBufCodecs.DOUBLE, AuxParticlePacketNT::y,
			ByteBufCodecs.DOUBLE, AuxParticlePacketNT::z,
			AuxParticlePacketNT::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	/** The original's sendToAllAround(new AuxParticlePacketNT(data, x, y, z), new TargetPoint(dim, x, y, z, range)) */
	public static void sendToAllAround(ServerLevel world, CompoundTag data, double x, double y, double z, double range) {
		com.hbm.packet.PacketDispatcher.sendToPlayersNear(world, x, y, z, range, new AuxParticlePacketNT(data, x, y, z));
	}

	public static void handle(AuxParticlePacketNT packet, IPayloadContext context) {
		CompoundTag data = packet.data().copy();
		data.putDouble("posX", packet.x());
		data.putDouble("posY", packet.y());
		data.putDouble("posZ", packet.z());
		com.hbm.particle.ParticleEffectsNT.effectNT(data);
	}
}
