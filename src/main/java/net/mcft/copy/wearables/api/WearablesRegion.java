package net.mcft.copy.wearables.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.EquipmentSlot;

public class WearablesRegion
{
	// Equipment regions based on Vanilla armor slots.
	public static final WearablesRegion HEAD  = new WearablesRegion( "head").setVanilla(EquipmentSlot.HEAD , "helmet"    );
	public static final WearablesRegion CHEST = new WearablesRegion("chest").setVanilla(EquipmentSlot.CHEST, "chestplate");
	public static final WearablesRegion LEGS  = new WearablesRegion( "legs").setVanilla(EquipmentSlot.LEGS , "leggings"  );
	public static final WearablesRegion FEET  = new WearablesRegion( "feet").setVanilla(EquipmentSlot.FEET , "boots"     );
	
	// Custom equipment regions, visible above off-hand slot.
	public static final WearablesRegion BACK = new WearablesRegion("back");
	public static final WearablesRegion ARMS = new WearablesRegion("arms");
	
	
	public final String name;
	
	protected EquipmentSlot vanillaSlot;
	public EquipmentSlot getVanillaSlot() { return vanillaSlot; }
	
	protected String containerSlotHint;
	public String getContainerSlotHint() { return containerSlotHint; }
	
	protected WearablesRegion setVanilla(EquipmentSlot slot, String hint)
		{ this.vanillaSlot = slot; this.containerSlotHint = hint; return this; }
	
	protected final Set<WearablesSlot> children = new HashSet<>();
	public Set<WearablesSlot> getChildren()
		{ return Collections.unmodifiableSet(children); }
	
	
	protected WearablesRegion(String name)
	{
		this.name = name;
	}
}
