package net.mcft.copy.wearables.common.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.mcft.copy.wearables.api.IWearablesRegion;
import net.mcft.copy.wearables.api.IWearablesSlotType;

import net.minecraft.entity.EquipmentSlot;

public class WearablesRegionImpl
	implements IWearablesRegion
{
	private final String _name;
	
	public final List<WearablesSlotTypeImpl> slotTypes = new ArrayList<>();
	public final Map<Class<?>, Position> position = new HashMap<>();
	public final EquipmentSlot vanillaSlot;
	public final String containerSlotHint;
	
	public WearablesRegionImpl(String name)
		{ this(name, null, null); }
	public WearablesRegionImpl(String name, EquipmentSlot vanillaSlot, String containerSlotHint)
	{
		if (name == null) throw new IllegalArgumentException("name is null");
		if (!IWearablesRegion.REGION_REGEX.matcher(name).matches())
			throw new IllegalArgumentException("name '" + name + "' is not a valid region name");
		
		this._name             = name;
		this.vanillaSlot       = vanillaSlot;
		this.containerSlotHint = containerSlotHint;
	}
	
	
	public static final class Position
	{
		public final int x;
		public final int y;
		public Position(int x, int y)
			{ this.x = x; this.y = y; }
	}
	
	
	// IWearablesRegion implementation
	
	@Override
	public String getName() { return this._name; }
	
	@Override
	public Collection<IWearablesSlotType> getSlotTypes()
		{ return Collections.unmodifiableCollection(this.slotTypes); }
	
	@Override
	public boolean isVanilla()
		{ return (this.vanillaSlot != null); }
}
