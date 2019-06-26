package net.mcft.copy.wearables.api;

import java.util.regex.Pattern;

import net.minecraft.entity.EquipmentSlot;

/** Represents a general slot type of which there may be multiple, such
 *  as {@code chest/chestplate}, {@code neck/amulet} or {@code fingers/ring}. */
public final class WearablesSlotType
{
	public static final Pattern SLOT_TYPE_REGEX =
		Pattern.compile("([a-z]+/)*(?<shortName>[a-z]+)");
	
	public static final WearablesSlotType HELMET     = new WearablesSlotType( "head/helmet"    );
	public static final WearablesSlotType CHESTPLATE = new WearablesSlotType("chest/chestplate");
	public static final WearablesSlotType LEGGINGS   = new WearablesSlotType( "legs/leggings"  );
	public static final WearablesSlotType BOOTS      = new WearablesSlotType( "feet/boots"     );
	
	
	/** The full name of this slot type, such as {@code "neck/amulet"}. */
	public final String fullName;
	
	public WearablesSlotType(String fullName)
	{
		if ((fullName == null) || fullName.isEmpty())
			throw new IllegalArgumentException("fullName is null or empty");
		if (!SLOT_TYPE_REGEX.matcher(fullName).matches())
			throw new IllegalArgumentException("fullName '" + fullName + "' is not a valid slot type string");
		this.fullName = fullName;
	}
	
	/** Returns the {@link WearablesSlotType} associated with the specified Vanilla {@link EquipmentSlot}.
	 *  @throws IllegalArgumentException If slot is null or its type isn't {@link EquipmentSlot.Type#ARMOR}. */
	public static WearablesSlotType fromVanillaSlot(EquipmentSlot slot)
	{
		if (slot == null)
			throw new IllegalArgumentException("slot is null");
		if (slot.getType() != EquipmentSlot.Type.ARMOR)
			throw new IllegalArgumentException("slot's type isn't ARMOR");
		switch (slot) {
			case HEAD  : return HELMET;
			case CHEST : return CHESTPLATE;
			case LEGS  : return LEGGINGS;
			case FEET  : return BOOTS;
			default: throw new RuntimeException(
				"slot has type ARMOR, but isn't HEAD, CHEST, LEGS or FEET?!");
		}
	}
	
	/** Gets the short name (or last part) of this slot type,
	 *  such as {@code "amulet"}. Used for localization. */
	public String getShortName()
	{
		int start = this.fullName.lastIndexOf('/');
		return (start >= 0) ? this.fullName.substring(start + 1)
		                    : this.fullName;
	}
	
	/**
	 * Returns whether this slot type "matches" the specified slot type mask.
	 * That is, this slot type is equal to, or a child of the mask, such as
	 * {@code chest/chestplate} in relation to the mask {@code chest}.
	 * 
	 * @throws IllegalArgumentException Thrown if mask is {@code null}.
	 */
	public boolean matches(WearablesSlotType mask)
	{
		if (mask == null) throw new IllegalArgumentException("mask is null");
		int len     = this.fullName.length();
		int maskLen = mask.fullName.length();
		return (len == maskLen) ? this.fullName.equals(mask.fullName)
		     : (len  > maskLen) ? this.fullName.startsWith(mask.fullName) && (this.fullName.charAt(maskLen) == '/')
		                        : false;
	}
	
	
	@Override
	public String toString()
		{ return fullName; }
	
	@Override
	public boolean equals(Object obj)
	{
		return (obj instanceof WearablesSlotType)
		    && ((WearablesSlotType)obj).fullName.equals(this.fullName);
	}
	
	@Override
	public int hashCode()
		{ return this.fullName.hashCode(); }
}
