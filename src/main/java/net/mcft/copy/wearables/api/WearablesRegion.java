package net.mcft.copy.wearables.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.EquipmentSlot;

public class WearablesRegion
{
	public static final WearablesRegion HEAD  = WearablesAPI.getRegion("head");
	public static final WearablesRegion CHEST = WearablesAPI.getRegion("chest");
	public static final WearablesRegion LEGS  = WearablesAPI.getRegion("legs");
	public static final WearablesRegion FEET  = WearablesAPI.getRegion("feet");
	
	public static final WearablesRegion BACK  = WearablesAPI.getRegion("back");
	public static final WearablesRegion ARMS  = WearablesAPI.getRegion("arms");
	
	
	public final String name;
	
	protected EquipmentSlot vanillaSlot;
	public EquipmentSlot getVanillaSlot() { return vanillaSlot; }
	
	protected String containerSlotHint;
	public String getContainerSlotHint() { return containerSlotHint; }
	
	protected WearablesRegion setVanilla(EquipmentSlot slot, String hint)
		{ this.vanillaSlot = slot; this.containerSlotHint = hint; return this; }
	
	protected final Set<WearablesSlotSettings> children = new HashSet<>();
	public Set<WearablesSlotSettings> getChildren()
		{ return Collections.unmodifiableSet(children); }
	
	
	protected WearablesRegion(String name)
	{
		this.name = name;
	}
}
