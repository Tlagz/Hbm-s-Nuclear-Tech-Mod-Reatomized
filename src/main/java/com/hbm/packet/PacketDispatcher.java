package com.hbm.packet;

import com.hbm.lib.RefStrings;
import com.hbm.packet.toclient.ExtPropPacket;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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
	}
}
