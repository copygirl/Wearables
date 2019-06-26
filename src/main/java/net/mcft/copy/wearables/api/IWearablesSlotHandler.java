package net.mcft.copy.wearables.api;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

/**
 * Allows registering of additional non-Wearables
 * slots per entity type, to expose them to Wearables.
 * <p>
 * For example, this is done for Vanilla armor slots
 * {@code head/helmet}, {@code chest/chestplate} and so on.
 * <p>
 * The handler must ensure that {@link IWearablesSlot#invokeBeforeUnequip}
 * and {@link IWearablesSlot#invokeAfterEquip} are called to allow Wearables
 * to function properly.
 */
public interface IWearablesSlotHandler<TEntity extends Entity>
{
	/** Global registry of slot handlers and entity classes. Internal use only. */
	public static final List<Pair<IWearablesSlotHandler<Entity>, Class<Entity>>> REGISTRY = new ArrayList<>();
	
	/** Registers the specified handler for the specified entity class.
	 *  @throws IllegalArgumentException Thrown if clazz or handler is {@code null}. */
	@SuppressWarnings("unchecked")
	public static <TEntity extends Entity> void register(Class<TEntity> clazz, IWearablesSlotHandler<TEntity> handler)
	{
		if (handler == null) throw new IllegalArgumentException("handler is null");
		REGISTRY.add(new Pair<IWearablesSlotHandler<Entity>, Class<Entity>>(
			(IWearablesSlotHandler<Entity>)handler, (Class<Entity>)clazz));
	}
	
	
	/** Returns an iterable of slot types this handler exposes for
	 *  the specified entity type, or an empty iterable if none. */
	public Iterable<WearablesSlotType> getSlotTypes(EntityType<TEntity> entityType);
	
	/** Returns the custom sprite icon for the specified (handled) slot type.
	 *  For example {@code minecraft:item/empty_armor_slot_helmet}. */
	public Identifier getIcon(WearablesSlotType slotType);
	
	
	/** Gets the {@link ItemStack} contained in the specified (handled)
	 *  slot for the specified entity, or an empty stack if none. */
	public ItemStack get(TEntity entity, IWearablesSlot slot);
	
	/** Sets the {@link ItemStack} contained in the
	 *  specified (handled) slot for the specified entity. */
	public void set(TEntity entity, IWearablesSlot slot, ItemStack value);
}
