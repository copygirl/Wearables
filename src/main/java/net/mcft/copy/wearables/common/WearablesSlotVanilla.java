package net.mcft.copy.wearables.common;

import net.mcft.copy.wearables.api.IWearablesSlot;
import net.mcft.copy.wearables.api.WearablesSlotType;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;

public class WearablesSlotVanilla
	implements IWearablesSlot
{
	private final LivingEntity _entity;
	private final WearablesSlotType _slotType;
	private final EquipmentSlot _equipmentSlot;
	
	public WearablesSlotVanilla(LivingEntity entity, WearablesSlotType slotType)
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
	public LivingEntity getEntity() { return this._entity; }
	
	@Override
	public WearablesSlotType getSlotType() { return this._slotType; }
	
	
	@Override
	public ItemStack get()
		{ return this._entity.getEquippedStack(_equipmentSlot); }
	
	@Override
	public void set(ItemStack stack)
		{ this._entity.setEquippedStack(_equipmentSlot, stack); }
	
	@Override
	public boolean canEquip(ItemStack stack)
		{ return stack.isEmpty() || (MobEntity.getPreferredEquipmentSlot(stack) == this._equipmentSlot); }
	
	
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof WearablesSlotVanilla)) return false;
		WearablesSlotVanilla slot = (WearablesSlotVanilla)obj;
		return (slot._entity == slot._entity)
		    && (slot._slotType == slot._slotType)
		    && (slot._equipmentSlot == slot._equipmentSlot);
	}
}
