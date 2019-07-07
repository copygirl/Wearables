package net.mcft.copy.wearables.common.network;

import java.util.Map;
import java.util.stream.Collectors;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.PacketContext;

import net.mcft.copy.wearables.WearablesCommon;
import net.mcft.copy.wearables.api.IWearablesEntity;
import net.mcft.copy.wearables.api.IWearablesSlot;
import net.mcft.copy.wearables.common.WearablesEntry;
import net.mcft.copy.wearables.common.InteractionHandler.Result;
import net.mcft.copy.wearables.common.network.NetUtil;
import net.mcft.copy.wearables.common.network.NetworkHandler;
import net.mcft.copy.wearables.common.network.WearablesInteractPacketC2S;
import net.mcft.copy.wearables.common.network.WearablesUpdatePacketS2C;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class NetworkHandler
{
	public void initializeCommon()
	{
		NetUtil.registerClientToServer(WearablesInteractPacketC2S.class, this::onInteractPacket);
	}
	
	@Environment(EnvType.CLIENT)
	public void initializeClient()
	{
		NetUtil.registerServerToClient(WearablesUpdatePacketS2C.class, this::onUpdatePacket);
	}
	
	
	public void onInteractPacket(PacketContext context, WearablesInteractPacketC2S packet)
	{
		PlayerEntity player = context.getPlayer();
		// FIXME: Currently can only change own wearable slots!
		IWearablesSlot slot = IWearablesEntity.from(player)
			.getWearablesSlot(packet.slotType, packet.index);
		
		if (WearablesCommon.INTERACT.onInteract(player, slot, packet.action, packet.clientCursorStack) != Result.SUCCESS) {
			// FIXME: Resync current container and Wearables slot.
		}
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
