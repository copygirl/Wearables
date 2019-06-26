package net.mcft.copy.wearables.common.impl.item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
		this._lookup.put(EquipmentSlot.HEAD , "ItemArmor:helmet"    );
		this._lookup.put(EquipmentSlot.CHEST, "ItemArmor:chestplate");
		this._lookup.put(EquipmentSlot.LEGS , "ItemArmor:leggings"  );
		this._lookup.put(EquipmentSlot.FEET , "ItemArmor:boots"     );
	}
	
	
	@Override
	public Collection<String> getHandledSpecialItems()
		{ return this._lookup.values(); }
	
	@Override
	public String getSpecialItems(Item item)
	{
		return (item instanceof ArmorItem) 
			? this._lookup.get(((ArmorItem)item).getSlotType())
			: null;
	}
}
