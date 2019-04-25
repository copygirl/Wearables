package net.mcft.copy.wearables.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import net.minecraft.client.gui.ingame.PlayerInventoryScreen;
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
	// TODO: I have the feeling a slot map would be useful.
	
	static
	{
		_regions.put("head" , WearablesRegion.HEAD);
		_regions.put("chest", WearablesRegion.CHEST);
		_regions.put("legs" , WearablesRegion.LEGS);
		_regions.put("feet" , WearablesRegion.FEET);
		
		_regions.put("back", WearablesRegion.BACK);
		_regions.put("arms", WearablesRegion.ARMS);
		
		WearablesRegion.BACK.setPosition(PlayerInventoryScreen.class, 76, 25);
		WearablesRegion.ARMS.setPosition(PlayerInventoryScreen.class, 76, 43);
		
		WearablesAPI.registerOrGetSlotType( "head:armor/helmet"    ).setVanilla(EquipmentSlot.HEAD );
		WearablesAPI.registerOrGetSlotType("chest:armor/chestplate").setVanilla(EquipmentSlot.CHEST);
		WearablesAPI.registerOrGetSlotType( "legs:armor/leggings"  ).setVanilla(EquipmentSlot.LEGS );
		WearablesAPI.registerOrGetSlotType( "feet:armor/boots"     ).setVanilla(EquipmentSlot.FEET );
		
		WearablesAPI.registerOrGetSlotType( "head:clothing/hat"  );
		WearablesAPI.registerOrGetSlotType("chest:clothing/shirt");
		WearablesAPI.registerOrGetSlotType( "legs:clothing/pants");
		WearablesAPI.registerOrGetSlotType( "feet:clothing/socks");
		
		WearablesAPI.registerOrGetSlotType("chest:neck/amulet").setOrder(-500);
		WearablesAPI.registerOrGetSlotType( "legs:waist/belt" ).setOrder(-500);
		
		WearablesAPI.registerOrGetSlotType("back:tool"        ).setNumSlots(2);
		WearablesAPI.registerOrGetSlotType("back:carry"       ).setOrder(100);
		WearablesAPI.registerOrGetSlotType("arms:hands/gloves").setOrder(100);
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
	 * @exception IllegalArgumentException Thrown if nameOrSlot is null or empty.
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
	
	
	public static WearablesSlotType registerOrGetSlotType(String fullName)
	{
		if ((fullName == null) || fullName.isEmpty())
			throw new IllegalArgumentException("fullName is null or empty");
		
		int endIndex = fullName.indexOf(':');
		if (endIndex < 0) throw new IllegalArgumentException("fullName must contain a colon (:)");
		String regionName      = fullName.substring(0, endIndex);
		WearablesRegion region = getRegion(regionName);
		if (region == null) throw new IllegalStateException("region '" + regionName+ "' does not exist");
		
		WearablesSlotType parent = null;
		int startIndex = endIndex + 1;
		for (; (endIndex = fullName.indexOf('/', startIndex)) >= 0; startIndex = endIndex + 1) {
			final int finalEndIndex = endIndex; // Fuck Java.
			CharSequence partName = fullName.subSequence(startIndex, endIndex);
			if (partName.length() == 0) throw new IllegalArgumentException("fullName contains an empty part");
			parent = ((parent != null) ? parent._children : region.getChildren())
				.stream().filter(s -> partName.equals(s.name)).findFirst()
				.orElseGet(() -> registerOrGetSlotType(fullName.substring(0, finalEndIndex)));
		}
		
		WearablesSlotType result = new WearablesSlotType(region, parent, fullName.substring(startIndex));
		((parent != null) ? parent._children : region._children).add(result);
		return result;
	}
	
	public static WearablesSlotType findSlotType(String name)
	{
		if ((name == null) || name.isEmpty())
			throw new IllegalArgumentException("name is null or empty");
		
		int endIndex = name.indexOf(':');
		if (endIndex < 0) throw new IllegalArgumentException("fullName must contain a colon (:)");
		String regionName      = name.substring(0, endIndex);
		WearablesRegion region = getRegion(regionName);
		if (region == null) return null;
		
		WearablesSlotType result = null;
		WearablesSlotType parent = null;
		int startIndex = endIndex + 1;
		for (; startIndex > 0; startIndex = endIndex + 1) {
			endIndex = name.indexOf('/', startIndex);
			CharSequence partName = (endIndex > 0)
				? name.subSequence(startIndex, endIndex)
				: name.subSequence(startIndex, name.length());
			if (partName.length() == 0) throw new IllegalArgumentException("name contains an empty part");
			parent = ((parent != null) ? parent._children : region.getChildren())
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
	
	public static List<WearablesSlotType> getValidSlots(ItemStack stack)
	{
		return getAppropriateSlotNames(stack).stream()
			.map(WearablesAPI::findSlotType)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}
}
