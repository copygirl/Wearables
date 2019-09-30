package net.mcft.copy.wearables.common.network;

import java.util.Map;
import java.util.stream.Collectors;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.PacketContext;

import net.mcft.copy.wearables.api.IWearablesEntity;
import net.mcft.copy.wearables.api.IWearablesSlot;
import net.mcft.copy.wearables.common.WearablesEntry;
import net.mcft.copy.wearables.common.network.NetUtil;
import net.mcft.copy.wearables.common.network.NetworkHandler;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public class NetworkHandler
{
	public void initializeCommon()
	{
	}
	
	@Environment(EnvType.CLIENT)
	public void initializeClient()
	{
		NetUtil.registerServerToClient(WearablesContainerPacketS2C.class, this::onContainerPacket);
		NetUtil.registerServerToClient(WearablesUpdatePacketS2C.class, this::onUpdatePacket);
	}
	
	
	@Environment(EnvType.CLIENT)
	public void onContainerPacket(PacketContext context, WearablesContainerPacketS2C packet)
	{
		
	}
	
	@Environment(EnvType.CLIENT)
	public void onUpdatePacket(PacketContext context, WearablesUpdatePacketS2C packet)
	{
		Entity entity = context.getPlayer().world.getEntityById(packet.entityId);
		if (entity == null) throw new RuntimeException(
			"Got WearablesUpdatePacket for non-existent entity");
		if (!IWearablesEntity.is(entity)) throw new RuntimeException(
			"Got WearablesUpdatePacket for non Wearables entity '" + entity.getClass() + "'");
		IWearablesEntity wearablesEntity = IWearablesEntity.from(entity);
		
		if (packet.replaceAll) {
			
			// First of all, collect all the Wearables to be set.
			Map<IWearablesSlot, ItemStack> map = packet.data.stream()
				.collect(Collectors.toMap(entry -> getSlot(wearablesEntity, entry),
				                          entry -> entry.stack));
			
			// Remove all equipped Wearables whose slots aren't in the packet.
			wearablesEntity.getEquippedWearables()
				.filter(slot -> !map.containsKey(slot))
				.collect(Collectors.toList())
				.forEach(slot -> slot.set(ItemStack.EMPTY));
			
			// Set all the changed / newly equipped Wearables.
			for (Map.Entry<IWearablesSlot, ItemStack> entry : map.entrySet())
				entry.getKey().set(entry.getValue());
			
		} else for (WearablesEntry entry : packet.data)
			getSlot(wearablesEntity, entry).set(entry.stack);
	}
	
	private static IWearablesSlot getSlot(IWearablesEntity entity, WearablesEntry entry)
	{
		return entity.getWearablesSlot(entry.slotType, entry.index, true);
	}
}
