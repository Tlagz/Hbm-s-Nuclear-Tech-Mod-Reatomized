package com.hbm.packet;

import com.hbm.lib.RefStrings;
import com.hbm.packet.toclient.BufPacket;
import com.hbm.packet.toclient.ExtPropPacket;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.packet.toserver.NBTItemControlPacket;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Registers all network payloads, counterpart of the original's SimpleNetworkWrapper setup */
@EventBusSubscriber(modid = RefStrings.MODID)
public class PacketDispatcher {

	public static final String PROTOCOL = "1";

	@SubscribeEvent
	public static void register(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar(PROTOCOL);

		registrar.playToClient(ExtPropPacket.TYPE, ExtPropPacket.STREAM_CODEC, ExtPropPacket::handle);
		registrar.playToClient(BufPacket.TYPE, BufPacket.STREAM_CODEC, BufPacket::handle);
		registrar.playToClient(com.hbm.packet.toclient.AuxParticlePacketNT.TYPE, com.hbm.packet.toclient.AuxParticlePacketNT.STREAM_CODEC, com.hbm.packet.toclient.AuxParticlePacketNT::handle);
		registrar.playToServer(NBTControlPacket.TYPE, NBTControlPacket.STREAM_CODEC, NBTControlPacket::handle);
		registrar.playToServer(NBTItemControlPacket.TYPE, NBTItemControlPacket.STREAM_CODEC, NBTItemControlPacket::handle);
		registrar.playToServer(com.hbm.packet.toserver.AnvilCraftPacket.TYPE, com.hbm.packet.toserver.AnvilCraftPacket.STREAM_CODEC, com.hbm.packet.toserver.AnvilCraftPacket::handle);
	}

	/** Whether the player's client can receive the payload, fake players (other mods' machines, GameTest mock players) can't */
	public static boolean canReceive(ServerPlayer player, CustomPacketPayload payload) {
		return player.connection != null && player.connection.hasChannel(payload);
	}

	/** PacketDistributor.sendToPlayersNear, skipping players that can't receive the payload */
	public static void sendToPlayersNear(ServerLevel level, double x, double y, double z, double range, CustomPacketPayload payload) {
		for(ServerPlayer player : level.players()) {
			if(player.distanceToSqr(x, y, z) < range * range && canReceive(player, payload)) {
				PacketDistributor.sendToPlayer(player, payload);
			}
		}
	}
}
