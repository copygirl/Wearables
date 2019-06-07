package net.mcft.copy.wearables.common;

import java.io.IOException;

import net.mcft.copy.wearables.api.IWearablesSlot;
import net.mcft.copy.wearables.common.misc.INbtDeserializer;
import net.mcft.copy.wearables.common.misc.INbtSerializable;
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
	public WearablesEntry(IWearablesSlot slot)
		{ this(slot.getSlotType().getFullName(), slot.getIndex(), slot.get()); }
	public WearablesEntry(String slotTypeName, int index, ItemStack stack)
		{ this.slotTypeName = slotTypeName; this.index = index; this.stack = stack; }
	
	public static WearablesEntry createFromBuffer(PacketByteBuf buffer) throws IOException
		{ WearablesEntry entry = new WearablesEntry(); entry.read(buffer); return entry; }
	
	
	// INbtSerializable / INbtDeserializer implementation
	
	public static final String SLOT_TYPE_TAG = "slotType";
	public static final String INDEX_TAG     = "index";
	public static final String STACK_TAG     = "stack";
	
	@Override
	public CompoundTag createTag()
		{ return new CompoundTag(); }
	
	@Override
	public void serializeToTag(CompoundTag tag)
	{
		tag.putString(SLOT_TYPE_TAG, slotTypeName);
		tag.putByte(INDEX_TAG, (byte)index);
		tag.put(STACK_TAG, stack.toTag(new CompoundTag()));
	}
	
	public static final INbtDeserializer<WearablesEntry, CompoundTag> DESERIALIZER =
		new INbtDeserializer<WearablesEntry, CompoundTag>(){
			@Override
			public WearablesEntry deserializeFromTag(CompoundTag tag)
			{
				return new WearablesEntry(
					tag.getString(SLOT_TYPE_TAG),
					tag.getByte(INDEX_TAG),
					ItemStack.fromTag(tag.getCompound(STACK_TAG)));
			}
		};
	
	
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