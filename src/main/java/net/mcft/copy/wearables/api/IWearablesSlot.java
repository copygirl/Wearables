package net.mcft.copy.wearables.api;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/** Interface for a specific slot on an entity into which a wearable item may be equipped. */
public interface IWearablesSlot
{
	/** Gets the entity this slot is attached to. */
	public Entity getEntity();
	
	/** Gets the {@link WearablesSlotType} of this slot. */
	public WearablesSlotType getSlotType();
	
	/** Gets the numeric index of the slot, differenciating it from other slots of the same type. */
	public int getIndex();
	
	/** Gets the "order" of this slot, whether it's "above" or "below" other slots.
	 *  Smaller number is above, larger is below. Always 0 for Vanilla slots. Default is 500. */
	public int getOrder();
	
	/**
	 * Gets whether this slot is valid / supported by this entity.
	 * This is {@code false} if the slot type is not supported or the
	 * index is equal to or larger than the configured slot count.
	 * <p>
	 * Invalid slots might be returned by the methods
	 * {@link IWearablesEntity#getEquippedWearables getEquippedWearables} and
	 * {@link IWearablesEntity#getWearablesSlot getWearablesSlot(..., true)}.
	 * <p>
	 * An invalid slot still allows setting the stack, but
	 * it should only ever be set to {@link ItemStack#EMPTY}.
	 */
	public boolean isValid();
	
	
	/** Gets the {@link ItemStack} currently contained
	 *  in this slot, or {@link ItemStack#EMPTY} if empty. */
	public ItemStack get();
	
	/** Sets the {@link ItemStack} contained in this slot, without
	 *  checking if the specified stack is valid or can be equipped. */
	public void set(ItemStack value);
	
	
	/**
	 * Returns if the specified {@link ItemStack} can be equipped in this slot.
	 * Always returns {@code true} if the specified stack is {@code null}.
	 * <p>
	 * Does not check if a currently equipped stack can be unequipped.
	 */
	public default boolean canEquip(ItemStack stack)
	{
		return stack.isEmpty() ||
		       (isValid() && IWearablesEntity.from(getEntity()).getValidSlots(stack.getItem()).contains(this)
		                  && IWearablesItem.from(stack.getItem()).canEquip(this, stack));
	}
	
	/** Returns if the {@link ItemStack} currently contained in this slot can be
	 *  unequipped. Always returns {@code true} if the current stack is {@code null}. */
	public default boolean canUnequip()
	{
		return get().isEmpty() ||
		       ((getEntity() instanceof PlayerEntity) && ((PlayerEntity)getEntity()).isCreative()) ||
		       (!EnchantmentHelper.hasBindingCurse(get()) && IWearablesItem.from(get().getItem()).canUnequip(this));
	}
	
	
	/**
	 * Intended to be called by {@link IWearablesSlotHandler} implementations to
	 * run internal code meant to run before a stack is changed. For example, this
	 * causes {@link IWearablesItem#onUnequip} to be called on the specified stack.
	 */
	public void invokeBeforeUnequip(ItemStack previousStack);
	
	/**
	 * Intended to be called by {@link IWearablesSlotHandler} implementations to
	 * run internal code meant to run after a stack is changed. For example, this
	 * causes {@link IWearablesItem#onEquip} to be called on the specified stack.
	 */
	public void invokeAfterEquip(ItemStack currentStack);
}
