package com.hbm.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/**
 * ByteBuf helpers for the tile sync packets (BufPacket), which use plain ByteBufs. ItemStacks need the
 * registries in 1.21, so the buffer is wrapped with the level's registry access.
 */
public class BufferUtil {

	public static void writeItemStack(ByteBuf buf, ItemStack stack, RegistryAccess registries) {
		ItemStack.OPTIONAL_STREAM_CODEC.encode(new RegistryFriendlyByteBuf(buf, registries), stack == null ? ItemStack.EMPTY : stack);
	}

	public static ItemStack readItemStack(ByteBuf buf, RegistryAccess registries) {
		return ItemStack.OPTIONAL_STREAM_CODEC.decode(new RegistryFriendlyByteBuf(buf, registries));
	}
}
