package net.mcft.copy.wearables.common;

import java.io.IOException;

import net.mcft.copy.wearables.common.misc.INbtSerializable;
import net.mcft.copy.wearables.common.misc.NbtUtil;
import net.mcft.copy.wearables.common.network.INetSerializable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;

public final class WearablesEntry
	implements INbtSerializable<CompoundTag>
	         , INetSerializable
{
	public String slotTypeName;
	public int index;
	public ItemStack stack;
	
	public WearablesEntry() {  }
	public WearablesEntry(WearablesSlot slot)
		{ this(slot.getSlotType().fullName, slot.getIndex(), slot.get()); }
	public WearablesEntry(String slotTypeName, int index, ItemStack stack)
		{ this.slotTypeName = slotTypeName; this.index = index; this.stack = stack; }
	
	public static WearablesEntry createFromBuffer(PacketByteBuf buffer) throws IOException
		{ WearablesEntry entry = new WearablesEntry(); entry.read(buffer); return entry; }
	
	
	// INbtSerializable implementation
	
	@Override
	public CompoundTag serializeToTag()
	{
		return NbtUtil.createCompound(
			"slotType", slotTypeName,
			"index", index,
			"stack", stack);
	}
	
	@Override
	public void deserializeFromTag(CompoundTag tag)
	{
		this.slotTypeName = tag.getString("slotType");
		this.index        = tag.getByte("index");
		this.stack        = ItemStack.fromTag(tag.getCompound("stack"));
	}
	
	
	// INetworkSerializable implementation
	
	@Override
	public void read(PacketByteBuf buffer)
		throws IOException
	{
		this.slotTypeName = buffer.readString();
		this.index        = buffer.readByte();
		this.stack        = buffer.readItemStack();
	}
	
	@Override
	public void write(PacketByteBuf buffer)
		throws IOException
	{
		buffer.writeString(this.slotTypeName);
		buffer.writeByte(this.index);
		buffer.writeItemStack(this.stack);
	}
}