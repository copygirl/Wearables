package net.mcft.copy.wearables.api;

import java.util.regex.Pattern;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/** Represents a general slot type definition which holds information about a
 *  type of slot, of which there may be multiple, such as {@code chest:neck/amulet}. */
public interface IWearablesSlotType
{
	public static final Pattern SLOT_TYPE_REGEX
		= Pattern.compile("(?<region>[a-z]+):([a-z]+/)*(?<name>[a-z]+)");
	
	
	/** Gets the full name of this slot type, such as {@code "chest:neck/amulet"}. */
	public String getFullName();
	
	/** Gets the short name of this slot type, such as {@code "amulet"}. Used for localization. */
	public String getName();
	
	/** Gets the {@link IWearablesRegion} this slot type belongs to, such as {@code chest}. */
	public IWearablesRegion getRegion();
	
	
	/** Gets the resource location {@link Identifier} for this slot type,
	 *  such as {@code "wearables:textures/gui/icons/chest_neck_amulet.png"}. */
	public Identifier getIcon();
	
	/** Gets the order for slots of this type, which define whether they're "above" or "below"
	 *  other slots. Smaller number is above, larger is below. 0 is for the Vanilla armor layer. */
	public int getOrder();
	
	/** Gets the number of slots of this type. For example, multiple rings may be equipped. */
	public int getSlotCount();
	
	
	/** Gets the Vanilla {@link EquipmentSlot} this slot is equivalent with, such as
	 *  {@link EquipmentSlot#CHEST} for {@code chest:armor/chestplate}, or {@code null} if none. */
	public EquipmentSlot getVanilla();
	
	/** Returns whether this slot can be accessed through a Vanilla
	 *  {@link EquipmentSlot}, such as {@code chest:armor/chestplate}. */
	public default boolean isVanilla()
		{ return (getVanilla() != null); }
	
	
	/** Returns whether the specified stack may be worn in this type of slot. */
	public default boolean isValid(ItemStack stack)
		{ return stack.isEmpty() || IWearablesData.INSTANCE.getValidSlots(stack).contains(this); }
}
