package net.mcft.copy.wearables.common.impl.slot;

import java.util.HashMap;
import java.util.Map;

import net.mcft.copy.wearables.api.IWearablesSlot;
import net.mcft.copy.wearables.api.IWearablesSlotHandler;
import net.mcft.copy.wearables.api.WearablesSlotType;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public final class LivingEntityEquipmentSlotHandler
	implements IWearablesSlotHandler<LivingEntity>
{
	public static final LivingEntityEquipmentSlotHandler INSTANCE = new LivingEntityEquipmentSlotHandler();
	
	private final Map<WearablesSlotType, EquipmentSlot> _lookup = new HashMap<>();
	
	private LivingEntityEquipmentSlotHandler()
	{
		this._lookup.put(WearablesSlotType.HELMET    , EquipmentSlot.HEAD );
		this._lookup.put(WearablesSlotType.CHESTPLATE, EquipmentSlot.CHEST);
		this._lookup.put(WearablesSlotType.LEGGINGS  , EquipmentSlot.LEGS );
		this._lookup.put(WearablesSlotType.BOOTS     , EquipmentSlot.FEET );
	}
	
	
	@Override
	public Iterable<WearablesSlotType> getSlotTypes(EntityType<LivingEntity> entityType)
		{ return this._lookup.keySet(); }
	
	@Override
	public Identifier getIcon(WearablesSlotType slotType)
		{ return new Identifier("item/empty_armor_slot_" + slotType.getShortName()); }
	
	
	@Override
	public ItemStack get(LivingEntity entity, IWearablesSlot slot)
		{ return entity.getEquippedStack(this._lookup.get(slot.getSlotType())); }
	
	@Override
	public void set(LivingEntity entity, IWearablesSlot slot, ItemStack value)
		{ entity.setEquippedStack(this._lookup.get(slot.getSlotType()), value); }
	
	
	// FIXME: setEquippedStack isn't called for shift-click in inventory GUI.
	//        But that's okay, we need to replace that behavior anyway to
	//        check whether the item can be equipped in the first place.
}
