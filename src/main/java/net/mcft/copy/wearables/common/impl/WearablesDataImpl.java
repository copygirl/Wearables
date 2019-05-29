package net.mcft.copy.wearables.common.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.mcft.copy.wearables.api.IWearablesData;
import net.mcft.copy.wearables.api.IWearablesRegion;
import net.mcft.copy.wearables.api.IWearablesSlotType;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class WearablesDataImpl
	implements IWearablesData
{
	public final Map<String, WearablesRegionImpl>   regions   = new HashMap<>();
	public final Map<String, WearablesSlotTypeImpl> slotTypes = new HashMap<>();
	
	public final Map<Identifier, Set<IWearablesSlotType>> itemToValidSlots = new HashMap<>();
	
	private IWearablesSlotType[] _vanillaSlotLookup;
	private List<Set<IWearablesSlotType>> _vanillaSlotSetLookup;
	
	
	public void clear()
	{
		this.regions.clear();
		this.slotTypes.clear();
		this.itemToValidSlots.clear();
		
		// Add hardcoded Vanilla regions.
		this.regions.put("head" , new WearablesRegionImpl("head" , EquipmentSlot.HEAD , "helmet"    ));
		this.regions.put("chest", new WearablesRegionImpl("chest", EquipmentSlot.CHEST, "chestplate"));
		this.regions.put("legs" , new WearablesRegionImpl("legs" , EquipmentSlot.LEGS , "leggings"  ));
		this.regions.put("feet" , new WearablesRegionImpl("feet" , EquipmentSlot.FEET , "boots"     ));
		// Add (currently) built-in custon regions.
		this.regions.put("back", new WearablesRegionImpl("back"));
		this.regions.put("arms", new WearablesRegionImpl("arms"));
	
		// Add built-in Vanilla slot types.
		this.slotTypes.put( "head:armor/helmet"    , new WearablesSlotTypeImpl( "head:armor/helmet"    , EquipmentSlot.HEAD ));
		this.slotTypes.put("chest:armor/chestplate", new WearablesSlotTypeImpl("chest:armor/chestplate", EquipmentSlot.CHEST));
		this.slotTypes.put( "legs:armor/leggings"  , new WearablesSlotTypeImpl( "legs:armor/leggings"  , EquipmentSlot.LEGS ));
		this.slotTypes.put( "feet:armor/boots"     , new WearablesSlotTypeImpl( "feet:armor/boots"     , EquipmentSlot.FEET ));
		
		// FIXME: InventoryScreen is a client class!
		this.regions.get("back").position.put(InventoryScreen.class, new WearablesRegionImpl.Position(76, 25));
		this.regions.get("arms").position.put(InventoryScreen.class, new WearablesRegionImpl.Position(76, 43));
		
		// this.regions.get("back").position.put(CreativeInventoryScreen.class, new WearablesRegionImpl.Position(126,  9));
		// this.regions.get("arms").position.put(CreativeInventoryScreen.class, new WearablesRegionImpl.Position(126, 28));
		
		this._vanillaSlotLookup = new IWearablesSlotType[] {
			getSlotType( "feet:armor/boots"     ), getSlotType( "legs:armor/leggings"  ),
			getSlotType("chest:armor/chestplate"), getSlotType( "head:armor/helmet"    ) };
		
		this._vanillaSlotSetLookup = Arrays.stream(this._vanillaSlotLookup)
			.map(slot -> new HashSet<>(Arrays.asList(slot)))
			.collect(Collectors.toList());
	}
	
	
	// TODO: Custom handlers should be implemented on an API level.
	public Set<IWearablesSlotType> getSlotsFromVanilla(ArmorItem item)
		{ return _vanillaSlotSetLookup.get(item.getSlotType().getEntitySlotId()); }
	
	
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
	public IWearablesSlotType getSlotType(EquipmentSlot slot)
		{ return _vanillaSlotLookup[slot.getEntitySlotId()]; }
	
	@Override
	public Set<IWearablesSlotType> getValidSlots(ItemStack stack)
	{
		Item       item   = stack.getItem();
		Identifier itemId = Registry.ITEM.getId(item);
		Set<IWearablesSlotType> validSlots = itemToValidSlots.get(itemId);
		return (validSlots != null)        ? validSlots
		     : (item instanceof ArmorItem) ? getSlotsFromVanilla((ArmorItem)item)
		                                   : Collections.emptySet();
	}
}
