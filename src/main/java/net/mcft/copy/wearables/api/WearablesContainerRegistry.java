package net.mcft.copy.wearables.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.container.Container;

/**
 * Allows registering container classes, associating them with a
 * string identifier. This string identifier can then be used in
 * data packs to refer to this specific container class.
 */
public final class WearablesContainerRegistry
{
	private WearablesContainerRegistry() {  }
	
	
	private static final Map<Class<Container>, String> LOOKUP = new HashMap<>();
	
	/** Registers the specified container class under the specified identifier. */
	@SuppressWarnings("unchecked")
	public static void register(String identifier, Class<? extends Container> clazz)
	{
		if ((identifier == null) || identifier.isEmpty()) throw new IllegalArgumentException("identifier is null or empty");
		if (clazz == null) throw new IllegalArgumentException("clazz is null");
		LOOKUP.put((Class<Container>)clazz, identifier);
	}
	
	/** Gets the associated identifier for the specified
	 *  container class, or {@link Optional#empty} if none. */
	public static Optional<String> find(Class<?> clazz)
		{ return Optional.ofNullable(LOOKUP.get(clazz)); }
}
