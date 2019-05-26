package net.mcft.copy.wearables.common.impl;

import net.mcft.copy.wearables.api.IWearablesItem;
import net.mcft.copy.wearables.api.IWearablesSlotType;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class WearablesSlotImplVanilla
	extends WearablesSlotImpl
{
	private final EquipmentSlot _equipmentSlot;
	
	public WearablesSlotImplVanilla(LivingEntity entity, IWearablesSlotType slotType)
	{
		super(entity, slotType, 0);
		this._equipmentSlot = slotType.getVanilla();
		if (this._equipmentSlot == null) throw new IllegalArgumentException(
			"slotType '" + slotType + "' is not Vanilla");
	}
	
	
	// IWearablesSlot implementation
	
	@Override
	public ItemStack get()
		{ return this.getEntity().getEquippedStack(_equipmentSlot); }
	
	@Override
	public void set(ItemStack value)
	{
		if (value == null) throw new NullPointerException("value is null");
		
		ItemStack previous = get();
		if (ItemStack.areEqual(value, previous)) return;
		
		IWearablesItem.from(previous.getItem()).onUnequip(this);
		this.getEntity().setEquippedStack(_equipmentSlot, value);
		this._equippedTime = 0;
		IWearablesItem.from(value.getItem()).onEquip(this);
	}
}
