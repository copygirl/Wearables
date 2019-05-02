package net.mcft.copy.wearables.common;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import net.mcft.copy.wearables.common.misc.INbtSerializable;
import net.mcft.copy.wearables.common.misc.NbtUtil;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListTag;

public class WearablesEntityData
	implements INbtSerializable<ListTag>
{
	public interface IAccessor
		{ public WearablesEntityData getWearablesData(boolean create); }
	
	
	private final Map<String, Map<Integer, ItemStack>> _map = new HashMap<>();
	private int _numStacks = 0;
	
	
	public int getNumStacks() { return _numStacks; }
	
	public ItemStack get(String slotTypeName, int index)
	{
		Map<Integer, ItemStack> byIndex = this._map.get(slotTypeName);
		if (byIndex == null) return ItemStack.EMPTY;
		ItemStack stack = byIndex.get(index);
		return (stack != null) ? stack : ItemStack.EMPTY;
	}
	
	public void set(String slotTypeName, int index, ItemStack stack)
	{
		if (stack == null) throw new IllegalArgumentException("stack is null");
		Map<Integer, ItemStack> byIndex = this._map.get(slotTypeName);
		if (byIndex == null) this._map.put(slotTypeName, byIndex = new HashMap<>());
		
		if (!stack.isEmpty()) { if (byIndex.put(index, stack) == null) _numStacks++; }
		else if (byIndex.remove(index) != null) _numStacks--;
	}
	
	public Stream<WearablesEntry> getEntries()
	{
		return this._map.entrySet().stream()
			.flatMap(bySlotType -> bySlotType.getValue().entrySet().stream()
			.map(byIndex -> new WearablesEntry(bySlotType.getKey(), byIndex.getKey(), byIndex.getValue())));
	}
	
	public Stream<WearablesEntry> getEntries(String slotTypeName)
	{
		if ((slotTypeName == null) || slotTypeName.isEmpty())
			throw new IllegalArgumentException("slotTypeName is null or empty");
		Map<Integer, ItemStack> bySlotType = this._map.get(slotTypeName);
		return (bySlotType != null)
			? bySlotType.entrySet().stream().map(byIndex ->
				new WearablesEntry(slotTypeName, byIndex.getKey(), byIndex.getValue()))
			: Stream.empty();
	}
	
	
	// INbtSerializable implementation
	
	@Override
	public ListTag serializeToTag()
		{ return getEntries().collect(NbtUtil.toList()); }
	
	@Override
	public void deserializeFromTag(ListTag tag)
	{
		this._map.clear();
		this._numStacks = 0;
		for (WearablesEntry entry : NbtUtil.asList(tag, WearablesEntry::new))
			set(entry.slotTypeName, entry.index, entry.stack);
	}
}
