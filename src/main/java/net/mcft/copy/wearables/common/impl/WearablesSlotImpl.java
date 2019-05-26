package net.mcft.copy.wearables.common.impl;

import net.mcft.copy.wearables.api.IWearablesItem;
import net.mcft.copy.wearables.api.IWearablesSlot;
import net.mcft.copy.wearables.api.IWearablesSlotType;
import net.mcft.copy.wearables.common.WearablesEntityData;
import net.mcft.copy.wearables.common.network.NetUtil;
import net.mcft.copy.wearables.common.network.WearablesUpdatePacket;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class WearablesSlotImpl
	implements IWearablesSlot
{
	private final LivingEntity _entity;
	private final IWearablesSlotType _slotType;
	private final int _index;
	protected int _equippedTime = 0;
	
	
	public WearablesSlotImpl(LivingEntity entity, IWearablesSlotType slotType, int index)
	{
		if (entity == null) throw new IllegalArgumentException("entity is null");
		if (slotType == null) throw new IllegalArgumentException("slotType is null");
		this._entity   = entity;
		this._slotType = slotType;
		this._index    = index;
	}
	
	
	public void tick()
	{
		ItemStack stack = get();
		if (!stack.isEmpty() && (stack.getItem() instanceof IWearablesItem)) {
			IWearablesItem item = (IWearablesItem)stack.getItem();
			if (item.doesTick()) item.onEquippedTick(this, _equippedTime);
		}
		_equippedTime++;
		// TODO: Resync item if it has been changed.
	}
	
	
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof WearablesSlotImpl)) return false;
		WearablesSlotImpl slot = (WearablesSlotImpl)obj;
		return (slot._entity   == this._entity)
		    && (slot._slotType == this._slotType)
		    && (slot._index    == this._index);
	}
	
	@Override
	public String toString()
		{ return this._slotType + ":" + this._index; }
	
	
	// IWearablesSlot implementation
	
	@Override
	public LivingEntity getEntity() { return this._entity; }
	
	@Override
	public IWearablesSlotType getSlotType() { return this._slotType; }
	
	@Override
	public int getIndex() { return _index; }
	
	
	@Override
	public ItemStack get()
	{
		WearablesEntityData data = ((WearablesEntityData.IAccessor)this._entity).getWearablesData(false);
		return (data != null) ? data.get(this._slotType.getFullName(), this._index) : ItemStack.EMPTY;
	}
	
	@Override
	public void set(ItemStack value)
	{
		if (value == null) throw new NullPointerException("value is null");
		
		ItemStack previous = get();
		if (ItemStack.areEqual(value, previous)) return;
		
		IWearablesItem.from(previous.getItem()).onUnequip(this);
		((WearablesEntityData.IAccessor)this._entity).getWearablesData(true)
			.set(this._slotType.getFullName(), this._index, value);
		this._equippedTime = 0;
		IWearablesItem.from(value.getItem()).onEquip(this);
		
		// When called server-side, synchronizes the current stack to players tracking
		//  this slot's entity (including, if the entity is a player, themselves).
		if ((this._entity.world != null) && !this._entity.world.isClient)
			NetUtil.sendToPlayersTracking(this._entity, new WearablesUpdatePacket(this), true);
	}
}
