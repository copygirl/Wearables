package net.mcft.copy.wearables.api;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import net.mcft.copy.wearables.common.data.WearablesData;
import net.mcft.copy.wearables.common.impl.WearablesEntityImpl;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;

/**
 * Allows accessing an entity's {@link IWearableSlot IWearableSlots}, getting
 * and setting which items are equipped through Wearables.
 * <p>
 * Use {@link IWearablesEntity#is} to test whether an entity may support Wearables and
 * {@link IWearablesEntity#from} to safely get an IWearablesEntity. This is to account
 * for the later possibility of implementing Wearables using component-style properties.
 */
public interface IWearablesEntity
{
	/** Returns whether this entity has any items equipped
	 *  in Wearables slots, including in Vanilla armor slots. */
	public boolean hasWearables();
	
	/**
	 * Returns this entity's supported regions, such as
	 * {@link WearablesRegion#CHEST} or {@link WearablesRegion#BACK}.
	 * 
	 * @return A collection of regions, which group together slot types, for this entity,
	 *         or an empty collection if none or the entity doesn't support Wearables.
	 */
	public Collection<WearablesRegion> getWearablesRegions();
	
	
	/**
	 * Returns a stream of {@link IWearablesSlot} in which this entity has equipped Wearables.
	 * <p>
	 * May also include items worn in invalid slots. See {@link IWearablesSlot#isValid}.
	 * 
	 * @return A stream of slots in which there is a worn item, or an empty
	 *         stream if none, or the entity doesn't support Wearables.
	 */
	public Stream<IWearablesSlot> getEquippedWearables();
	
	/**
	 * Returns a stream of {@link IWearablesSlot} in the specified
	 * {@link WearablesRegion} in which this entity has equipped Wearables.
	 * <p>
	 * May also include items worn in invalid slots. See {@link IWearablesSlot#isValid}.
	 * 
	 * @param region A region such {@link WearablesRegion#CHEST} or {@link WearablesRegion#BACK}.
	 * @throws IllegalArgumentException Thrown if region is {@code null}.
	 * @return A stream of slots in which there is a worn item, or an empty
	 *         stream if none, or the entity doesn't support Wearables.
	 */
	public Stream<IWearablesSlot> getEquippedWearables(WearablesRegion region);
	
	/**
	 * Returns a stream of {@link IWearablesSlot} in slots with the specified
	 * {@link WearablesSlotType} in which this entity has equipped Wearables.
	 * <p>
	 * May also include items worn in invalid slots. See {@link IWearablesSlot#isValid}.
	 * 
	 * @param slotType A slot type such as {@link WearablesSlotType#CHESTPLATE}.
	 * @throws IllegalArgumentException Thrown if slotType is {@code null}.
	 * @return A stream of slots in which there is a worn item, or an empty
	 *         stream if none, or the entity doesn't support Wearables.
	 */
	public Stream<IWearablesSlot> getEquippedWearables(WearablesSlotType slotType);
	
	
	/**
	 * Returns this entity's supported {@link WearablesSlotType WearablesSlotTypes},
	 * such as {@link WearablesSlotType#CHESTPLATE}.
	 * 
	 * @return A collection of slot types for any region, into which Wearables may be worn, for
	 *         this entity, or an empty collection if none or the entity doesn't support Wearables.
	 */
	public Collection<WearablesSlotType> getSupportedWearablesSlotTypes();
	
	/**
	 * Returns this entity's supported {@link WearablesSlotType WearablesSlotTypes}
	 * for the specified region, such as {@link WearablesSlotType#CHESTPLATE}.
	 * 
	 * @throws IllegalArgumentException Thrown if region is {@code null}.
	 * @return A collection of slot types for the specified region into which
	 *         Wearables may be worn for this entity, or an empty collection if
	 *         none, the entity doesn't support Wearables or the specified region.
	 */
	public Collection<WearablesSlotType> getSupportedWearablesSlotTypes(WearablesRegion region);
	
	
	/**
	 * Returns a stream of {@link IWearablesSlot IWearablesSlots}
	 * this entity supports. Always includes Vanilla armor slots.
	 * 
	 * @return A collection of slots supported by this entity, or
	 *         an empty stream if it doesn't support Wearables.
	 */
	public Stream<IWearablesSlot> getSupportedWearablesSlots();
	
	/**
	 * Returns a stream of {@link IWearablesSlot IWearablesSlots} in the specified
	 * region which this entity supports. Always includes Vanilla armor slots.
	 * <p>
	 * You may use {@code getSupportedRegions().contains(region)} to test
	 * whether the specified region is supported for this entity, first.
	 * 
	 * @throws IllegalArgumentException Thrown if region is {@code null}.
	 * @return A collection of slots supported by this entity, or an empty stream if
	 *         it doesn't support Wearables at all, or just not the specified region.
	 */
	public Stream<IWearablesSlot> getSupportedWearablesSlots(WearablesRegion region);
	
	/**
	 * Returns a stream of {@link IWearablesSlot IWearablesSlots} of the specified slot types
	 * which this entity supports. Always returns {@link IWearablesSlotType#getSlotCount} slots.
	 * <p>
	 * You may use {@code getSupportedWearablesSlotTypes().contains(slotType)} to
	 * test whether the specified slot type is supported for this entity, first.
	 * 
	 * @throws IllegalArgumentException Thrown if region is {@code null}.
	 * @return A stream of slots supported by this entity, or an empty stream if it
	 *         doesn't support Wearables at all, or just not the specified slot type.
	 */
	public Stream<IWearablesSlot> getSupportedWearablesSlots(WearablesSlotType slotType);
	
	
	/**
	 * Creates and returns a set of {@link WearablesSlotType WearablesSlotTypes}
	 * this entity supports into which the specified {@link Item} may be
	 * equipped according to item configuration from loaded data packs.
	 * 
	 * @throws IllegalArgumentException Thrown if item is {@code null}.
	 * @return A set of slot types that are valid for the specified item, or an empty
	 *         set if none were found or this entity doesn't support Wearables.
	 */
	public Set<WearablesSlotType> getValidSlotTypes(Item item);
	
