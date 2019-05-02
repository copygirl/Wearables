package net.mcft.copy.wearables.api;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

/** Interface for a specific slot on an entity into which a wearable item may be equipped. */
public interface IWearablesSlot
{
	/** Gets the entity this slot is attached to. */
	public LivingEntity getEntity();
	
	/** Gets the {@link WearablesSlotType} of this slot. */
	public IWearablesSlotType getSlotType();
	
	/** Gets the numeric index of the slot, differenciating it from other slots of the same type. */
	public int getIndex();
	
	/** Gets the {@link WearablesRegion} of this slot. */
	public default IWearablesRegion getRegion()
		{ return getSlotType().getRegion(); }
	
	/**
	 * Gets the "order" of this slot, whether it's "above" or "below" other slots.
	 * Smaller number is above, larger is below. Always 0 for Vanilla slots. Default is 500.
	 * <p>
	 * TODO: Eventually, order may change on the fly. For example, an amulet could be worn above or below the chestplate.
	 */
	public default int getOrder()
		{ return getSlotType().getOrder(); }
	
	
	/** Gets the stack currently contained in this slot, or {@link ItemStack#EMPTY} if empty. */
	public ItemStack get();
	
	/** Sets the stack contained in this slot, without checking
	 *  if the specified stack is valid or can be equipped.
	 *  @exception IllegalArgumentException Thrown if stack is null. */
	public void set(ItemStack value);
	
	/** Returns if the specified stack can be equipped in this slot. */
	public boolean canEquip(ItemStack stack);
	
	/** Returns if the currently contained stack can be unequipped. */
	public default boolean canUnequip()
		{ return !EnchantmentHelper.hasBindingCurse(get()); }
}
