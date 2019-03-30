package net.mcft.copy.wearables.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class WearablesAPI
{
	private WearablesAPI() {  }
	
	
	private static String[][] _vanillaSlotLookup = {
		{  "feet:armor/boots"      }, { "legs:armor/leggings" },
		{ "chest:armor/chestplate" }, { "head:armor/helmet"   },
	};
	
	private static Map<String, WearablesRegion> _regions = new HashMap<>();
	
	static
	{
		// Equipment regions based on Vanilla armor slots.
		registerOrGetRegion( "head").setVanilla(EquipmentSlot.HEAD , "helmet"    );
		registerOrGetRegion("chest").setVanilla(EquipmentSlot.CHEST, "chestplate");
		registerOrGetRegion( "legs").setVanilla(EquipmentSlot.LEGS , "leggings"  );
		registerOrGetRegion( "feet").setVanilla(EquipmentSlot.FEET , "boots"     );
		
		// Custom equipment regions, visible above off-hand slot.
		registerOrGetRegion("back");
		registerOrGetRegion("arms");
		
		
		registerOrGetSlot( "head:armor/helmet"    ).setVanillaSlot(EquipmentSlot.HEAD);
		registerOrGetSlot("chest:armor/chestplate").setVanillaSlot(EquipmentSlot.CHEST);
		registerOrGetSlot( "legs:armor/leggings"  ).setVanillaSlot(EquipmentSlot.LEGS);
		registerOrGetSlot( "feet:armor/boots"     ).setVanillaSlot(EquipmentSlot.FEET);
		
		registerOrGetSlot( "head:clothing/hat");
		registerOrGetSlot("chest:clothing/shirt");
		registerOrGetSlot( "legs:clothing/pants");
		registerOrGetSlot( "feet:clothing/socks");
		
		registerOrGetSlot("chest:neck/amulet");
		registerOrGetSlot( "legs:waist/belt");
		
		registerOrGetSlot("back:tool").setNumSlots(2);
		registerOrGetSlot("back:carry");
		registerOrGetSlot("arms:hands/gloves");
	}
	
	
	/** Returns all registered regions such as {@link WearablesRegion#CHEST WearablesRegion.CHEST}
	 *  and {@link WearablesRegion#BACK WearablesRegion.BACK}. */
	public static Collection<WearablesRegion> getRegions()
		{ return _regions.values(); }
	
	/**
	 * Returns the region with the specified name / for the specified
	 * Wearables slot identifier, or null if none has been registered.
	 * 
	 * @param nameOrSlot A name or slot identifier such as {@code "chest"} or {@code "chest:neck/amulet"}.
	 * @return A region such as {@link WearablesRegion#CHEST WearablesRegion.CHEST} or null.
	 **/
	public static WearablesRegion getRegion(String nameOrSlot)
	{
		if ((nameOrSlot == null) || nameOrSlot.isEmpty())
			throw new IllegalArgumentException("nameOrSlot is null or empty");
		
		int colonIndex = nameOrSlot.indexOf(':');
		if (colonIndex >= 0) nameOrSlot = nameOrSlot.substring(0, colonIndex);
		
		return _regions.get(nameOrSlot);
	}
	
	public static WearablesRegion registerOrGetRegion(String name)
	{
		if ((name == null) || (name.length() == 0))
			throw new IllegalArgumentException("name is null or empty");
		if (name.indexOf(':') >= 0)
			throw new IllegalArgumentException("name may not contain a colon (:)");
		
		WearablesRegion result = _regions.get(name);
		if (result == null) _regions.put(name, result = new WearablesRegion(name));
		return result;
	}
	
	
	public static WearablesSlotSettings registerOrGetSlot(String fullName)
	{
		if ((fullName == null) || fullName.isEmpty())
			throw new IllegalArgumentException("fullName is null or empty");
		
		int endIndex = fullName.indexOf(':');
		if (endIndex < 0) throw new IllegalArgumentException("fullName must contain a colon (:)");
		String regionName      = fullName.substring(0, endIndex);
		WearablesRegion region = getRegion(regionName);
		if (region == null) throw new IllegalStateException("region '" + regionName+ "' does not exist");
		
		WearablesSlotSettings parent = null;
		int startIndex = endIndex + 1;
		for (; (endIndex = fullName.indexOf('/', startIndex)) >= 0; startIndex = endIndex + 1) {
			final int finalEndIndex = endIndex; // Fuck Java.
			CharSequence partName = fullName.subSequence(startIndex, endIndex);
			if (partName.length() == 0) throw new IllegalArgumentException("fullName contains an empty part");
			parent = ((parent != null) ? parent.children : region.children)
				.stream().filter(s -> partName.equals(s.name)).findFirst()
				.orElseGet(() -> registerOrGetSlot(fullName.substring(0, finalEndIndex)));
		}
		
		WearablesSlotSettings result = new WearablesSlotSettings(region, parent, fullName.substring(startIndex));
		((parent != null) ? parent.children : region.children).add(result);
		return result;
	}
	
	public static WearablesSlotSettings findSlot(String name)
	{
		if ((name == null) || name.isEmpty())
			throw new IllegalArgumentException("name is null or empty");
		
			int endIndex = name.indexOf(':');
			if (endIndex < 0) throw new IllegalArgumentException("fullName must contain a colon (:)");
			String regionName      = name.substring(0, endIndex);
			WearablesRegion region = getRegion(regionName);
			if (region == null) return null;
			
			WearablesSlotSettings result = null;
			WearablesSlotSettings parent = null;
			int startIndex = endIndex + 1;
			for (; startIndex > 0; startIndex = endIndex + 1) {
				endIndex = name.indexOf('/', startIndex);
				CharSequence partName = (endIndex > 0)
					? name.subSequence(startIndex, endIndex)
					: name.subSequence(startIndex, name.length() - 1);
				if (partName.length() == 0) throw new IllegalArgumentException("name contains an empty part");
				parent = ((parent != null) ? parent.children : region.children)
					.stream().filter(s -> partName.equals(s.name))
					.findFirst().orElse(null);
				if (parent == null) break;
				else if (parent.isEnabled()) result = parent;
			}
			return result;
	}
	
	
	public static List<String> getAppropriateSlotNames(ItemStack stack)
	{
		if (stack == null) throw new IllegalArgumentException("stack is null");
		Item item = stack.getItem();
		if (item instanceof ArmorItem)
			return Arrays.asList(_vanillaSlotLookup[((ArmorItem)item).getSlotType().getEntitySlotId()]);
		return Collections.emptyList();
	}
	
	public static List<WearablesSlotSettings> getValidSlots(ItemStack stack)
	{
		return getAppropriateSlotNames(stack).stream()
			.map(WearablesAPI::findSlot)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}
}
