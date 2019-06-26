package net.mcft.copy.wearables.common.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.mcft.copy.wearables.api.WearablesRegion;
import net.mcft.copy.wearables.api.WearablesSlotType;
import net.mcft.copy.wearables.common.misc.Position;

import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;

public class WearablesData
{
	public static final WearablesData INSTANCE = new WearablesData();
	
	public final Map<Class<? extends AbstractContainerScreen<?>>, ContainerData> containers = new HashMap<>();
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
	
	public static class ContainerData
	{
		public Map<WearablesRegion, Position> regionPositions = new HashMap<>();
	}
}
