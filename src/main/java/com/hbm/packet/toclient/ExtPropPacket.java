package com.hbm.packet.toclient;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.lib.RefStrings;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Syncs the player's own HbmLivingProps to their client every tick, like the original.
 * TODO HbmPlayerProps (shield, keybinds etc.) once ported
 */
public record ExtPropPacket(byte[] livingData) implements CustomPacketPayload {

	public static final Type<ExtPropPacket> TYPE = new Type<>(RefStrings.loc("ext_prop"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ExtPropPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.BYTE_ARRAY, ExtPropPacket::livingData,
			ExtPropPacket::new);

	public static ExtPropPacket of(HbmLivingProps props) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		props.serialize(buf);
		byte[] data = new byte[buf.readableBytes()];
		buf.readBytes(data);
		return new ExtPropPacket(data);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(ExtPropPacket packet, IPayloadContext context) {
		Player player = context.player();
		if(player == null) return;
		HbmLivingProps.getData(player).deserialize(new FriendlyByteBuf(Unpooled.wrappedBuffer(packet.livingData())));
	}
}
