package net.mcft.copy.wearables.common.network;

import java.io.IOException;

import net.mcft.copy.wearables.WearablesCommon;
import net.mcft.copy.wearables.api.IWearablesSlot;
import net.mcft.copy.wearables.api.WearablesSlotType;

import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public class WearablesInteractPacket implements IPacket
{
	public static final Identifier ID = new Identifier(WearablesCommon.MOD_ID, "interact");
	@Override public Identifier getID() { return ID; }
	
	
	public WearablesSlotType slotType;
	public int index;
	
	public WearablesInteractPacket() {  }
	public WearablesInteractPacket(IWearablesSlot slot)
		{ this(slot.getSlotType(), slot.getIndex()); }
	public WearablesInteractPacket(WearablesSlotType slotType, int index)
	{
		if (slotType == null) throw new IllegalArgumentException("slotType is null");
		if (index < 0) throw new IllegalArgumentException("index is negative");
		this.slotType = slotType;
		this.index    = index;
	}
	
	@Override
	public void read(PacketByteBuf buffer)
		throws IOException
	{
		this.slotType = new WearablesSlotType(buffer.readString());
		this.index    = buffer.readByte();
	}
	
	@Override
	public void write(PacketByteBuf buffer)
		throws IOException
	{
		buffer.writeString(this.slotType.fullName);
		buffer.writeByte(this.index);
	}
}
