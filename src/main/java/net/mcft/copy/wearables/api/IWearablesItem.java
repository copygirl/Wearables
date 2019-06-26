package net.mcft.copy.wearables.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Implemented by {@link Item Items} to add additional
 *  control over equipment rules and a tick handler. */
public interface IWearablesItem
{
	/**
	 * Returns whether the specified {@link ItemStack} can be equipped in the
	 * specified {@link IWearablesSlot} by the entity. This doesn't necessarily
	 * prevent an item from being equipped through other means.
	 * <p>
	 * Note that which slots an item can be equipped into is controlled by
	 * configuration stored in data packs under {@code config/wearables/item/}
	 * and not just this method.
	 * 
	 * @param slot The slot to be checked.
	 *             Use {@link IWearablesSlot#getEntity()} to access the entity
	 *             (not guaranteed to be a {@link PlayerEntity}).
	 * @param stack The exact stack to be checked whether it can be equipped.
	 */
	public default boolean canEquip(IWearablesSlot slot, ItemStack stack) { return true; }
	
	/**
	 * Returns whether this item can be unequipped from the specified
	 * {@link IWearablesSlot} by the entity. This doesn't necessarily prevent
	 * the item from being unequipped through other means, such as death.
	 * 
	 * @param slot The slot to be checked.
	 *             Use {@link IWearablesSlot#get()} to get the item to be
	 *             checked whether it can be unequipped.
	 *             Use {@link IWearablesSlot#getEntity()} to access the entity
	 *             (not guaranteed to be a {@link PlayerEntity}).
	 */
	public default boolean canUnequip(IWearablesSlot slot) { return true; }
	
	
	/**
	 * Called right after this item is equipped in the specified {@link IWearablesSlot},
	 * both on the client (which isn't authoritative) and the server.
	 * 
	 * @param slot The slot the item got equipped to.
	 *             Use {@link IWearablesSlot#get()} to get the full
	 *             {@link ItemStack} that got equipped.
	 *             Use {@link IWearablesSlot#getEntity()} to access the entity
	 *             (not guaranteed to be a {@link PlayerEntity}).
	 */
	public default void onEquip(IWearablesSlot slot) {  }
	
	/**
	 * Called right before this item is unequipped from the specified
	 * {@link IWearablesSlot}. Also called when the item is dropped / broken,
	 * due to death or taking damage.
	 * 
	 * @param slot The slot the item is being uneqipped from.
	 *             Use {@link IWearablesSlot#get()} to get the full
	 *             {@link ItemStack} about to be unequipped.
	 *             Use {@link IWearablesSlot#getEntity()} to access the entity
	 *             (not guaranteed to be a {@link PlayerEntity}).
	 */
	public default void onUnequip(IWearablesSlot slot) {  }
	
	
	/** Returns whether {@link #onEquippedTick} should be called for this item. */
	public default boolean doesTick() { return false; }
	
	/**
	 * Called each tick while this item is equipped in the specified {@link IWearablesSlot}.
	 * Only called if {@link #doesTick()} returns true (false by default!).
	 * 
	 * @param slot The slot the item is currently equipped in.
	 *             Use {@link IWearablesSlot#get()} to get the full
	 *             {@link ItemStack} that is currently equipped.
	 *             Use {@link IWearablesSlot#getEntity()} to access the entity
	 *             (not guaranteed to be a {@link PlayerEntity}).
	 * @param equippedTime The number of ticks (usually 20th of a second) since
	 *                     the item got equipped. Can be used to run code only
	 *                     every number of ticks: <p>
	 *                     {@code if ((equippedTime % 20) != 0) return;}
	 */
	public default void onEquippedTick(IWearablesSlot slot, int equippedTime) {  }
	
	
	/** Returns the specified {@link Item} casted as an
	 *  {@link IWearablesItem}, or {@link #DUMMY} if it isn't. */
	public static IWearablesItem from(Item item)
		{ return (item instanceof IWearablesItem) ? (IWearablesItem)item : DUMMY; }
	
	public static final IWearablesItem DUMMY = new IWearablesItem(){};
}
