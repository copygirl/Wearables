package net.mcft.copy.wearables.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.mcft.copy.wearables.api.IWearablesEntity;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;

/**
 * Allows registering of "special items" that can be
 * handled by the Wearables equipment rule system.
 * <p>
 * For example, Vanilla armor is registered as {@code ItemArmor:helmet} etc.
 * and can be referred to in data packs under {@code config/wearables/item/}
 * as {@code "!ItemArmor:helmet"} and so on.
 * <p>
 * To test whether an item may be equipped in a particular slot or slot type, use
 * {@link IWearablesEntity#getValidSlots} and {@link IWearablesEntity#getValidSlotTypes}.
 */
public interface IWearablesItemHandler
{
	/** Global registry of item handlers. Internal use only. */
	public static final List<IWearablesItemHandler> REGISTRY = new ArrayList<>();
	/** Global all valid "special item" strings. Internal use only. */
	public static final Set<String> VALID_SPECIAL_ITEMS = new HashSet<>();
	
	/** Registers the specified handler, which can handle certain "special items".
	 *  @throws IllegalArgumentException Thrown if handler is {@code null}. */
	public static <TEntity extends Entity> void register(IWearablesItemHandler handler)
	{
		if (handler == null) throw new IllegalArgumentException("handler is null");
		VALID_SPECIAL_ITEMS.addAll(handler.getHandledSpecialItems());
		REGISTRY.add(handler);
	}
	
	
	/** Returns a collection of "special item" strings this handler provides. */
	public Collection<String> getHandledSpecialItems();
	
	/**
	 * Returns a "special item" string if the specified {@link Item} is
	 * handled by this instance, or {@link Optional#empty} if it isn't.
	 * <p>
	 * The returned string must be one contained in the
	 * collection returned by {@link #getHandledSpecialItems}.
	 */
	public Optional<String> getSpecialItem(Item item);
}
