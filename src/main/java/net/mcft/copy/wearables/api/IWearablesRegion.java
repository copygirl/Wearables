package net.mcft.copy.wearables.api;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Represents an "equipment region" which can be thought of as a general physical
 * location on an entity's body, such as {@code chest}, {@code feet} and {@code back}.
 * <p>
 * Groups together one or more {@link IWearablesSlotType}s, allowing them to be
 * neatly accessed through a single virtual slot in the player's inventory GUI.
 * <p>
 * TODO: Allow the definition of custom regions through data packs.
 */
public interface IWearablesRegion
{
	public static final Pattern REGION_REGEX
		= Pattern.compile("[a-z]+");
	
	
	/** Gets the name of this region, such as {@code chest}. */
	public String getName();
	
	/** Gets a collection of slot types that belong into this region,
	 *  such as {@code chest:armor/chestplate} and {@code chest:neck/amulet}. */
	public Collection<IWearablesSlotType> getSlotTypes();
	
	/** Returns whether this region is based on a Vanilla armor slot. */
	public boolean isVanilla();
}
