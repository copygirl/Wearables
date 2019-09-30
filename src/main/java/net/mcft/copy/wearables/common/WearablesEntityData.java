package net.mcft.copy.wearables.common;

import java.util.HashMap;
import java.util.Map;

import net.fabricmc.fabric.api.util.NbtType;

import net.mcft.copy.wearables.api.WearablesSlotType;
import net.mcft.copy.wearables.common.misc.INbtDeserializer;
import net.mcft.copy.wearables.common.misc.INbtSerializable;
import net.mcft.copy.wearables.common.misc.NbtUtil;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class WearablesEntityData
	implements INbtSerializable<CompoundTag>
{
	public interface IAccessor
		{ public WearablesEntityData getWearablesData(boolean create); }
	
	public static WearablesEntityData from(Entity entity, boolean create)
	{
		WearablesEntityData data = ((IAccessor)entity).getWearablesData(create);
		return (data != null) ? data : DUMMY;
	}
	
	private static final WearablesEntityData DUMMY = new WearablesEntityData()
	{
		@Override
		public Entry get(WearablesSlotType slotType, int index, boolean create) {
			if (create) throw new UnsupportedOperationException();
			return DUMMY_ENTRY;
		}
		@Override
		public void remove(WearablesSlotType slotType, int index)
			{ throw new UnsupportedOperationException(); }
	};
	
	
	public final Map<WearablesSlotKey, Entry> map = new HashMap<>();
	
	
	public boolean isEmpty()
		{ return this.map.isEmpty(); }
	
	public Entry get(WearablesSlotType slotType, int index, boolean create)
	{
		WearablesSlotKey key = new WearablesSlotKey(slotType, index);
		Entry entry = this.map.get(key);
		if (entry == null) {
			if (!create) entry = DUMMY_ENTRY;
			else this.map.put(key, entry = new Entry());
		}
		return entry;
	}
	
	public void remove(WearablesSlotType slotType, int index)
		{ this.map.remove(new WearablesSlotKey(slotType, index)); }
	
	
	public final Entry DUMMY_ENTRY = new Entry(){
		@Override public void setStack(ItemStack value) { throw new UnsupportedOperationException(); }
		@Override public void resetEquippedTime() { throw new UnsupportedOperationException(); }
	};
	
	public static class Entry
		implements INbtSerializable<CompoundTag>
	{
		private ItemStack _stack  = ItemStack.EMPTY;
		private int _equippedTime = 0;
		
		public ItemStack getStack() { return this._stack; }
		public void setStack(ItemStack value)
		{
			if (value == null) throw new IllegalArgumentException("value is null");
			this._stack = value;
		}
		
		public int getEquippedTime() { return this._equippedTime; }
		public void resetEquippedTime() { this._equippedTime = 0; }
		
		// TODO: Allow storing extra non-stack data in a Wearables slot?
		
		
		// INbtSerializable / INbtDeserializer implementation
		
		public static final String STACK_TAG         = "stack";
		public static final String EQUIPPED_TIME_TAG = "equippedTime";
		
		@Override
		public CompoundTag createTag()
			{ return new CompoundTag(); }
		
		@Override
		public void serializeToTag(CompoundTag tag)
		{
			if (!this._stack.isEmpty()) tag.put(STACK_TAG, this._stack.toTag(new CompoundTag()));
			tag.putInt(EQUIPPED_TIME_TAG, this._equippedTime);
		}
		
		public static final INbtDeserializer<Entry, CompoundTag> DESERIALIZER =
			new INbtDeserializer<WearablesEntityData.Entry,CompoundTag>()
			{
				@Override
				public Entry deserializeFromTag(CompoundTag tag)
				{
					Entry entry = new Entry();
					if (tag.containsKey(STACK_TAG)) entry.setStack(ItemStack.fromTag(tag.getCompound(STACK_TAG)));
					entry._equippedTime = tag.getInt(EQUIPPED_TIME_TAG);
					return entry;
				}
			};
	}
	
	
	// INbtSerializable / INbtDeserializer implementation
	
	public static final int    VERSION     = 0;
	public static final String VERSION_TAG = "version";
	public static final String ENTRIES_TAG = "entries";
	
	@Override
	public CompoundTag createTag()
		{ return new CompoundTag(); }
	
	@Override
	public void serializeToTag(CompoundTag tag)
	{
		tag.putInt(VERSION_TAG, VERSION);
		tag.put(ENTRIES_TAG, this.map.entrySet().stream().map(e -> {
				CompoundTag compound = new CompoundTag();
				e.getKey().serializeToTag(compound);
				e.getValue().serializeToTag(compound);
				return compound;
			}).collect(NbtUtil.toList()));
	}
	
	public static final INbtDeserializer<WearablesEntityData, CompoundTag> DESERIALIZER =
		new INbtDeserializer<WearablesEntityData, CompoundTag>()
		{
			@Override
			public WearablesEntityData deserializeFromTag(CompoundTag tag)
			{
				WearablesEntityData data = new WearablesEntityData();
				
				int version = tag.getInt(VERSION_TAG);
				if (version != 0) throw new RuntimeException("Unknown version '" + version + "'");
				
				for (Tag t : tag.getList(ENTRIES_TAG, NbtType.COMPOUND)) {
					CompoundTag compound = (CompoundTag)t;
					WearablesSlotKey key = WearablesSlotKey.DESERIALIZER.deserializeFromTag(compound);
					Entry entry = Entry.DESERIALIZER.deserializeFromTag(compound);
					data.map.put(key, entry);
				}
				
				return data;
			}
		};
}
