package net.mcft.copy.wearables.common.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.mcft.copy.wearables.api.IWearablesSlotHandler;
import net.mcft.copy.wearables.api.WearablesRegion;
import net.mcft.copy.wearables.api.WearablesSlotType;
import net.mcft.copy.wearables.common.data.DataManager.Exclude;
import net.mcft.copy.wearables.common.impl.slot.DefaultSlotHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Pair;

public class EntityTypeData
{
	public static EntityTypeData from(Entity entity)
	{
		if (entity == null) throw new IllegalArgumentException("entity is null");
		EntityTypeData data = WearablesData.INSTANCE.entities
			.computeIfAbsent(entity.getType(), e -> new EntityTypeData());
		if (!data._handlerCached) data.cacheHandler(entity);
		return data;
	}
	
	
	public Set<WearablesRegion> regions = new HashSet<>();
	public Map<WearablesSlotType, SlotTypeData> slotTypes = new HashMap<>();
	public Map<WearablesRegion, Set<WearablesSlotType>> slotTypesByRegion = new HashMap<>();
	
	
	public static class SlotTypeData
	{
		public int slotCount = 1;
		public int order     = 500;
		
		@Exclude
		public IWearablesSlotHandler<Entity> handler =
			DefaultSlotHandler.INSTANCE;
	}
	
	
	private boolean _handlerCached = false;
	
	@SuppressWarnings("unchecked")
	private void cacheHandler(Entity entity)
	{
		for (Pair<IWearablesSlotHandler<Entity>, Class<Entity>> pair : IWearablesSlotHandler.REGISTRY)
			if (pair.getRight().isAssignableFrom(entity.getClass()))
				for (WearablesSlotType slotType : pair.getLeft().getSlotTypes((EntityType<Entity>)entity.getType()))
					this.slotTypes.computeIfAbsent(slotType, s -> new SlotTypeData()).handler = pair.getLeft();
		this._handlerCached = true;
	}
}
