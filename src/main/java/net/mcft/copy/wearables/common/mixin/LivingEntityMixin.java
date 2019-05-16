package net.mcft.copy.wearables.common.mixin;

import java.util.Collection;
import java.util.stream.Stream;

import net.fabricmc.fabric.api.util.NbtType;
import net.mcft.copy.wearables.api.IWearablesData;
import net.mcft.copy.wearables.api.IWearablesEntity;
import net.mcft.copy.wearables.api.IWearablesRegion;
import net.mcft.copy.wearables.api.IWearablesSlot;
import net.mcft.copy.wearables.api.IWearablesSlotType;
import net.mcft.copy.wearables.common.WearablesEntityData;
import net.mcft.copy.wearables.common.WearablesEntry;
import net.mcft.copy.wearables.common.impl.WearablesSlotImpl;
import net.mcft.copy.wearables.common.impl.WearablesSlotImplVanilla;
import net.mcft.copy.wearables.common.misc.NbtUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin
	extends Entity
	implements IWearablesEntity
	         , WearablesEntityData.IAccessor
{
	private LivingEntityMixin()
		{ super(null, null); }
	
	
	private WearablesEntityData _wearablesData;
	
	@Override
	public WearablesEntityData getWearablesData(boolean create)
	{
		if (this._wearablesData == null)
			this._wearablesData = new WearablesEntityData();
		return this._wearablesData;
	}
	
	
	// Writing / reading WearablesEntityData to / from NBT
	
	@Inject(method="writeCustomDataToTag", at=@At("HEAD"))
	public void writeCustomDataToTag(CompoundTag entityData, CallbackInfo info)
	{
		if (hasWearables()) entityData.put("wearables:data", this._wearablesData.serializeToTag());
	}
	
	@Inject(method="readCustomDataFromTag", at=@At("HEAD"))
	public void readCustomDataFromTag(CompoundTag entityData, CallbackInfo info)
	{
		this._wearablesData = entityData.containsKey("wearables:data")
			? NbtUtil.asValue(entityData.getList("wearables:data", NbtType.COMPOUND), new WearablesEntityData())
			: null;
	}
	
	
	// IWearablesEntity implementation
	
	@Override
	public Collection<IWearablesRegion> getWearablesRegions()
		{ return IWearablesData.INSTANCE.getRegions(); }
	
	@Override
	public Collection<IWearablesSlotType> getWearablesSlotTypes()
		{ return IWearablesData.INSTANCE.getSlotTypes(); }
	
	@Override
	public Collection<IWearablesSlotType> getWearablesSlotTypes(IWearablesRegion region)
	{
		if (region == null) throw new IllegalArgumentException("region is null");
		return region.getSlotTypes();
	}
	
	
	@Override
	public boolean hasWearables()
		{ return (this._wearablesData != null) && (this._wearablesData.getNumStacks() > 0); }
	
	
	@Override
	public IWearablesSlot getWearablesSlot(IWearablesSlotType slotType, int index)
	{
		if (slotType == null) throw new IllegalArgumentException("slotType is null");
		if (index < 0) throw new IllegalArgumentException("index is negative");
		// TODO: Throw if slotType is not valid for this entity.
		
		EquipmentSlot vanillaSlot = slotType.getVanilla();
		return ((vanillaSlot != null) && (index == 0))
			? new WearablesSlotImplVanilla((LivingEntity)(Object)this, slotType)
			: new WearablesSlotImpl((LivingEntity)(Object)this, slotType, index);
	}
	
	
	@Override
	public Stream<IWearablesSlot> getEquippedWearables()
	{
		return hasWearables()
			? this._wearablesData.getEntries().map(this::toSlot)
			: Stream.empty();
	}
	
	@Override
	public Stream<IWearablesSlot> getEquippedWearables(IWearablesRegion region)
	{
		if (region == null) throw new IllegalArgumentException("region is null");
		return getEquippedWearables()
			.filter(slot -> (slot.getRegion() == region));
	}
	
	@Override
	public Stream<IWearablesSlot> getEquippedWearables(IWearablesSlotType slotType)
	{
		if (slotType == null) throw new IllegalArgumentException("slotType is null");
		return hasWearables()
			? this._wearablesData.getEntries(slotType.getFullName()).map(this::toSlot)
			: Stream.empty();
	}
	
	
	private IWearablesSlot toSlot(WearablesEntry entry)
		{ return getWearablesSlot(IWearablesData.INSTANCE.getSlotType(entry.slotTypeName), entry.index); }
}
