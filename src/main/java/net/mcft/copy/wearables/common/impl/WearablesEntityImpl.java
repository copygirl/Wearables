package net.mcft.copy.wearables.common.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.mcft.copy.wearables.api.IWearablesEntity;
import net.mcft.copy.wearables.api.IWearablesItemHandler;
import net.mcft.copy.wearables.api.IWearablesSlot;
import net.mcft.copy.wearables.api.IWearablesSlotHandler;
import net.mcft.copy.wearables.api.WearablesRegion;
import net.mcft.copy.wearables.api.WearablesSlotType;
import net.mcft.copy.wearables.common.WearablesEntityData;
import net.mcft.copy.wearables.common.WearablesSlotKey;
import net.mcft.copy.wearables.common.data.EntityTypeData;
import net.mcft.copy.wearables.common.data.WearablesData;
import net.mcft.copy.wearables.common.data.EntityTypeData.SlotTypeData;
import net.mcft.copy.wearables.common.data.WearablesData.ItemData;
import net.mcft.copy.wearables.common.impl.slot.DefaultSlotHandler;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;

public class WearablesEntityImpl
	implements IWearablesEntity
{
	public final Entity entity;
	
	public WearablesEntityImpl(Entity entity)
	{
		if (entity == null) throw new IllegalArgumentException("entity is null");
		this.entity = entity;
	}
	
	
	// IWearablesEntity implementation
	
	@Override
	public boolean hasWearables()
		{ return !WearablesEntityData.from(this.entity, false).isEmpty(); }
	
	@Override
	public Collection<WearablesRegion> getWearablesRegions()
		{ return Collections.unmodifiableSet(EntityTypeData.from(this.entity).regions); }
	
	
	@Override
	public Stream<IWearablesSlot> getEquippedWearables()
		{ return getEquippedWearables(entry -> true); }
	
	@Override
	public Stream<IWearablesSlot> getEquippedWearables(WearablesRegion region)
	{
		if (region == null) throw new IllegalArgumentException("region is null");
		Set<WearablesSlotType> slotTypes = EntityTypeData.from(this.entity).slotTypesByRegion.get(region);
		if (slotTypes == null) return Stream.empty();
		return getEquippedWearables(entry -> slotTypes.contains(entry.getKey().slotType));
	}
	
	@Override
	public Stream<IWearablesSlot> getEquippedWearables(WearablesSlotType slotType)
		{ return getEquippedWearables(entry -> slotType.equals(entry.getKey().slotType)); }
	
	private Stream<IWearablesSlot> getEquippedWearables(
		Predicate<Map.Entry<WearablesSlotKey, WearablesEntityData.Entry>> predicate)
	{
		return WearablesEntityData.from(this.entity, false).map.entrySet().stream().filter(predicate)
			.map(entry -> getWearablesSlot(entry.getKey().slotType, entry.getKey().index, true));
	}
	
	
	@Override
	public Collection<WearablesSlotType> getSupportedWearablesSlotTypes()
		{ return Collections.unmodifiableSet(EntityTypeData.from(this.entity).slotTypes.keySet()); }
	
	@Override
	public Collection<WearablesSlotType> getSupportedWearablesSlotTypes(WearablesRegion region)
	{
		if (region == null) throw new IllegalArgumentException("region is null");
		return EntityTypeData.from(this.entity).slotTypesByRegion.getOrDefault(region, Collections.emptySet());
	}
	
	
	@Override
	public Stream<IWearablesSlot> getSupportedWearablesSlots()
	{
		return EntityTypeData.from(this.entity).slotTypes.keySet()
			.stream().flatMap(this::getSupportedWearablesSlots);
	}
	
	@Override
	public Stream<IWearablesSlot> getSupportedWearablesSlots(WearablesRegion region)
	{
		if (region == null) throw new IllegalArgumentException("region is null");
		Set<WearablesSlotType> slotTypes = EntityTypeData.from(this.entity).slotTypesByRegion.get(region);
		if (slotTypes == null) return Stream.empty();
		return slotTypes.stream().flatMap(this::getSupportedWearablesSlots);
	}
	
	// FIXME: Fix this not returning multiple slots for some reason.
	@Override
	public Stream<IWearablesSlot> getSupportedWearablesSlots(WearablesSlotType slotType)
	{
		if (slotType == null) throw new IllegalArgumentException("slotType is null");
		SlotTypeData slotTypeData = EntityTypeData.from(this.entity).slotTypes.get(slotType);
		if (slotTypeData == null) return Stream.empty();
		return IntStream.range(0, slotTypeData.slotCount)
		                .mapToObj(i -> getWearablesSlot(slotType, i));
	}
	
	
	@Override
	public Collection<WearablesSlotType> getValidSlotTypes(Item item)
	{
		if (item == null) throw new IllegalArgumentException("item is null");
		
		ItemData itemData = IWearablesItemHandler.REGISTRY.stream()
			.map(handler -> handler.getSpecialItem(item))
			.filter(Optional::isPresent).map(Optional::get).findFirst()
			.flatMap(specialItem -> Optional.ofNullable(WearablesData.INSTANCE.specialItems.get(specialItem)))
			.orElseGet(() -> WearablesData.INSTANCE.items.get(item));
		if (itemData == null) return Collections.emptySet();
		
		Set<WearablesSlotType> result = new HashSet<>();
		Collection<WearablesSlotType> supportedSlotTypes = getSupportedWearablesSlotTypes();
		
		for (WearablesSlotType slotType : itemData.validSlots) {
			if (supportedSlotTypes.contains(slotType))
				result.add(slotType);
			else supportedSlotTypes.stream()
				.filter(slotType::matches)
				.max(Comparator.comparingInt(s -> s.fullName.length()))
				.ifPresent(result::add);
		}
		
		return result;
	}
	
	@Override
	public Collection<IWearablesSlot> getValidSlots(Item item)
	{
		return getValidSlotTypes(item).stream()
			.flatMap(this::getSupportedWearablesSlots)
			.collect(Collectors.toList());
	}
	
	
	@Override
	public IWearablesSlot getWearablesSlot(WearablesSlotType slotType, int index, boolean force)
	{
		if (slotType == null) throw new IllegalArgumentException("slotType is null");
		if (index < 0) throw new IllegalArgumentException("index is negative");
		
		SlotTypeData slotTypeData = EntityTypeData.from(this.entity).slotTypes.get(slotType);
		
		if (!force) {
			if (slotTypeData == null) throw new UnsupportedOperationException(
				"slotType '" + slotType + "' is not supported for entity '" + this.entity + "'");
			if (index >= slotTypeData.slotCount) throw new UnsupportedOperationException(
				"index " + index + " is out of range for slot type '" + slotType + "'");
		} else if ((slotTypeData == null) && !IWearablesEntity.is(this.entity))
			throw new UnsupportedOperationException("Entity '" + this.entity + "' doesn't support Wearables.");
		
		IWearablesSlotHandler<Entity> slotHandler = (slotTypeData != null)
			? slotTypeData.handler : DefaultSlotHandler.INSTANCE;
		return new WearablesSlotImpl(this.entity, slotType, index, slotHandler);
	}
}
