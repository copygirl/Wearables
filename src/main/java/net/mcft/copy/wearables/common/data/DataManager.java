package net.mcft.copy.wearables.common.data;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;

import net.mcft.copy.wearables.WearablesCommon;
import net.mcft.copy.wearables.api.IWearablesItemHandler;
import net.mcft.copy.wearables.api.WearablesRegion;
import net.mcft.copy.wearables.api.WearablesSlotType;
import net.mcft.copy.wearables.common.data.ContainerData.RegionEntry;
import net.mcft.copy.wearables.common.data.EntityTypeData.SlotTypeData;
import net.mcft.copy.wearables.common.data.WearablesData.ItemData;
import net.mcft.copy.wearables.common.misc.Position;

import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;

/** Loads mod configuration data from data packs. */
public class DataManager
	implements SimpleResourceReloadListener<DataManager.RawData>
{
	@Override
	public Identifier getFabricId()
		{ return new Identifier(WearablesCommon.MOD_ID, "data"); }
	
	public void registerReloadListener()
		{ ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(this); }
	
	@Override
	public CompletableFuture<RawData> load(ResourceManager manager, Profiler profiler, Executor executor)
		{ return CompletableFuture.supplyAsync(() -> new RawData(manager), executor); }
	
	@Override
	public CompletableFuture<Void> apply(RawData data, ResourceManager manager, Profiler profiler, Executor executor)
		{ return CompletableFuture.runAsync(data::apply, executor); }
	
	
	public static class RawData
	{
		private static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(Identifier.class       , (SimpleStringDeserializer<?>)Identifier::new)
			.registerTypeAdapter(WearablesRegion.class  , (SimpleStringDeserializer<?>)WearablesRegion::new)
			.registerTypeAdapter(WearablesSlotType.class, (SimpleStringDeserializer<?>)WearablesSlotType::new)
			.registerTypeAdapter(Position.class         , PositionDeserializer.INSTANCE)
			.enableComplexMapKeySerialization()
			.create();
		
		public List<RawContainerData> containers = new ArrayList<>();
		public List<RawEntityTypeData> entities = new ArrayList<>();
		public List<RawItemDataMap> items = new ArrayList<>();
		
		public RawData(ResourceManager manager)
		{
			for (Identifier id : manager.findResources("config/wearables", path -> path.endsWith(".json"))) {
				try {
					InputStreamReader reader = new InputStreamReader(manager.getResource(id).getInputStream());
					if (id.getPath().startsWith("config/wearables/container/"))
						containers.add(GSON.fromJson(reader, RawContainerData.class));
					else if (id.getPath().startsWith("config/wearables/entity/"))
						entities.add(GSON.fromJson(reader, RawEntityTypeData.class));
					else if (id.getPath().startsWith("config/wearables/item/"))
						items.add(GSON.fromJson(reader, RawItemDataMap.class));
					else WearablesCommon.LOGGER.warn("[Wearables:DataManager] Unknown resource '{}'", id);
				} catch (JsonIOException | JsonSyntaxException ex) {
					WearablesCommon.LOGGER.error("[Wearables:DataManager] Error while parsing resource '{}'", id, ex);
				} catch (IOException ex) {
					WearablesCommon.LOGGER.error("[Wearables:DataManager] Error reading resource '{}'", id, ex);
				} catch (Exception ex) {
					WearablesCommon.LOGGER.error("[Wearables:DataManager] Error loading resource '{}'", id, ex);
				}
			}
		}
		
		public void apply()
		{
			WearablesData data = WearablesData.INSTANCE;
			data.containers.clear();
			data.entities.clear();
			data.items.clear();
			data.version++;
			
			for (RawContainerData rawContainerData : this.containers) {
				for (Identifier containerIdentifier : rawContainerData.appliesTo) {
					ContainerData containerData = data.containers
						.computeIfAbsent(containerIdentifier, id -> new ContainerData());
					
					if (rawContainerData.mergeStrategy == MergeStrategy.REPLACE)
						containerData.entries.clear();
					
					containerData.entries.addAll(rawContainerData.entries);
				}
			}
			
			Map<EntityType<?>, Map<WearablesRegion, Set<WearablesSlotType>>> masksData = new HashMap<>();
			
			for (RawEntityTypeData rawEntityData : this.entities) {
				for (Identifier entityTypeId : rawEntityData.appliesTo) {
					EntityType<?> entityType = Registry.ENTITY_TYPE.getOrEmpty(entityTypeId).orElse(null);
					if (entityType == null) {
						WearablesCommon.LOGGER.info("[Wearables:DataManager] Could not find entity type '{}'", entityTypeId);
						continue;
					}
					EntityTypeData entityData = data.entities
						.computeIfAbsent(entityType, e -> new EntityTypeData());
					
					if (rawEntityData.mergeStrategy == MergeStrategy.REPLACE) {
						entityData.regions.clear();
						entityData.slotTypes.clear();
						masksData.remove(entityType);
					}
					
					if ((rawEntityData.regions != null) && !rawEntityData.regions.isEmpty()) {
						entityData.regions.addAll(rawEntityData.regions.keySet());
						
						Map<WearablesRegion, Set<WearablesSlotType>> masksByRegion =
							masksData.computeIfAbsent(entityType, e -> new HashMap<>());
						for (Map.Entry<WearablesRegion, WearablesSlotType[]> entry : rawEntityData.regions.entrySet())
							masksByRegion.computeIfAbsent(entry.getKey(), e -> new HashSet<>())
							             .addAll(Arrays.asList(entry.getValue()));
					}
					
					for (Map.Entry<WearablesSlotType, SlotTypeData> entry : rawEntityData.slots.entrySet()) {
						WearablesSlotType slotType = entry.getKey();
						SlotTypeData slotTypeData  = entry.getValue();
						if (slotTypeData.slotCount > 0)
							entityData.slotTypes.put(slotType, slotTypeData);
						else entityData.slotTypes.remove(slotType);
					}
				}
			}
			
			for (Map.Entry<EntityType<?>, Map<WearablesRegion, Set<WearablesSlotType>>> entityEntry : masksData.entrySet()) {
				EntityTypeData entityData = data.entities.get(entityEntry.getKey());
				for (Map.Entry<WearablesRegion, Set<WearablesSlotType>> entry : entityEntry.getValue().entrySet()) {
					WearablesRegion region = entry.getKey();
					Set<WearablesSlotType> slotTypesByRegion = entityData.slotTypesByRegion.get(region);
					if (slotTypesByRegion == null) slotTypesByRegion = new HashSet<>();
					
					entityData.slotTypes.keySet().stream()
						.filter(slotType -> entry.getValue().stream().anyMatch(slotType::matches))
						.forEach(slotTypesByRegion::add);
					
					if (!entityData.slotTypesByRegion.containsKey(region) && !slotTypesByRegion.isEmpty())
						entityData.slotTypesByRegion.put(region, slotTypesByRegion);
				}
			}
			
			for (RawItemDataMap rawItemDataMap : this.items) {
				for (Map.Entry<String, ItemData> entry : rawItemDataMap.entrySet()) {
					String key = entry.getKey();
					if (key.isEmpty()) {
						WearablesCommon.LOGGER.info("[Wearables:DataManager] Empty item key / identifier");
						continue;
					}
					
					if (key.charAt(0) == '!') {
						key = key.substring(1);
						if (!IWearablesItemHandler.VALID_SPECIAL_ITEMS.contains(key)) {
							WearablesCommon.LOGGER.warn("[Wearables:DataManager] No special item handler for '{}'", key);
							continue;
						}
						data.specialItems.put(key, entry.getValue());
					} else {
						Identifier id = Identifier.tryParse(key);
						if (id == null) {
							WearablesCommon.LOGGER.error("[Wearables:DataManager] Item identifier '{}' is invalid", key);
							continue;
						}
						Item item = Registry.ITEM.get(id);
						if (item == Items.AIR) {
							WearablesCommon.LOGGER.info("[Wearables:DataManager] Could not find item '{}'", id);
							continue;
						}
						data.items.put(item, entry.getValue());
					}
				}
			}
		}
	}
	
	
	public static class RawContainerData
	{
		public Identifier[] appliesTo;
		public MergeStrategy mergeStrategy;
		public RegionEntries entries;
		
		@SuppressWarnings("serial")
		public static class RegionEntries
			extends ArrayList<RegionEntry> {  }
	}
	
	public static class RawEntityTypeData
	{
		public Identifier[] appliesTo;
		public MergeStrategy mergeStrategy;
		public Regions regions;
		public Slots slots;
		
		@SuppressWarnings("serial")
		public static class Regions
			extends HashMap<WearablesRegion, WearablesSlotType[]> {  }
		
		@SuppressWarnings("serial")
		public static class Slots
			extends HashMap<WearablesSlotType, SlotTypeData> {  }
	}
	
	@SuppressWarnings("serial")
	public static class RawItemDataMap
		extends HashMap<String, ItemData> {  }
	
	public static enum MergeStrategy
		{ COMBINE, REPLACE; }
	
	
	@FunctionalInterface
	public interface SimpleStringDeserializer<T>
		extends Function<String, T>
		      , JsonDeserializer<T>
	{
		public default T deserialize(JsonElement json, Type typeOfT,
		                             JsonDeserializationContext context)
			{ return this.apply(json.getAsString()); }
	}
	
	public static class PositionDeserializer
		implements JsonDeserializer<Position>
	{
		public static final PositionDeserializer INSTANCE = new PositionDeserializer();
		private PositionDeserializer() {  }
		
		public Position deserialize(JsonElement json, Type typeOfT,
		                            JsonDeserializationContext context)
		{
			JsonArray array = json.getAsJsonArray();
			if (array.size() != 2) throw new IllegalStateException("Position array isn't of size 2");
			return new Position(array.get(0).getAsInt(), array.get(1).getAsInt());
		}
	}
	
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Exclude {  }
	
	public class ExcludeByAnnotationStrategy
		implements ExclusionStrategy
	{
		public boolean shouldSkipField(FieldAttributes f)
			{ return f.getAnnotation(Exclude.class) != null; }
		
		public boolean shouldSkipClass(Class<?> clazz)
			{ return false; }
	}
}
