package net.mcft.copy.wearables.common;

import net.mcft.copy.wearables.api.IWearablesSlot;
import net.mcft.copy.wearables.api.WearablesSlotType;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class WearablesSlot
	implements IWearablesSlot
{
	private final LivingEntity _entity;
	private final WearablesSlotType _slotType;
	private final int _index;
	
	public WearablesSlot(LivingEntity entity, WearablesSlotType slotType, int index)
	{
		if (entity == null) throw new IllegalArgumentException("entity is null");
		if (slotType == null) throw new IllegalArgumentException("slotType is null");
		this._entity   = entity;
		this._slotType = slotType;
		this._index    = index;
	}
	
	
	@Override
	public LivingEntity getEntity() { return this._entity; }
	
	@Override
	public WearablesSlotType getSlotType() { return this._slotType; }
	
	
	// TODO: Handle Vanilla armor slots properly.
	
	@Override
	public ItemStack get()
	{
		WearablesMap map = ((WearablesMap.IAccessor)this._entity).getWearablesMap(false);
		return (map != null) ? map.get(this._slotType, this._index) : ItemStack.EMPTY;
	}
	
	@Override
	public void set(ItemStack value)
	{
		((WearablesMap.IAccessor)this._entity).getWearablesMap(true)
			.set(this._slotType, this._index, value);
	}
	
	@Override
	public boolean canEquip(ItemStack stack)
	{
		if (stack.isEmpty()) return false;
		// TODO: Actual checking if item can be equipped.
		return true;
	}
	
	@Override
	public boolean canUnequip()
	{
		ItemStack stack = get();
		return !stack.isEmpty() && !EnchantmentHelper.hasBindingCurse(stack);
	}
	
	
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof WearablesSlot)) return false;
		WearablesSlot slot = (WearablesSlot)obj;
		return (slot._entity == slot._entity)
		    && (slot._slotType == slot._slotType)
		    && (slot._index == slot._index);
	}
}
