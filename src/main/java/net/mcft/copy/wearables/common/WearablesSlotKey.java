package net.mcft.copy.wearables.common;

import net.mcft.copy.wearables.api.WearablesSlotType;
import net.mcft.copy.wearables.common.misc.INbtDeserializer;
import net.mcft.copy.wearables.common.misc.INbtSerializable;

import net.minecraft.nbt.CompoundTag;

public final class WearablesSlotKey
	implements INbtSerializable<CompoundTag>
{
	public WearablesSlotType slotType;
	public int index;
	
	public WearablesSlotKey(WearablesSlotType slotType, int index)
	{
		if (slotType == null) throw new IllegalArgumentException("slotType is null");
		if (index < 0) throw new IllegalArgumentException("index is negative");
		if (index > 127) throw new IllegalArgumentException("index is greater than 127");
		this.slotType = slotType;
		this.index    = index;
	}
	
	
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof WearablesSlotKey)) return false;
		WearablesSlotKey key = (WearablesSlotKey)obj;
		return key.slotType.equals(this.slotType)
		    && (key.index == this.index);
	}
	
	@Override
	public int hashCode()
		{ return this.slotType.hashCode() ^ index; }
	
	
	// INbtSerializable / INbtDeserializer implementation
	
	public static final String SLOT_TYPE_TAG = "slotType";
	public static final String INDEX_TAG     = "index";
	
	@Override
	public CompoundTag createTag()
		{ return new CompoundTag(); }
	
	@Override
	public void serializeToTag(CompoundTag tag)
	{
		tag.putString(SLOT_TYPE_TAG, this.slotType.toString());
		tag.putByte(INDEX_TAG, (byte)this.index);
	}
	
	public static final INbtDeserializer<WearablesSlotKey, CompoundTag> DESERIALIZER =
		new INbtDeserializer<WearablesSlotKey, CompoundTag>()
		{
			@Override
			public WearablesSlotKey deserializeFromTag(CompoundTag tag)
			{
				return new WearablesSlotKey(
					new WearablesSlotType(tag.getString(SLOT_TYPE_TAG)),
					tag.getByte(INDEX_TAG));
			}
		};
}
