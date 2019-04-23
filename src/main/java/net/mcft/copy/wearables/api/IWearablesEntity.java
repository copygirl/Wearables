package net.mcft.copy.wearables.api;

import java.util.stream.Stream;

public interface IWearablesEntity
{
	public boolean hasWearables();
	
	
	public IWearablesSlot getWearablesSlot(WearablesSlotType slotType, int index);
	
	
	public Stream<IWearablesSlot> getWearablesSlots();
	
	public Stream<IWearablesSlot> getWearablesSlots(WearablesRegion region);
	
	public Stream<IWearablesSlot> getWearablesSlots(WearablesSlotType slotType);
	
	
	public Stream<IWearablesSlot> getEquippedWearables();
	
	public Stream<IWearablesSlot> getEquippedWearables(WearablesRegion region);
	
	public Stream<IWearablesSlot> getEquippedWearables(WearablesSlotType slotType);
}