	/**
	 * Creates and returns a set of {@link IWearablesSlot IWearablesSlots}
	 * this entity supports into which the specified {@link Item} may be
	 * equipped according to item configuration from loaded data packs.
	 * 
	 * @throws IllegalArgumentException Thrown if item is {@code null}.
	 * @return A set of slots that are valid for the specified item, or an empty
	 *         set if none were found or this entity doesn't support Wearables.
	 */
	public Set<IWearablesSlot> getValidSlots(Item item);
	
	
	/**
	 * Returns a specific {@link IWearablesSlot} of the specified slot type and index.
	 * 
	 * @param slotType The slot type of which to get a slot from.
	 * @param index    The slot type relative index of which to get a slot from.
	 * @param force    If {@code true}, may return an invalid slot. See {@link IWearablesSlot#isValid}.
	 *                 If {@code false}, instead throws an exception in those cases.
	 * 
	 * @throws IllegalArgumentException Thrown if slotType is {@code null} or index is negative.
	 * @throws UnsupportedOperationException Thrown if this entity doesn't support Wearables.
	 *                                       You may test this using {@link IWearablesEntity#is}.
	 *                                       If force is {@code false}, also thrown if slotType or index is not supported.
	 * @return A slot for this entity which may be used to get and set the worn Wearable in it.
	 */
	public IWearablesSlot getWearablesSlot(WearablesSlotType slotType, int index, boolean force);
	
	/**
	 * Returns a specific {@link IWearablesSlot} of the specified slot type and index.
	 * 
	 * @param slotType The slot type of which to get a slot from.
	 * @param index    The slot type relative index of which to get a slot from.
	 * 
	 * @throws IllegalArgumentException Thrown if slotType is {@code null} or index is negative.
	 * @throws UnsupportedOperationException Thrown if this entity doesn't support Wearables.
	 *                                       You may test this using {@link IWearablesEntity#is}.
	 *                                       Also thrown if slotType or index is not supported.
	 * @return A slot for this entity which may be used to get and set the worn Wearable in it.
	 */
	public default IWearablesSlot getWearablesSlot(WearablesSlotType slotType, int index)
		{ return getWearablesSlot(slotType, index, false); }
	
	/**
	 * Returns a specific {@link IWearablesSlot} that is associated with the specified Vanilla armor {@link EquipmentSlot}.
	 * 
	 * @param slot  The Vanilla equipment slot which to get an associated Wearables slot for.
	 * @param force If {@code true}, may return an invalid slot. See {@link IWearablesSlot#isValid}.
	 *              If {@code false}, instead throws an exception in those cases.
	 * 
	 * @throws IllegalArgumentException Thrown if slot is {@code null} or its type is not {@link EquipmentSlot.Type#ARMOR}.
	 * @throws UnsupportedOperationException Thrown if this entity doesn't support Wearables.
	 *                                       You may test this using {@link IWearablesEntity#is}.
	 *                                       If force is {@code false}, also thrown if the {@link WearablesSlotType}
	 *                                       associated with the specified slot is not supported for this entity.
	 * @return A slot for this entity which may be used to get and set the worn Wearable in it.
	 */
	public default IWearablesSlot getWearablesSlot(EquipmentSlot slot, boolean force)
		{ return getWearablesSlot(WearablesSlotType.fromVanillaSlot(slot), 0, force); }
	
	/**
	 * Returns a specific {@link IWearablesSlot} that is associated with the specified Vanilla armor {@link EquipmentSlot}.
	 * 
	 * @param slot The Vanilla equipment slot which to get an associated Wearables slot for.
	 * 
	 * @throws IllegalArgumentException Thrown if slot is {@code null} or its type is not {@link EquipmentSlot.Type#ARMOR}.
	 * @throws UnsupportedOperationException Thrown if this entity doesn't support Wearables.
	 *                                       You may test this using {@link IWearablesEntity#is}.
	 *                                       Also thrown if the {@link IWearablesSlotType} associated
	 *                                       with the specified slot is not supported for this entity.
	 * @return A slot for this entity which may be used to get and set the worn Wearable in it.
	 */
	public default IWearablesSlot getWearablesSlot(EquipmentSlot slot)
		{ return getWearablesSlot(slot, false); }
	
	
	/**
	 * Returns whether the specified entity may have Wearables equipped.
	 * @throws IllegalArgumentException Thrown if entity is {@code null}.
	 */
	public static boolean is(Entity entity)
	{
		if (entity == null) throw new IllegalArgumentException("entity is null");
		return WearablesData.INSTANCE.entities.containsKey(entity.getType());
	}
	
	/**
	 * Returns an {@link IWearablesEntity} of the specified
	 * {@link Entity}, or a dummy if it doesn't support Wearables.
	 * <p>
	 * The dummy implementation will support accessing Vanilla armor slots
	 * through {@link IWearablesSlot} (such as {@code chest/chestplate}) if
	 * the entity is a {@link net.minecraft.entity.LivingEntity LivingEntity}.
	 * 
	 * @throws IllegalArgumentException Thrown if entity is {@code null}.
	 */
	public static IWearablesEntity from(Entity entity)
		{ return new WearablesEntityImpl(entity); }
}
