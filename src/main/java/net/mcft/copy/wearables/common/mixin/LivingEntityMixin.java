package net.mcft.copy.wearables.common.mixin;

import java.util.Collection;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.mcft.copy.wearables.api.IWearablesEntity;
import net.mcft.copy.wearables.api.IWearablesSlot;
import net.mcft.copy.wearables.api.WearablesAPI;
import net.mcft.copy.wearables.api.WearablesRegion;
import net.mcft.copy.wearables.api.WearablesSlotType;
import net.mcft.copy.wearables.common.WearablesEntry;
import net.mcft.copy.wearables.common.WearablesMap;
import net.mcft.copy.wearables.common.WearablesSlot;
import net.mcft.copy.wearables.common.WearablesSlotVanilla;
import net.mcft.copy.wearables.common.WearablesMap.ByIndex;
import net.mcft.copy.wearables.common.WearablesMap.BySlotType;
import net.mcft.copy.wearables.common.misc.NbtUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin
	extends Entity
	implements IWearablesEntity, WearablesMap.IAccessor
{
	private LivingEntityMixin()
		{ super(null, null); }
	
	
	// WearablesMap.IAccessor implementation
	
	private WearablesMap _wearables;
	
	@Override
	public WearablesMap getWearablesMap(boolean create)
	{
		if (this._wearables == null)
			this._wearables = new WearablesMap();
		return this._wearables;
	}
	
	
	// Writing / reading Wearables data to / from NBT.
	
	@Inject(method="writeCustomDataToTag", at=@At("HEAD"))
	public void writeCustomDataToTag(CompoundTag entityData, CallbackInfo info)
	{
		if (this._wearables == null) return;
		ListTag slots = getEquippedWearables()
			.filter(WearablesSlot.class::isInstance)
			.map(WearablesSlot.class::cast)
			.map(WearablesEntry::new)
			.collect(NbtUtil.toList());
		if (slots.size() > 0)
			NbtUtil.set(entityData, slots, "wearables:map", "slots");
	}
	
	@Inject(method="readCustomDataFromTag", at=@At("HEAD"))
	public void readCustomDataFromTag(CompoundTag entityData, CallbackInfo info)
	{
		if (!entityData.containsKey("wearables:map")) return;
		if (this._wearables != null) this._wearables.clear();
		else this._wearables = new WearablesMap();
		
		ListTag list = NbtUtil.getList(entityData, "wearables:map", "slots");
		for (WearablesEntry entry : NbtUtil.asList(list, WearablesEntry::new)) {
			WearablesSlotType slotType = WearablesAPI.findSlotType(entry.slotTypeName);
			if (slotType == null) { throw new RuntimeException("Slot type '" + entry.slotTypeName + "' not found"); }
			this._wearables.set(slotType, entry.index, entry.stack);
		}
	}
	
	
	// IWearablesEntity implementation
	
	@Override
	public boolean hasWearables()
		{ return (_wearables != null) && getEquippedWearables().findAny().isPresent(); }
	
	
	@Override
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
	
	@Override
	public Stream<IWearablesSlot> getWearablesSlots()
		{ return WearablesAPI.getRegions().stream().flatMap(this::getWearablesSlots); }
	
	@Override
	public Stream<IWearablesSlot> getWearablesSlots(WearablesRegion region)
	{
		if (region == null) throw new IllegalArgumentException("region is null");
		return wearables_getSlots(wearables_getEnabledSlotTypes(region.getChildren()));
	}
	
	@Override
	public Stream<IWearablesSlot> getWearablesSlots(WearablesSlotType slotType)
	{
		if (slotType == null) throw new IllegalArgumentException("slotType is null");
		return wearables_getSlots(wearables_getEnabledSlotTypes(slotType));
	}
	
	
	@Override
	public Stream<IWearablesSlot> getEquippedWearables()
	{
		if (this._wearables == null) return Stream.empty();
		return this._wearables.values().stream().flatMap(
			bySlotType -> bySlotType.entrySet().stream().flatMap(
				bySlotTypeEntry -> bySlotTypeEntry.getValue().entrySet().stream().map(
					byIndexEntry -> getWearablesSlot(bySlotTypeEntry.getKey(), byIndexEntry.getKey()))));
	}
	
	@Override
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
	
	@Override
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
	
	
	// Utility methods
	
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
