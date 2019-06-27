package net.mcft.copy.wearables.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;

@Environment(EnvType.CLIENT)
public final class WearablesScreenRegistry
{
	private WearablesScreenRegistry() {  }
	
	
	private static final Map<Class<AbstractContainerScreen<?>>, String> CLASS_LOOKUP = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public static void register(String identifier, Class<? extends AbstractContainerScreen<?>> clazz)
	{
		if ((identifier == null) || identifier.isEmpty()) throw new IllegalArgumentException("identifier is null or empty");
		if (clazz == null) throw new IllegalArgumentException("clazz is null");
		CLASS_LOOKUP.put((Class<AbstractContainerScreen<?>>)clazz, identifier);
	}
	
	public static Optional<String> find(Class<?> clazz)
		{ return Optional.ofNullable(CLASS_LOOKUP.get(clazz)); }
}
