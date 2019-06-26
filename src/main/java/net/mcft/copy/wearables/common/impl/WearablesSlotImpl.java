package net.mcft.copy.wearables.common.impl;

import net.mcft.copy.wearables.api.IWearablesItem;
import net.mcft.copy.wearables.api.IWearablesSlot;
import net.mcft.copy.wearables.api.IWearablesSlotHandler;
import net.mcft.copy.wearables.api.WearablesSlotType;
import net.mcft.copy.wearables.common.WearablesEntityData;
import net.mcft.copy.wearables.common.data.EntityTypeData;
import net.mcft.copy.wearables.common.data.EntityTypeData.SlotTypeData;
import net.mcft.copy.wearables.common.impl.slot.DefaultSlotHandler;
import net.mcft.copy.wearables.common.network.NetUtil;
import net.mcft.copy.wearables.common.network.WearablesUpdatePacket;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public class WearablesSlotImpl
	implements IWearablesSlot
	         , Comparable<WearablesSlotImpl>
{
	private final Entity _entity;
	private final WearablesSlotType _slotType;
	private final int _index;
	private final IWearablesSlotHandler<Entity> _handler;
	
	public WearablesSlotImpl(Entity entity, WearablesSlotType slotType, int index,
	                         IWearablesSlotHandler<Entity> handler)
	{
		if (entity == null) throw new IllegalArgumentException("entity is null");
		if (slotType == null) throw new IllegalArgumentException("slotType is null");
		if (index < 0) throw new IllegalArgumentException("index is negative");
		if (handler == null) throw new IllegalArgumentException("handler is null");
		this._entity   = entity;
		this._slotType = slotType;
		this._index    = index;
		this._handler  = handler;
	}
	
	
	public boolean hasDefaultHandler()
		{ return (this._handler == DefaultSlotHandler.INSTANCE); }
	
	public void tick()
	{
		// TODO: Implement ticking again!
		// ItemStack stack = get();
		// if (!stack.isEmpty() && (stack.getItem() instanceof IWearablesItem)) {
		// 	IWearablesItem item = (IWearablesItem)stack.getItem();
		// 	if (item.doesTick()) item.onEquippedTick(this, this._equippedTime);
		// }
		// // TODO: Resync item if it has been changed.
	}
	
	/** When called server-side, synchronizes the current stack to players tracking
	 *  this slot's entity (including, if the entity is a player, themselves). */
	public void sync()
	{
		if ((this._entity.world == null) || this._entity.world.isClient) return;
		NetUtil.sendToPlayersTracking(this._entity, new WearablesUpdatePacket(this), true);
	}
	
	
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof WearablesSlotImpl)) return false;
		WearablesSlotImpl slot = (WearablesSlotImpl)obj;
		return (slot._entity == this._entity)
		    && slot._slotType.equals(this._slotType)
		    && (slot._index == this._index);
	}
	
	@Override
	public int hashCode()
		{ return this._entity.getEntityId() ^ this._slotType.hashCode() ^ this._index; }
	
	@Override
	public String toString()
		{ return this._slotType + ":" + this._index; }
	
	
	// Comparable implementation
	
	@Override
	public int compareTo(WearablesSlotImpl other)
		{ return getOrder() - other.getOrder(); }
	
	
	// IWearablesSlot implementation
	
	@Override
	public Entity getEntity() { return this._entity; }
	
	@Override
	public WearablesSlotType getSlotType() { return this._slotType; }
	
	@Override
	public int getIndex() { return this._index; }
	
	@Override
	public int getOrder()
	{
		// TODO: Eventually, order may change on the fly. For example, an amulet could be worn above or below the chestplate.
		SlotTypeData data = EntityTypeData.from(getEntity()).slotTypes.get(getSlotType());
		return (data != null) ? data.order : 500;
	}
	
	@Override
	public boolean isValid()
	{
		SlotTypeData data = EntityTypeData.from(getEntity()).slotTypes.get(getSlotType());
		return (data != null) && (getIndex() < data.slotCount);
	}
	

	@Override
	public ItemStack get()
		{ return _handler.get(getEntity(), this); }
	
	@Override
	public void set(ItemStack value)
	{
		if (value == null) throw new NullPointerException("value is null");
		if (ItemStack.areItemsEqual(value, get())) return;
		_handler.set(getEntity(), this, value);
	}
	
	
	@Override
	public void invokeBeforeUnequip(ItemStack previousStack)
	{
		IWearablesItem.from(previousStack.getItem()).onUnequip(this);
	}
	
	@Override
	public void invokeAfterEquip(ItemStack currentStack)
	{
		WearablesEntityData data = ((WearablesEntityData.IAccessor)this._entity).getWearablesData(true);
		if (!currentStack.isEmpty()) {
			IWearablesItem.from(currentStack.getItem()).onEquip(this);
			data.get(this._slotType, this._index, true).resetEquippedTime();
		} else data.remove(this._slotType, this._index);
	}
}
