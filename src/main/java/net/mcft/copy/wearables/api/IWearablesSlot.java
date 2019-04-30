package net.mcft.copy.wearables.api;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface IWearablesSlot
{
	/** Gets the entity this slot is attached to. */
	public LivingEntity getEntity();
	
	/** Gets the {@link WearablesSlotType WearablesSlotType} of this slot. */
	public WearablesSlotType getSlotType();
	
	/** Gets the {@link WearablesRegion WearablesRegion} of this slot. */
	public default WearablesRegion getRegion() { return getSlotType().region; }
	
	/** Gets the "order" of this slot, whether it's "above" or "below" other slots.
	 *  Smaller number is above, larger is below. 0 is for the Vanilla armor layer. */
	// TODO: Eventually, order may change. For example, an amulet may be worn above or below the chestplate.
	public default int getOrder() { return getSlotType().getOrder(); }
	
	
	/** Gets the stack currently contained in this slot,
	 *  or {@link ItemStack#EMPTY ItemStack.EMPTY} if empty. */
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
