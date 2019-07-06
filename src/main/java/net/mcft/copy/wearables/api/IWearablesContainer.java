package net.mcft.copy.wearables.api;

import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

/**
 * Implemented on containers which can support Wearables slots.
 * <p>
 * Actual slots are defined through data packs, in this case
 * {@code config/wearables/container}. This interface simply
 * defines which entities are exposed to the configuration.
 */
public interface IWearablesContainer
{
	/** Returns an {@link Identifier} that can
	 *  be used to refer to this container. */
	public Identifier getWearablesIdentifier();
	
	
	/** Returns the default entity for slots
	 *  when not provided with an explicit key. */
	public Entity getWearablesDefaultEntity();
	
	/**
	 * Returns a map of all entities accessible through this
	 * container mapped by the key the can be accessed by.
	 * <p>
	 * The same entity may occur multiple times under different keys.
	 */
	public Map<String, Entity> getWearablesEntityMap();
}
