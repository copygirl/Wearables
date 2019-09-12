package net.mcft.copy.wearables.common.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
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
import net.minecraft.entity.Entity;

public class WearablesContainerImpl
	implements IWearablesContainer
{
	private final Container _container;
	private final ContainerAccessor _accessor;
	private final Optional<IWearablesContainerId> _containerId;
	private final Optional<ContainerData> _data;
	private final List<RegionEntry> _regions;
	
	public WearablesContainerImpl(Container container)
	{
		if (container == null) throw new IllegalArgumentException("container is null");
		
		this._container = container;
		this._accessor  = (ContainerAccessor)container;
		
		this._containerId = Optional.ofNullable(
			(container instanceof IWearablesContainerId)
				? (IWearablesContainerId)container : null);
		
		this._data = this._containerId
			.map(id -> WearablesData.INSTANCE.containers.get(id.getWearablesIdentifier()))
			.filter(Objects::nonNull);
		
		this._regions = computeRegionEntries();
	}
	
	private List<RegionEntry> computeRegionEntries()
	{
		if (!this._data.isPresent()) return Collections.emptyList();
		return this._data.get().entries.stream().map(entry -> {
			Entity entity = Optional.ofNullable(entry.entityKey)
				.map(key -> this._containerId.get().getWearablesEntityMap().get(key))
				.orElseGet(() -> this._containerId.get().getWearablesDefaultEntity());
			
			if (entity == null) {
				WearablesCommon.LOGGER.warn("[wearables:WearablesRegionPopup] Unknown entity key '{}'", entry.entityKey);
				return null;
			}
			
			IWearablesEntity wearablesEntity = IWearablesEntity.from(entity);
			SortedSet<IWearablesSlot> sortedSlots = new TreeSet<>();
			wearablesEntity.getSupportedWearablesSlots(entry.region).forEach(sortedSlots::add);
			wearablesEntity.getEquippedWearables(entry.region).forEach(sortedSlots::add);
			
			AtomicInteger index = new AtomicInteger();
			List<WearablesContainerSlot> containerSlots = sortedSlots.stream()
				.map(slot -> new WearablesContainerSlot(slot,
					entry.position.x + 4,
					entry.position.y + 4 + index.getAndIncrement() * 18))
				.collect(Collectors.toList());
			
			return new RegionEntry(entity, entry.position, entry.region, containerSlots);
		})
		.filter(Objects::nonNull)
		.collect(Collectors.toList());
	}
	
	public void addWearablesSlots()
	{
		this._regions.stream()
			.flatMap(region -> region.slots.stream())
			.forEach(this._accessor::invokeAddSlot);
	}
	
	// IWearablesContainer implementation
	
	@Override
	public Collection<RegionEntry> getRegions() { return this._regions; }
}
