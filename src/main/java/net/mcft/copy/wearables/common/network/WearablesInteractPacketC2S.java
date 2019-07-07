package net.mcft.copy.wearables.common.network;

import java.io.IOException;

import net.mcft.copy.wearables.WearablesCommon;
import net.mcft.copy.wearables.api.IWearablesSlot;
import net.mcft.copy.wearables.api.WearablesSlotType;
import net.mcft.copy.wearables.common.InteractionHandler.Action;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public class WearablesInteractPacketC2S implements IPacket
{
	public static final Identifier ID = new Identifier(WearablesCommon.MOD_ID, "interact");
	@Override public Identifier getID() { return ID; }
	
	
	public Action action;
	public WearablesSlotType slotType;
	public int index;
	public ItemStack clientCursorStack;
	
	public WearablesInteractPacketC2S() {  }
	public WearablesInteractPacketC2S(Action action, IWearablesSlot slot, ItemStack clientCursorStack)
	{
		if (action == null) throw new IllegalArgumentException("action is null");
		if (slot == null) throw new IllegalArgumentException("slot is null");
		if (clientCursorStack == null) throw new IllegalArgumentException("clientCursorStack is null");
		if (slot.getIndex() > 127) throw new IllegalArgumentException("index is greater than 127");
		this.action   = action;
		this.slotType = slot.getSlotType();
		this.index    = slot.getIndex();
		this.clientCursorStack = clientCursorStack;
	}
	
	@Override
	public void read(PacketByteBuf buffer)
		throws IOException
	{
		this.action   = Action.values()[buffer.readByte()];
		this.slotType = new WearablesSlotType(buffer.readString());
		this.index    = buffer.readByte();
		this.clientCursorStack = buffer.readItemStack();
	}
	
	@Override
	public void write(PacketByteBuf buffer)
		throws IOException
	{
		buffer.writeByte(this.action.ordinal());
		buffer.writeString(this.slotType.fullName);
		buffer.writeByte(this.index);
		buffer.writeItemStack(this.clientCursorStack);
	}
}
