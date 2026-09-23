package com.hbm.packet.toclient;

import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IBufPacketReceiver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Tile entity state sync: the tile writes whatever it wants into a buffer ({@link IBufPacketReceiver#serialize}),
 * the client-side tile at the same position reads it back.
 */
public record BufPacket(BlockPos pos, byte[] data) implements CustomPacketPayload {

	public static final Type<BufPacket> TYPE = new Type<>(RefStrings.loc("buf"));

	public static final StreamCodec<RegistryFriendlyByteBuf, BufPacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, BufPacket::pos,
			ByteBufCodecs.BYTE_ARRAY, BufPacket::data,
			BufPacket::new);

	public static BufPacket of(BlockPos pos, IBufPacketReceiver rec) {
		ByteBuf buf = Unpooled.buffer();
		rec.serialize(buf);
		byte[] data = new byte[buf.readableBytes()];
		buf.readBytes(data);
		return new BufPacket(pos, data);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(BufPacket packet, IPayloadContext context) {
		Level level = context.player().level();
		if(!level.isLoaded(packet.pos())) return;

		BlockEntity te = level.getBlockEntity(packet.pos());

		if(te instanceof IBufPacketReceiver rec) {
			try {
				rec.deserialize(Unpooled.wrappedBuffer(packet.data()));
			} catch(Exception e) { // just in case I fucked up
				MainRegistry.logger.warn("A ByteBuf packet failed to be read and has thrown an error. This normally means that there was a buffer underflow and more data was read than was actually in the packet.");
				MainRegistry.logger.warn("Tile: {}", te.getType());
				MainRegistry.logger.warn(e.getMessage());
			}
		}
	}
}
