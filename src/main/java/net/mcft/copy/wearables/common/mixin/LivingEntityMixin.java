package net.mcft.copy.wearables.common.mixin;

import java.util.Collection;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.mcft.copy.wearables.api.IWearablesEntity;
import net.mcft.copy.wearables.api.IWearablesSlot;
import net.mcft.copy.wearables.api.WearablesAPI;
import net.mcft.copy.wearables.api.WearablesRegion;
import net.mcft.copy.wearables.api.WearablesSlotType;
import net.mcft.copy.wearables.common.WearablesMap;
import net.mcft.copy.wearables.common.WearablesSlot;
import net.mcft.copy.wearables.common.WearablesSlotVanilla;
import net.mcft.copy.wearables.common.WearablesMap.ByIndex;
import net.mcft.copy.wearables.common.WearablesMap.BySlotType;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin
	extends Entity
	implements IWearablesEntity, WearablesMap.IAccessor
{
	private LivingEntityMixin()
		{ super(null, null); }
	
	
	private WearablesMap _wearables;
	
	public WearablesMap getWearablesMap(boolean create)
	{
		if (this._wearables == null)
			this._wearables = new WearablesMap();
		return this._wearables;
	}
	
	
	public IWearablesSlot getWearablesSlot(WearablesSlotType slotType, int index)
	{
		if (slotType == null) throw new IllegalArgumentException("slotType is null");
		if (index < 0) throw new IllegalArgumentException("index is negative");
		
		EquipmentSlot vanillaSlot = slotType.getVanilla();
		return ((vanillaSlot != null) && (index == 0))
			? new WearablesSlotVanilla((LivingEntity)(Object)this, slotType)
			: new WearablesSlot((LivingEntity)(Object)this, slotType, index);
	}
	
	
	// TODO: Should different types of entities have different slots?
	
	public Stream<IWearablesSlot> getWearablesSlots()
	{
		return WearablesAPI.getRegions().stream().flatMap(this::getWearablesSlots);
	}
	
	public Stream<IWearablesSlot> getWearablesSlots(WearablesRegion region)
	{
		if (region == null) throw new IllegalArgumentException("region is null");
		return wearables_getSlots(wearables_getEnabledSlotTypes(region.getChildren()));
	}
	
	public Stream<IWearablesSlot> getWearablesSlots(WearablesSlotType slotType)
	{
		if (slotType == null) throw new IllegalArgumentException("slotType is null");
		return wearables_getSlots(wearables_getEnabledSlotTypes(slotType));
	}
	
	
	public Stream<IWearablesSlot> getEquippedWearables()
	{
		if (this._wearables == null) return Stream.empty();
		return this._wearables.values().stream().flatMap(
			bySlotType -> bySlotType.entrySet().stream().flatMap(
				bySlotTypeEntry -> bySlotTypeEntry.getValue().entrySet().stream().map(
					byIndexEntry -> getWearablesSlot(bySlotTypeEntry.getKey(), byIndexEntry.getKey()))));
	}
	
	public Stream<IWearablesSlot> getEquippedWearables(WearablesRegion region)
	{
		if (region == null) throw new IllegalArgumentException("region is null");
		
		if (this._wearables == null) return Stream.empty();
		BySlotType bySlotType = this._wearables.get(region);
		if (bySlotType == null) return Stream.empty();
		
		return bySlotType.entrySet().stream().flatMap(
			bySlotTypeEntry -> bySlotTypeEntry.getValue().entrySet().stream().map(
				byIndexEntry -> getWearablesSlot(bySlotTypeEntry.getKey(), byIndexEntry.getKey())));
	}
	
	public Stream<IWearablesSlot> getEquippedWearables(WearablesSlotType slotType)
	{
		if (slotType == null) throw new IllegalArgumentException("slotType is null");
		
		if (this._wearables == null) return Stream.empty();
		BySlotType bySlotType = this._wearables.get(slotType.region);
		if (bySlotType == null) return Stream.empty();
		ByIndex byIndex = bySlotType.get(slotType);
		if (byIndex == null) return Stream.empty();
		
		return byIndex.entrySet().stream().map(
			byIndexEntry -> getWearablesSlot(slotType, byIndexEntry.getKey()));
	}
	
	
	private Stream<WearablesSlotType> wearables_getEnabledSlotTypes(WearablesSlotType slotType)
	{
		return slotType.isEnabled()
			? Stream.concat(Stream.of(slotType), wearables_getEnabledSlotTypes(slotType.getChildren()))
			: wearables_getEnabledSlotTypes(slotType.getChildren());
	}
	
	private Stream<WearablesSlotType> wearables_getEnabledSlotTypes(Collection<WearablesSlotType> slotTypes)
		{ return slotTypes.stream().flatMap(this::wearables_getEnabledSlotTypes); }
	
	private Stream<IWearablesSlot> wearables_getSlots(Stream<WearablesSlotType> slotTypes)
	{
		return slotTypes.flatMap(slotType ->
			IntStream.range(0, slotType.getNumSlots())
			         .mapToObj(i -> getWearablesSlot(slotType, i)));
	}
}
