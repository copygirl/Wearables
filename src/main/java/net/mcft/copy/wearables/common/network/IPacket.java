package net.mcft.copy.wearables.common.network;

import net.minecraft.util.Identifier;

public interface IPacket
	extends INetSerializable
{
	public Identifier getID();
}
