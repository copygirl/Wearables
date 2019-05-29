package net.mcft.copy.wearables.api;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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
	
	
	/** Gets the {@link ItemStack} currently contained
	 *  in this slot, or {@link ItemStack#EMPTY} if empty. */
	public ItemStack get();
	
	/** Sets the {@link ItemStack} contained in this slot, without
	 *  checking if the specified stack is valid or can be equipped. */
	public void set(ItemStack value);
	
	
	/**
	 * Returns if the specified {@link ItemStack} can be equipped in this slot.
	 * Does not check if a currently equipped stack can be unequipped.
	 * Returns {@code true} if the specified stack is {@code null}.
	 */
	public default boolean canEquip(ItemStack stack)
	{
		return stack.isEmpty()
		    || IWearablesData.INSTANCE.getValidSlots(stack).contains(getSlotType())
		    && IWearablesItem.from(stack.getItem()).canEquip(this, stack);
	}
	
	/** Returns if the {@link ItemStack} currently contained in this slot can
	 *  be unequipped. Returns {@code true} if the current stack is {@code null}. */
	public default boolean canUnequip()
	{
		return ((getEntity() instanceof PlayerEntity) && ((PlayerEntity)getEntity()).isCreative())
		    || !EnchantmentHelper.hasBindingCurse(get())
		    && IWearablesItem.from(get().getItem()).canUnequip(this);
	}
}
