package net.mcft.copy.wearables.api;

import java.util.regex.Pattern;

/**
 * Represents an "equipment region" which can be thought of as a general physical
 * location on an entity's body, such as {@code chest}, {@code feet} and {@code back}.
 * <p>
 * They group together one or more {@link WearablesSlotType WearablesSlotTypes}, allowing
 * them to be neatly accessed through a single virtual slot in the player's inventory GUI.
 */
public final class WearablesRegion
{
	public static final Pattern REGION_REGEX =
		Pattern.compile("[a-z]+");
	
	// Regions based on Vanilla armor slots.
	public static final WearablesRegion HEAD  = new WearablesRegion("head");
	public static final WearablesRegion CHEST = new WearablesRegion("chest");
	public static final WearablesRegion LEGS  = new WearablesRegion("legs");
	public static final WearablesRegion FEET  = new WearablesRegion("feet");
	
	// Additional recommended default regions.
	public static final WearablesRegion BACK = new WearablesRegion("back");
	public static final WearablesRegion ARMS = new WearablesRegion("arms");
	
	
	/** The name of this region, such as {@code "chest"} or {@code "back"}. */
	public final String name;
	
	public WearablesRegion(String name)
	{
		if ((name == null) || name.isEmpty())
			throw new IllegalArgumentException("name is null or empty");
		if (!REGION_REGEX.matcher(name).matches())
			throw new IllegalArgumentException("name '" + name + "' is not a valid slot type string");
		this.name = name;
	}
	
	
	@Override
	public String toString()
		{ return name; }
	
	@Override
	public boolean equals(Object obj)
	{
		return (obj instanceof WearablesRegion)
		    && ((WearablesRegion)obj).name.equals(this.name);
	}
	
	@Override
	public int hashCode()
		{ return this.name.hashCode(); }
}
