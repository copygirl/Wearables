package net.mcft.copy.wearables.common.network;

import java.io.IOException;

import net.minecraft.util.PacketByteBuf;

public interface INetSerializable
{
	public void read(PacketByteBuf buffer)
		throws IOException;
	
	public void write(PacketByteBuf buffer)
		throws IOException;
}
