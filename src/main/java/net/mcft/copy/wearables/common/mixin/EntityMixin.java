package net.mcft.copy.wearables.common.mixin;

import net.mcft.copy.wearables.api.IWearablesEntity;
import net.mcft.copy.wearables.common.WearablesEntityData;
import net.mcft.copy.wearables.common.impl.WearablesSlotImpl;
import net.mcft.copy.wearables.common.misc.NbtUtil;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// TODO: How to drop wearables on death? Because drop chance and such.
@Mixin(Entity.class)
public abstract class EntityMixin
	implements WearablesEntityData.IAccessor
{
	private WearablesEntityData _wearables_data;
	
	@Override
	public WearablesEntityData getWearablesData(boolean create)
	{
		if (create) {
			if (!IWearablesEntity.is((Entity)(Object)this))
				throw new UnsupportedOperationException();
			if (this._wearables_data == null)
				this._wearables_data = new WearablesEntityData();
		}
		return this._wearables_data;
	}
	
	
	// Writing / reading WearablesEntityData to / from NBT
	
	private static final String WEARABLES_DATA_TAG = "wearables:data";
	
	@Inject(method="toTag", at=@At("TAIL"))
	public void toTag(CompoundTag entityData, CallbackInfoReturnable<CompoundTag> info)
	{
		if ((this._wearables_data != null) && !this._wearables_data.isEmpty())
			entityData.put(WEARABLES_DATA_TAG, this._wearables_data.serializeToTag());
	}
	
	@Inject(method="fromTag", at=@At("TAIL"))
	public void fromTag(CompoundTag entityData, CallbackInfo info)
	{
		this._wearables_data = entityData.containsKey(WEARABLES_DATA_TAG)
			? NbtUtil.asValue(entityData.getCompound(WEARABLES_DATA_TAG), WearablesEntityData.DESERIALIZER)
			: null;
	}
	
	
	// Updating equipped items
	
	@Inject(method="tick", at=@At("TAIL"))
	public void tick(CallbackInfo info)
	{
		// TODO: Improve performance.
		IWearablesEntity.from((Entity)(Object)this)
			.getEquippedWearables()
			.map(WearablesSlotImpl.class::cast)
			.forEach(WearablesSlotImpl::tick);
	}
}
