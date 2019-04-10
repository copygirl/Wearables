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
		_regions.put("head" , WearablesRegion.HEAD);
		_regions.put("chest", WearablesRegion.CHEST);
		_regions.put("legs" , WearablesRegion.LEGS);
		_regions.put("feet" , WearablesRegion.FEET);
		
		_regions.put("back", WearablesRegion.BACK);
		_regions.put("arms", WearablesRegion.ARMS);
		
		WearablesAPI.registerOrGetSlot( "head:armor/helmet"    ).setOrder(0).setVanilla(EquipmentSlot.HEAD );
		WearablesAPI.registerOrGetSlot("chest:armor/chestplate").setOrder(0).setVanilla(EquipmentSlot.CHEST);
		WearablesAPI.registerOrGetSlot( "legs:armor/leggings"  ).setOrder(0).setVanilla(EquipmentSlot.LEGS );
		WearablesAPI.registerOrGetSlot( "feet:armor/boots"     ).setOrder(0).setVanilla(EquipmentSlot.FEET );
		
		WearablesAPI.registerOrGetSlot( "head:clothing/hat"  );
		WearablesAPI.registerOrGetSlot("chest:clothing/shirt");
		WearablesAPI.registerOrGetSlot( "legs:clothing/pants");
		WearablesAPI.registerOrGetSlot( "feet:clothing/socks");
		
		WearablesAPI.registerOrGetSlot("chest:neck/amulet").setOrder(-500);
		WearablesAPI.registerOrGetSlot( "legs:waist/belt" ).setOrder(-500);
		
		WearablesAPI.registerOrGetSlot("back:tool"        ).setNumSlots(2);
		WearablesAPI.registerOrGetSlot("back:carry"       ).setOrder(100);
		WearablesAPI.registerOrGetSlot("arms:hands/gloves").setOrder(100);
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
	
	
	public static WearablesSlot registerOrGetSlot(String fullName)
	{
		if ((fullName == null) || fullName.isEmpty())
			throw new IllegalArgumentException("fullName is null or empty");
		
		int endIndex = fullName.indexOf(':');
		if (endIndex < 0) throw new IllegalArgumentException("fullName must contain a colon (:)");
		String regionName      = fullName.substring(0, endIndex);
		WearablesRegion region = getRegion(regionName);
		if (region == null) throw new IllegalStateException("region '" + regionName+ "' does not exist");
		
		WearablesSlot parent = null;
		int startIndex = endIndex + 1;
		for (; (endIndex = fullName.indexOf('/', startIndex)) >= 0; startIndex = endIndex + 1) {
			final int finalEndIndex = endIndex; // Fuck Java.
			CharSequence partName = fullName.subSequence(startIndex, endIndex);
			if (partName.length() == 0) throw new IllegalArgumentException("fullName contains an empty part");
			parent = ((parent != null) ? parent.children : region.children)
				.stream().filter(s -> partName.equals(s.name)).findFirst()
				.orElseGet(() -> registerOrGetSlot(fullName.substring(0, finalEndIndex)));
		}
		
		WearablesSlot result = new WearablesSlot(region, parent, fullName.substring(startIndex));
		((parent != null) ? parent.children : region.children).add(result);
		return result;
	}
	
	public static WearablesSlot findSlot(String name)
	{
		if ((name == null) || name.isEmpty())
			throw new IllegalArgumentException("name is null or empty");
		
		int endIndex = name.indexOf(':');
		if (endIndex < 0) throw new IllegalArgumentException("fullName must contain a colon (:)");
		String regionName      = name.substring(0, endIndex);
		WearablesRegion region = getRegion(regionName);
		if (region == null) return null;
		
		WearablesSlot result = null;
		WearablesSlot parent = null;
		int startIndex = endIndex + 1;
		for (; startIndex > 0; startIndex = endIndex + 1) {
			endIndex = name.indexOf('/', startIndex);
			CharSequence partName = (endIndex > 0)
				? name.subSequence(startIndex, endIndex)
				: name.subSequence(startIndex, name.length());
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
	
	public static List<WearablesSlot> getValidSlots(ItemStack stack)
	{
		return getAppropriateSlotNames(stack).stream()
			.map(WearablesAPI::findSlot)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}
}
