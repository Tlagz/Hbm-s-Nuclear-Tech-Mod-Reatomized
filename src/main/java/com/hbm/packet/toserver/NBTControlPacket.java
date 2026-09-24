package com.hbm.packet.toserver;

import com.hbm.interfaces.IControlReceiver;
import com.hbm.lib.RefStrings;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** GUI button presses etc., the tile at the position receives the data if the player is allowed to use it */
public record NBTControlPacket(CompoundTag data, BlockPos pos) implements CustomPacketPayload {

	public static final Type<NBTControlPacket> TYPE = new Type<>(RefStrings.loc("nbt_control"));

	public static final StreamCodec<RegistryFriendlyByteBuf, NBTControlPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.COMPOUND_TAG, NBTControlPacket::data,
			BlockPos.STREAM_CODEC, NBTControlPacket::pos,
			NBTControlPacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	/** Client side convenience, the original did PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, x, y, z)) */
	public static void send(CompoundTag data, BlockPos pos) {
		PacketDistributor.sendToServer(new NBTControlPacket(data, pos));
	}

	public static void handle(NBTControlPacket packet, IPayloadContext context) {
		Player player = context.player();
		if(!player.level().isLoaded(packet.pos())) return;

		BlockEntity te = player.level().getBlockEntity(packet.pos());

		if(te instanceof IControlReceiver tile && tile.hasPermission(player)) {
			tile.receiveControl(player, packet.data());
			tile.receiveControl(packet.data());
		}
	}
}
