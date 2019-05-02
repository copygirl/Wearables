package net.mcft.copy.wearables.common.impl;

import java.util.regex.Matcher;

import net.mcft.copy.wearables.api.IWearablesData;
import net.mcft.copy.wearables.api.IWearablesRegion;
import net.mcft.copy.wearables.api.IWearablesSlotType;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Identifier;

public class WearablesSlotTypeImpl
	implements IWearablesSlotType
{
	private final IWearablesRegion _region;
	private final String _fullName;
	private final String _name;
	private final Identifier _icon;
	private final EquipmentSlot _vanillaSlot;
	
	public int order    = 500;
	public int numSlots = 1;
	
	
	public WearablesSlotTypeImpl(String fullName)
		{ this(fullName, null); }
	public WearablesSlotTypeImpl(String fullName, EquipmentSlot vanillaSlot)
	{
		if (fullName == null) throw new IllegalArgumentException("fullName is null");
		
		Matcher fullNameMatcher = IWearablesSlotType.SLOT_TYPE_REGEX.matcher(fullName);
		if (!fullNameMatcher.matches()) throw new IllegalArgumentException(
			"fullName '" + fullName + "' is not a valid slot type name");
		
		this._region = IWearablesData.INSTANCE.getRegion(fullNameMatcher.group("region"));
		if (this._region == null) throw new IllegalStateException(
			"Unknown region for slot type '" + fullName + "'");
		
		this._fullName    = fullName;
		this._name        = fullNameMatcher.group("name");
		this._vanillaSlot = vanillaSlot;
		this._icon        = new Identifier("wearables",
			"textures/gui/icons/" + fullName.replace(':', '_').replace('/', '_') + ".png");
		
		if (isVanilla()) order = 0;
	}
	
	
	@Override
	public String toString()
		{ return this._fullName; }
	
	
	// IWearablesSlotType implementation
	
	@Override
	public String getFullName() { return this._fullName; }
	
	@Override
	public String getName() { return this._name; }
	
	@Override
	public IWearablesRegion getRegion() { return this._region; }
	
	@Override
	public Identifier getIcon() { return this._icon; }
	
	@Override
	public EquipmentSlot getVanilla() { return this._vanillaSlot; }
	
	@Override
	public int getOrder() { return this.order; }
	
	@Override
	public int getNumSlots() { return this.numSlots; }
}
