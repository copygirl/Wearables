package net.mcft.copy.wearables.common.network;

import java.io.IOException;

import net.mcft.copy.wearables.WearablesCommon;

import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public class WearablesContainerPacketS2C implements IPacket
{
	public static final Identifier ID = new Identifier(WearablesCommon.MOD_ID, "container");
	@Override public Identifier getID() { return ID; }
	
	
	public WearablesContainerPacketS2C() {  }
	
	@Override
	public void read(PacketByteBuf buffer)
		throws IOException
	{
	}
	
	@Override
	public void write(PacketByteBuf buffer)
		throws IOException
	{
	}
}
