package net.mcft.copy.wearables.common.impl;

import net.mcft.copy.wearables.api.IWearablesSlot;
import net.mcft.copy.wearables.api.IWearablesSlotType;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;

public class WearablesSlotImplVanilla
	implements IWearablesSlot
{
	private final LivingEntity _entity;
	private final IWearablesSlotType _slotType;
	private final EquipmentSlot _equipmentSlot;
	
	public WearablesSlotImplVanilla(LivingEntity entity, IWearablesSlotType slotType)
	{
		if (entity == null) throw new IllegalArgumentException("entity is null");
		if (slotType == null) throw new IllegalArgumentException("slotType is null");
		
		this._entity   = entity;
		this._slotType = slotType;
		
		this._equipmentSlot = slotType.getVanilla();
		if (this._equipmentSlot == null) throw new IllegalArgumentException(
			"slotType '" + slotType + "' is not Vanilla");
	}
	
	
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof WearablesSlotImplVanilla)) return false;
		WearablesSlotImplVanilla slot = (WearablesSlotImplVanilla)obj;
		return (slot._entity == slot._entity)
		    && (slot._slotType == slot._slotType)
		    && (slot._equipmentSlot == slot._equipmentSlot);
	}
	
	@Override
	public String toString()
		{ return this._slotType.toString(); }
	
	
	// IWearablesSlot implementation
	
	@Override
	public LivingEntity getEntity() { return this._entity; }
	
	@Override
	public IWearablesSlotType getSlotType() { return this._slotType; }
	
	@Override
	public int getIndex() { return 0; }
	
	
	@Override
	public ItemStack get()
		{ return this._entity.getEquippedStack(_equipmentSlot); }
	
	@Override
	public void set(ItemStack stack)
		{ this._entity.setEquippedStack(_equipmentSlot, stack); }
	
	@Override
	public boolean canEquip(ItemStack stack)
		{ return stack.isEmpty() || (MobEntity.getPreferredEquipmentSlot(stack) == this._equipmentSlot); }
}
