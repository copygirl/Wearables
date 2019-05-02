package net.mcft.copy.wearables.api;

import java.util.Collection;

import net.mcft.copy.wearables.common.impl.WearablesDataImpl;

import net.minecraft.item.ItemStack;

public interface IWearablesData
{
	public static final IWearablesData INSTANCE = new WearablesDataImpl();
	
	
	/**
	 * Returns all registered {@link WearablesRegion}s, such as
	 * {@link WearablesRegion#CHEST} and {@link WearablesRegion#BACK}.
	 * <p>
	 * Use {@link IWearablesEntity#getRegions()} to get entity-specific regions.
	 */
	public Collection<IWearablesRegion> getRegions();
	
	/**
	 * Returns all registered {@link WearablesSlotType}s, such as
	 * {@code head:armor/helmet}, {@code chest:neck/amulet}, and {@code back:carry}.
	 * <p>
	 * Use {@link IWearablesEntity#getSlotTypes()} to get entity-specific slot types.
	 */
	public Collection<IWearablesSlotType> getSlotTypes();
	
	/**
	 * Returns the {@link WearablesRegion} with the specified region
	 * or {@link WearablesSlotType} name, or {@code null} if none.
	 * 
	 * @param nameOrSlot A region or slot type name such as {@code "chest"} or {@code "chest:neck/amulet"}.
	 * @return A region such as {@link WearablesRegion#CHEST} or {@code null}.
	 * @exception IllegalArgumentException Thrown if nameOrSlot is {@code null} or empty.
	 **/
	public IWearablesRegion getRegion(String nameOrSlot);
	
	/**
	 * Returns the {@link WearablesSlotType} with the specified full name, or {@code null} if none.
	 * 
	 * @param fullName The slot type's full name such as {@code "chest:neck/amulet"}.
	 * @return A slot type with the specified name or {@code null}.
	 * @exception IllegalArgumentException Thrown if fullName is {@code null} or empty.
	 */
	public IWearablesSlotType getSlotType(String fullName);
	
	/**
	 * Returns a collection of {@link WearablesSlotType} into
	 * which the specified {@link ItemStack} may be equipped.
	 * 
	 * @param stack The {@link ItemStack} to be checked.
	 * @return A collection of slot types the stack is valid in, or an empty collection if none.
	 * @exception IllegalArgumentException Thrown if stack is {@value null}.
	 */
	public Collection<IWearablesSlotType> getValidSlots(ItemStack stack);
}
