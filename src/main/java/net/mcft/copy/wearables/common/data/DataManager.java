package net.mcft.copy.wearables.common.data;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;

import net.mcft.copy.wearables.WearablesCommon;
import net.mcft.copy.wearables.api.IWearablesData;
import net.mcft.copy.wearables.api.IWearablesSlotType;
import net.mcft.copy.wearables.common.impl.WearablesDataImpl;
import net.mcft.copy.wearables.common.impl.WearablesRegionImpl;
import net.mcft.copy.wearables.common.impl.WearablesSlotTypeImpl;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.profiler.Profiler;

public class DataManager
	implements SimpleResourceReloadListener<DataManager.Data>
{
	@Override
	public Identifier getFabricId()
		{ return new Identifier(WearablesCommon.MOD_ID, "data"); }
	
	public void registerReloadListener()
		{ ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(this); }
	
	@Override
	public CompletableFuture<Data> load(ResourceManager manager, Profiler profiler, Executor executor)
		{ return CompletableFuture.supplyAsync(() -> new Data(manager), executor); }
	
	@Override
	public CompletableFuture<Void> apply(Data data, ResourceManager manager, Profiler profiler, Executor executor)
		{ return CompletableFuture.runAsync(data::apply, executor); }
	
	
	public static class Data
	{
		private static final Gson GSON = new GsonBuilder().create();
		
		public Map<String, SlotTypeData> slotTypes = new HashMap<>();
		public Map<Identifier, ItemData> items = new HashMap<>();
		
		public Data(ResourceManager manager)
		{
			for (Identifier id : manager.findResources("config/wearables", path -> path.endsWith(".json"))) {
				try {
					InputStreamReader reader = new InputStreamReader(manager.getResource(id).getInputStream());
					if (id.getPath().startsWith("config/wearables/slots/")) loadSlotTypesResource(id, reader);
					else if (id.getPath().startsWith("config/wearables/items/")) loadItemsResource(id, reader);
					else WearablesCommon.LOGGER.warn("[Wearables:DataManager] Unknown resource '{}'", id);
				} catch (JsonIOException | JsonSyntaxException ex) {
					WearablesCommon.LOGGER.error("[Wearables:DataManager] Error while parsing resource '{}'", id, ex);
				} catch (IOException ex) {
					WearablesCommon.LOGGER.error("[Wearables:DataManager] Error reading resource '{}'", id, ex);
				}
			}
		}
		
		private <TKey, TValue extends Loadable<TKey>> void loadResourceMany(
			Identifier id, InputStreamReader reader, Map<TKey, TValue> dstMap, Type type)
		{
			Map<String, TValue> map = GSON.fromJson(reader, type);
			for (Map.Entry<String, TValue> entry : map.entrySet())
				try { dstMap.put(entry.getValue().load(entry.getKey()), entry.getValue()); }
				catch (Exception ex) { WearablesCommon.LOGGER.error("[Wearables:DataManager] Error loading resource '{}': {}", id, ex.getMessage()); }
		}
		
		private void loadSlotTypesResource(Identifier id, InputStreamReader reader)
			{ loadResourceMany(id, reader, slotTypes, new TypeToken<Map<String, SlotTypeData>>(){}.getType()); }
		
		private void loadItemsResource(Identifier id, InputStreamReader reader)
			{ loadResourceMany(id, reader, items, new TypeToken<Map<String, ItemData>>(){}.getType()); }
		
		
		public void apply()
		{
			WearablesDataImpl wearablesData = (WearablesDataImpl)IWearablesData.INSTANCE;
		
			// Clear slotTypes on (currently hardcoded) regions.
			for (WearablesRegionImpl region : wearablesData.regions.values()) region.slotTypes.clear();
			// Clear all other data.
			wearablesData.slotTypes.clear();
			wearablesData.itemToValidSlots.clear();
			
			// Add built-in Vanilla slot types.
			wearablesData.slotTypes.put( "head:armor/helmet"    , new WearablesSlotTypeImpl( "head:armor/helmet"    , EquipmentSlot.HEAD ));
			wearablesData.slotTypes.put("chest:armor/chestplate", new WearablesSlotTypeImpl("chest:armor/chestplate", EquipmentSlot.CHEST));
			wearablesData.slotTypes.put( "legs:armor/leggings"  , new WearablesSlotTypeImpl( "legs:armor/leggings"  , EquipmentSlot.LEGS ));
			wearablesData.slotTypes.put( "feet:armor/boots"     , new WearablesSlotTypeImpl( "feet:armor/boots"     , EquipmentSlot.FEET ));
			
			// Add or modify WearablesSlotTypeImpl from SlotTypeData.
			for (Map.Entry<String, SlotTypeData> slotTypeEntry : slotTypes.entrySet()) {
				String       slotTypeName = slotTypeEntry.getKey();
				SlotTypeData slotTypeData = slotTypeEntry.getValue();
				WearablesSlotTypeImpl slotType = wearablesData.slotTypes.get(slotTypeName);
				if (slotType == null) wearablesData.slotTypes.put(slotTypeName, slotType = new WearablesSlotTypeImpl(slotTypeName));
				
				if (slotTypeData.order != null)
					slotType.order = slotTypeData.order;
				if (slotTypeData.slotCount != null)
					slotType.slotCount = slotTypeData.slotCount;
				if ((slotTypeData.minSlotCount != null) && (slotTypeData.minSlotCount > slotType.slotCount))
					slotType.slotCount = slotTypeData.minSlotCount;
			}
			
			// Add slot types to regions.
			for (WearablesSlotTypeImpl slotType : wearablesData.slotTypes.values())
				((WearablesRegionImpl)slotType.getRegion()).slotTypes.add(slotType);
			
			// Add valid types for items.
			for (Map.Entry<Identifier, ItemData> itemDataEntry : items.entrySet()) {
				Identifier itemDataId = itemDataEntry.getKey();
				ItemData   itemData   = itemDataEntry.getValue();
				if ((itemData.validSlots == null) || (itemData.validSlots.length == 0)) continue;
				
				Set<IWearablesSlotType> validSlots = wearablesData.itemToValidSlots.get(itemDataId);
				if (validSlots == null) wearablesData.itemToValidSlots.put(itemDataId, validSlots = new HashSet<>());
				
				for (String slotTypeName : itemData.validSlots) {
					// Try to find a slot that matches the requested slotTypeName.
					// If `chest:neck/amulet` is requested, but that doesn't exist, `chest:neck` is checked next.
					while (true) {
						IWearablesSlotType slotType = wearablesData.getSlotType(slotTypeName);
						if (slotType != null) {
							// Found something!
							validSlots.add(slotType);
							break;
						}
						int slashIndex = slotTypeName.lastIndexOf('/');
						if (slashIndex >= 0) slotTypeName = slotTypeName.substring(0, slashIndex);
						else break;
					}
				}
			}
			
			// Make valid slots sets unmodifiable.
			for (Map.Entry<Identifier, Set<IWearablesSlotType>> entry : wearablesData.itemToValidSlots.entrySet())
				entry.setValue(Collections.unmodifiableSet(entry.getValue()));
		}
	}
	
	
	public interface Loadable<T>
	{
		public T load(String key);
	}
	
	public static class SlotTypeData
		implements Loadable<String>
	{
		public Integer order;
		public Integer slotCount;
		public Integer minSlotCount;
		
		@Override
		public String load(String key)
		{
			if (!IWearablesSlotType.SLOT_TYPE_REGEX.matcher(key).matches())
				throw new RuntimeException("Invalid slot type name '" + key + "'");
			// TODO: More verification in SlotTypeData.
			return key;
		}
	}
	
	public static class ItemData
		implements Loadable<Identifier>
	{
		public String[] validSlots;
		
		@Override
		public Identifier load(String key)
		{
			Identifier id;
			try { id = new Identifier(key); }
			catch (InvalidIdentifierException ex) { throw new RuntimeException("Invalid identifier '" + key + "'", ex); }
			// TODO: More verification in ItemData.
			return id;
		}
	}
}
