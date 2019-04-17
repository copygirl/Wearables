package net.mcft.copy.wearables.common;

import java.util.HashMap;

import net.mcft.copy.wearables.api.WearablesRegion;
import net.mcft.copy.wearables.api.WearablesSlotType;

import net.minecraft.item.ItemStack;

public class WearablesMap
	extends HashMap<WearablesRegion, WearablesMap.BySlotType>
{
	private static final long serialVersionUID = 1L;
	
	public ItemStack get(WearablesSlotType slotType, int index)
	{
		BySlotType bySlotType = get(slotType.region);
		if (bySlotType == null) return ItemStack.EMPTY;
		ByIndex byIndex = bySlotType.get(slotType);
		if (byIndex == null) return ItemStack.EMPTY;
		ItemStack stack = byIndex.get(index);
		return (stack != null) ? stack : ItemStack.EMPTY;
	}
	
	public void set(WearablesSlotType slotType, int index, ItemStack stack)
	{
		if (stack == null) throw new IllegalArgumentException("stack is null");
		BySlotType bySlotType = get(slotType.region);
		if (bySlotType == null) put(slotType.region, bySlotType = new BySlotType());
		ByIndex byIndex = bySlotType.get(slotType);
		if (byIndex == null) bySlotType.put(slotType, byIndex = new ByIndex());
		
		if (!stack.isEmpty()) byIndex.put(index, stack);
		else byIndex.remove(index);
	}
	
	
	public class BySlotType
		extends HashMap<WearablesSlotType, WearablesMap.ByIndex>
			{ private static final long serialVersionUID = 1L; }
	
	public class ByIndex
		extends HashMap<Integer, ItemStack>
			{ private static final long serialVersionUID = 1L; }
	
	public interface IAccessor
		{ public WearablesMap getWearablesMap(boolean create); }
}
