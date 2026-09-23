package com.hbm.tileentity;

import io.netty.buffer.ByteBuf;

/** Tiles that sync their state to clients with {@link com.hbm.packet.toclient.BufPacket} */
public interface IBufPacketReceiver {

	public void serialize(ByteBuf buf);
	public void deserialize(ByteBuf buf);
}
