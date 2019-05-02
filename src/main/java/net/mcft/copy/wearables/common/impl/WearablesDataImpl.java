package net.mcft.copy.wearables.common.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.mcft.copy.wearables.api.IWearablesData;
import net.mcft.copy.wearables.api.IWearablesRegion;
import net.mcft.copy.wearables.api.IWearablesSlotType;

import net.minecraft.client.gui.ingame.PlayerInventoryScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public final class WearablesDataImpl
	implements IWearablesData
{
	public final Map<String, WearablesRegionImpl> regions = new HashMap<>();
	public final Map<String, WearablesSlotTypeImpl> slotTypes = new HashMap<>();
	
	public WearablesDataImpl()
	{
		this.regions.put("head" , new WearablesRegionImpl("head" , EquipmentSlot.HEAD , "helmet"    ));
		this.regions.put("chest", new WearablesRegionImpl("chest", EquipmentSlot.CHEST, "chestplate"));
		this.regions.put("legs" , new WearablesRegionImpl("legs" , EquipmentSlot.LEGS , "leggings"  ));
		this.regions.put("feet" , new WearablesRegionImpl("feet" , EquipmentSlot.FEET , "boots"     ));
		
		this.regions.put("back", new WearablesRegionImpl("back"));
		this.regions.put("arms", new WearablesRegionImpl("arms"));
		
		this.regions.get("back").position.put(PlayerInventoryScreen.class, new WearablesRegionImpl.Position(76, 25));
		this.regions.get("arms").position.put(PlayerInventoryScreen.class, new WearablesRegionImpl.Position(76, 43));
		
		// this.regions.get("back").position.put(CreativePlayerInventoryScreen.class, new WearablesRegionImpl.Position(126,  9));
		// this.regions.get("arms").position.put(CreativePlayerInventoryScreen.class, new WearablesRegionImpl.Position(126, 28));
	}
	
	
	// IWearablesData implementation
	
	@Override
	public Collection<IWearablesRegion> getRegions()
		{ return Collections.unmodifiableCollection(this.regions.values()); }
	
	@Override
	public Collection<IWearablesSlotType> getSlotTypes()
		{ return Collections.unmodifiableCollection(this.slotTypes.values()); }
	
	@Override
	public IWearablesRegion getRegion(String nameOrSlot)
	{
		if ((nameOrSlot == null) || nameOrSlot.isEmpty())
			throw new IllegalArgumentException("nameOrSlot is null or empty");
		int colonIndex = nameOrSlot.indexOf(":");
		if (colonIndex >= 0) nameOrSlot = nameOrSlot.substring(0, colonIndex);
		return this.regions.get(nameOrSlot);
	}
	
	@Override
	public IWearablesSlotType getSlotType(String fullName)
	{
		if ((fullName == null) || fullName.isEmpty())
			throw new IllegalArgumentException("fullName is null or empty");
		return this.slotTypes.get(fullName);
	}
	
	@Override
	public Collection<IWearablesSlotType> getValidSlots(ItemStack stack)
	{
		// FIXME: Implement me!
		return Collections.emptyList();
	}
	
	// private String[][] _vanillaSlotLookup = {
	// 	{  "feet:armor/boots"      }, { "legs:armor/leggings" },
	// 	{ "chest:armor/chestplate" }, { "head:armor/helmet"   },
	// };
}
