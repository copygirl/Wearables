package net.mcft.copy.wearables.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.container.Container;

public final class WearablesContainerRegistry
{
	private WearablesContainerRegistry() {  }
	
	
	private static final Map<Class<Container>, String> CLASS_LOOKUP = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public static void register(String identifier, Class<? extends Container> clazz)
	{
		if ((identifier == null) || identifier.isEmpty()) throw new IllegalArgumentException("identifier is null or empty");
		if (clazz == null) throw new IllegalArgumentException("clazz is null");
		CLASS_LOOKUP.put((Class<Container>)clazz, identifier);
	}
	
	public static Optional<String> find(Class<?> clazz)
		{ return Optional.ofNullable(CLASS_LOOKUP.get(clazz)); }
}
