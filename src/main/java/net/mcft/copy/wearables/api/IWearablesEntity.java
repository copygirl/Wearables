package net.mcft.copy.wearables.api;

import java.util.Collection;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;

/**
 * Implemented by all entities extending {@link LivingEntity} through mixin.
 * Allows accessing an entity's {@link IWearableSlot IWearableSlots}, getting
 * and setting which items are equipped through Wearables.
 * <p>
 * TODO: Entity-type-specific regions and slots. (Creepers have no arms!)
 */
public interface IWearablesEntity
{
	/** Returns this entity's available {@link WearablesRegion WearablesRegions},
	 *  such as {@link WearablesRegion#CHEST} and {@link WearablesRegion#BACK}. */
	public Collection<IWearablesRegion> getWearablesRegions();
	
	/** Returns this entity's available {@link WearablesSlotType WearablesSlotTypes}, such
	 *  as {@code head:armor/helmet}, {@code chest:neck/amulet}, and {@code back:carry}. */
	public Collection<IWearablesSlotType> getWearablesSlotTypes();
	
	/**
	 * Returns this entity's available {@link WearablesSlotType WearablesSlotTypes} for the specified
	 * {@link WearablesRegion}, such as {@code chest:armor/chestplate}, {@code chest:neck/amulet}, and
	 * {@code chest:clothing/shirt}, or an empty collection if region is not valid for this entity.
	 * @exception IllegalArgumentException Thrown if region is {@code null}.
	 */
	public Collection<IWearablesSlotType> getWearablesSlotTypes(IWearablesRegion region);
	
	
	/** Returns whether this entity has any items equipped in custom Wearables slots.
	 *  Does not consider Vanilla armor slots such as {@code chest:armor/chestplate}. */
	public boolean hasWearables();
	
	
	public IWearablesSlot getWearablesSlot(IWearablesSlotType slotType, int index);
	
	public default IWearablesSlot getWearablesSlot(EquipmentSlot slot)
		{ return getWearablesSlot(IWearablesData.INSTANCE.getSlotType(slot), 0); }
	
	
	public default Stream<IWearablesSlot> getAvailableWearablesSlots()
	{
		return getWearablesSlotTypes().stream()
			.flatMap(this::getAvailableWearablesSlots);
	}
	
	public default Stream<IWearablesSlot> getAvailableWearablesSlots(IWearablesRegion region)
	{
		if (region == null) throw new IllegalArgumentException("region is null");
		// TODO: Throw if region is not valid for this entity?
		return getWearablesSlotTypes(region).stream()
			.flatMap(this::getAvailableWearablesSlots);
	}
	
	public default Stream<IWearablesSlot> getAvailableWearablesSlots(IWearablesSlotType slotType)
	{
		if (slotType == null) throw new IllegalArgumentException("slotType is null");
		// TODO: Throw if slotType is not valid for this entity?
		return IntStream.range(0, slotType.getSlotCount())
			.mapToObj(index -> getWearablesSlot(slotType, index));
	}
	
	
	public Stream<IWearablesSlot> getEquippedWearables();
	
	public Stream<IWearablesSlot> getEquippedWearables(IWearablesRegion region);
	
	public Stream<IWearablesSlot> getEquippedWearables(IWearablesSlotType slotType);
}
