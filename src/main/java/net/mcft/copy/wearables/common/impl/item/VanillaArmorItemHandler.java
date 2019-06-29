package net.mcft.copy.wearables.common.impl.item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.mcft.copy.wearables.api.IWearablesItemHandler;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;

public class VanillaArmorItemHandler
	implements IWearablesItemHandler
{
	public static final VanillaArmorItemHandler INSTANCE = new VanillaArmorItemHandler();
	
	
	private final Map<EquipmentSlot, String> _lookup = new HashMap<>();
	
	private VanillaArmorItemHandler()
	{
		this._lookup.put(EquipmentSlot.HEAD , "ArmorItem:helmet"    );
		this._lookup.put(EquipmentSlot.CHEST, "ArmorItem:chestplate");
		this._lookup.put(EquipmentSlot.LEGS , "ArmorItem:leggings"  );
		this._lookup.put(EquipmentSlot.FEET , "ArmorItem:boots"     );
	}
	
	
	@Override
	public Collection<String> getHandledSpecialItems()
		{ return this._lookup.values(); }
	
	@Override
	public Optional<String> getSpecialItem(Item item)
	{
		return (item instanceof ArmorItem) 
			? Optional.of(this._lookup.get(((ArmorItem)item).getSlotType()))
			: Optional.empty();
	}
}
