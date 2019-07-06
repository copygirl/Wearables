package net.mcft.copy.wearables.common.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.mcft.copy.wearables.api.WearablesSlotType;

import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public class WearablesData
{
	public static final WearablesData INSTANCE = new WearablesData();
	
	public final Map<Identifier, ContainerData> containers = new HashMap<>();
	public final Map<EntityType<?>, EntityTypeData> entities = new HashMap<>();
	public final Map<Item, ItemData> items = new HashMap<>();
	public final Map<String, ItemData> specialItems = new HashMap<>();
	
	public int version = 0;
	
	private WearablesData() {  }
	
	public static class ItemData
	{
		public Set<WearablesSlotType> validSlots = new HashSet<>();
		// TODO: Add conflicts.
	}
}
