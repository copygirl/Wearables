package net.mcft.copy.wearables.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class WearablesSlot
{
	public final WearablesRegion region;
	public final WearablesSlot parent;
	public final String name;
	public final String fullName;
	
	protected int order = 500;
	public int getOrder() { return order; }
	public WearablesSlot setOrder(int value) { this.order = value; return this; }
	
	protected int numSlots = 1;
	public int getNumSlots() { return numSlots; }
	public WearablesSlot setNumSlots(int value)
		{ this.numSlots = value; return this;}
	public WearablesSlot setMinNumSlots(int value)
		{ if (value > this.numSlots) this.numSlots = value; return this;}
	
	protected EquipmentSlot vanillaSlot = null;
	public EquipmentSlot getVanilla() { return vanillaSlot; }
	protected WearablesSlot setVanilla(EquipmentSlot value)
		{ this.vanillaSlot = value; return this; }
	
	protected final Set<WearablesSlot> children = new HashSet<>();
	public Set<WearablesSlot> getChildren()
		{ return Collections.unmodifiableSet(children); }
	
	
	protected WearablesSlot(WearablesRegion region, WearablesSlot parent, String name)
	{
		if (region == null) throw new IllegalArgumentException("region is null");
		if (name == null) throw new IllegalArgumentException("name is null");
		this.region = region;
		this.parent = parent;
		this.name   = name;
		
		String fullName = region.name + ":";
		for (; (parent != null); parent = parent.parent)
			fullName += parent.name + "/";
		fullName += name;
		this.fullName = fullName;
	}
	
	
	// TODO: For the moment, if slot has no children, it automatically "exists" and is enabled.
	public boolean isEnabled()
		{ return children.isEmpty(); }
	
	/** Returns if the specified stack may be worn in this slot. */
	public boolean isValid(ItemStack stack)
	{
		return !stack.isEmpty() && isEnabled()
		    && WearablesAPI.getAppropriateSlotNames(stack).stream()
		                   .anyMatch(s -> s.startsWith(fullName));
	}
	
	
	@Override
	public String toString()
		{ return fullName; }
}
