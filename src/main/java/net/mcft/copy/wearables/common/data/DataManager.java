package net.mcft.copy.wearables.common.data;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;

import net.mcft.copy.wearables.WearablesMod;
import net.mcft.copy.wearables.api.IWearablesData;
import net.mcft.copy.wearables.common.impl.WearablesDataImpl;
import net.mcft.copy.wearables.common.impl.WearablesRegionImpl;
import net.mcft.copy.wearables.common.impl.WearablesSlotTypeImpl;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

public class DataManager
	implements SimpleResourceReloadListener<DataManager.Data>
{
	private static final Gson GSON = new GsonBuilder().create();
	
	@Override
	public Identifier getFabricId()
		{ return new Identifier(WearablesMod.MOD_ID, "data"); }
	
	@Override
	public CompletableFuture<Data> load(ResourceManager manager, Profiler profiler, Executor executor)
	{ return CompletableFuture.supplyAsync(() -> {
		Data data = new Data();
		for (Identifier id : manager.findResources("config/wearables", path -> path.endsWith(".json"))) {
			try {
				InputStreamReader reader = new InputStreamReader(manager.getResource(id).getInputStream());
				if (id.getPath().startsWith("config/wearables/slots/")) {
					// TODO: Verify SlotTypeData.
					data.slotTypes.addAll(Arrays.asList(GSON.fromJson(reader, SlotTypeData[].class)));
				} else if (id.getPath().startsWith("config/wearables/items/")) {
					// TODO: Implement item data loading.
				} else WearablesMod.LOGGER.warn("[Wearables:DataManager] Unknown resource '{}'", id);
			} catch (JsonIOException | JsonSyntaxException ex) {
				WearablesMod.LOGGER.error("[Wearables:DataManager] Error while parsing '{}'", id, ex);
			} catch (IOException ex) {
				WearablesMod.LOGGER.error("[Wearables:DataManager] Error reading resource '{}'", id, ex);
			}
		}
		return data;
	}, executor); }
	
	@Override
	public CompletableFuture<Void> apply(Data data, ResourceManager manager, Profiler profiler, Executor executor)
	{ return CompletableFuture.runAsync(() -> {
		WearablesDataImpl wearablesData = (WearablesDataImpl)IWearablesData.INSTANCE;
		
		// Clear all slot types.
		for (WearablesRegionImpl region : wearablesData.regions.values()) region.slotTypes.clear();
		wearablesData.slotTypes.clear();
		
		// Add built-in Vanilla slot types.
		wearablesData.slotTypes.put( "head:armor/helmet"    , new WearablesSlotTypeImpl( "head:armor/helmet"    , EquipmentSlot.HEAD ));
		wearablesData.slotTypes.put("chest:armor/chestplate", new WearablesSlotTypeImpl("chest:armor/chestplate", EquipmentSlot.CHEST));
		wearablesData.slotTypes.put( "legs:armor/leggings"  , new WearablesSlotTypeImpl( "legs:armor/leggings"  , EquipmentSlot.LEGS ));
		wearablesData.slotTypes.put( "feet:armor/boots"     , new WearablesSlotTypeImpl( "feet:armor/boots"     , EquipmentSlot.FEET ));
		
		// Add or modify WearablesSlotTypeImpl from SlotTypeData.
		for (SlotTypeData slotTypeData : data.slotTypes) {
			WearablesSlotTypeImpl slotType = wearablesData.slotTypes.get(slotTypeData.name);
			if (slotType == null) wearablesData.slotTypes.put(slotTypeData.name, slotType = new WearablesSlotTypeImpl(slotTypeData.name));
			
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
	}, executor); }
	
	
	public static class Data
	{
		public List<SlotTypeData> slotTypes = new ArrayList<>();
	}
	
	public static class SlotTypeData
	{
		public String name;
		public Integer order;
		public Integer slotCount;
		public Integer minSlotCount;
	}
}
