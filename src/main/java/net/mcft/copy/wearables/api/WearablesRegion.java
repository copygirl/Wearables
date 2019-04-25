package net.mcft.copy.wearables.api;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
	
	private EquipmentSlot _vanillaSlot;
	public EquipmentSlot getVanillaSlot() { return _vanillaSlot; }
	
	private String _containerSlotHint;
	public String getContainerSlotHint() { return _containerSlotHint; }
	
	protected WearablesRegion setVanilla(EquipmentSlot slot, String hint)
		{ this._vanillaSlot = slot; this._containerSlotHint = hint; return this; }
	
	private Map<String, Position> _guiPosLookup = new HashMap<>();
	public WearablesRegion setPosition(Class<?> guiClass, int x, int y)
		{ return setPosition(guiClass.getName(), x, y); }
	public WearablesRegion setPosition(String guiClassName, int x, int y)
		{ _guiPosLookup.put(guiClassName, new Position(x, y)); return this; }
	public Position getPosition(Class<?> guiClass)
		{ return _guiPosLookup.get(guiClass.getName()); }
	
	protected final Set<WearablesSlotType> _children = new HashSet<>();
	public Collection<WearablesSlotType> getChildren()
		{ return Collections.unmodifiableCollection(_children); }
	
	
	protected WearablesRegion(String name)
		{ this.name = name; }
	
	
	public static final class Position
	{
		public final int x;
		public final int y;
		public Position(int x, int y)
			{ this.x = x; this.y = y; }
	}
}
