package net.mcft.copy.wearables.common;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import net.mcft.copy.wearables.WearablesCommon;
import net.mcft.copy.wearables.api.IWearablesContainer;
import net.mcft.copy.wearables.api.IWearablesContainerId;
import net.mcft.copy.wearables.api.IWearablesEntity;
import net.mcft.copy.wearables.api.IWearablesSlot;
import net.mcft.copy.wearables.api.WearablesContainerSlot;
import net.mcft.copy.wearables.common.data.ContainerData;
import net.mcft.copy.wearables.common.data.WearablesData;
import net.mcft.copy.wearables.common.mixin.container.ContainerAccessor;

import net.minecraft.container.Container;
import net.minecraft.container.Slot;
import net.minecraft.entity.Entity;

public class WearablesContainerData
	implements IWearablesContainer
{
	public interface IAccessor
		{ public WearablesContainerData getWearablesData(boolean create); }
	
	public static WearablesContainerData from(Container entity)
		{ return ((IAccessor)entity).getWearablesData(true); }
	
	
	private final Container _container;
	private List<RegionEntry> _regions;
	
	public Container getContainer() { return this._container; }
	
	
	public WearablesContainerData(Container container)
		{ this._container = container; }
	
	
	public void computeAndAddRegionEntries()
	{
		if (!(this._container instanceof IWearablesContainerId)) return;
		IWearablesContainerId containerId = (IWearablesContainerId)this._container;
		ContainerData data = WearablesData.INSTANCE.containers.get(containerId.getWearablesIdentifier());
		if (data == null) return;
		
		setRegionEntries(data.entries.stream()
			.map(entry -> {
				Entity entity = Optional.ofNullable(entry.entityKey)
					.map(key -> containerId.getWearablesEntityMap().get(key))
					.orElseGet(() -> containerId.getWearablesDefaultEntity());
				if (entity == null) {
					WearablesCommon.LOGGER.warn("[wearables:WearablesContainerData] Unknown entity key '{}'", entry.entityKey);
					return null;
				}
				IWearablesEntity wearablesEntity = IWearablesEntity.from(entity);
				SortedSet<IWearablesSlot> sortedSlots = new TreeSet<>();
				wearablesEntity.getSupportedWearablesSlots(entry.region).forEach(sortedSlots::add);
				wearablesEntity.getEquippedWearables(entry.region).forEach(sortedSlots::add);
				if (sortedSlots.size() == 0) return null;
				List<WearablesContainerSlot> containerSlots = sortedSlots.stream()
					.map(WearablesContainerSlot::new)
					.collect(Collectors.toList());
				return new RegionEntry(entity, entry.position, entry.region, containerSlots);
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList()));
	}
	
	public void setRegionEntries(List<RegionEntry> regions)
	{
		ContainerAccessor accessor = (ContainerAccessor)this._container;
		this._regions = regions;
		for (RegionEntry entry : this._regions) {
			Optional<Slot> existingSlot = this._container.slotList.stream()
				.filter(slot -> (slot.xPosition == entry.position.x)
				             && (slot.yPosition == entry.position.y))
				.findFirst();
			
			entry.centerSlot.xPosition = entry.position.x;
			entry.centerSlot.yPosition = entry.position.y;
			if (existingSlot.isPresent()) {
				entry.centerSlot.id = existingSlot.get().id;
				this._container.slotList.set(existingSlot.get().id, entry.centerSlot);
			}
			
			for (WearablesContainerSlot newSlot : entry.slots)
				if ((newSlot != entry.centerSlot) || !existingSlot.isPresent())
					accessor.invokeAddSlot(newSlot);
		}
	}
	
	
	// IWearablesContainer implementation
	
	@Override
	public Collection<RegionEntry> getRegions()
		{ return this._regions; }
}
