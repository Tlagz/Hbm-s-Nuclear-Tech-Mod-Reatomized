package com.hbm.packet.toserver;

import com.hbm.items.IItemControlReceiver;
import com.hbm.lib.RefStrings;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Settings from a client screen for the item in the player's main hand */
public record NBTItemControlPacket(CompoundTag data) implements CustomPacketPayload {

	public static final Type<NBTItemControlPacket> TYPE = new Type<>(RefStrings.loc("nbt_item_control"));

	public static final StreamCodec<RegistryFriendlyByteBuf, NBTItemControlPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.COMPOUND_TAG, NBTItemControlPacket::data,
			NBTItemControlPacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void send(CompoundTag data) {
		PacketDistributor.sendToServer(new NBTItemControlPacket(data));
	}

	public static void handle(NBTItemControlPacket packet, IPayloadContext context) {
		Player player = context.player();
		ItemStack held = player.getMainHandItem();

		if(!held.isEmpty() && held.getItem() instanceof IItemControlReceiver receiver) {
			receiver.receiveControl(player, held, packet.data());
		}
	}
}
