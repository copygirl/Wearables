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
import net.mcft.copy.wearables.common.network.WearablesInteractPacket;
import net.mcft.copy.wearables.common.network.WearablesUpdatePacket;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class NetworkHandler
{
	public void initializeCommon()
	{
		NetUtil.registerClientToServer(WearablesInteractPacket.class, this::onInteractPacket);
	}
	
	@Environment(EnvType.CLIENT)
	public void initializeClient()
	{
		NetUtil.registerServerToClient(WearablesUpdatePacket.class, this::onUpdatePacket);
	}
	
	
	public void onInteractPacket(PacketContext context, WearablesInteractPacket packet)
	{
		PlayerEntity player = context.getPlayer();
		IWearablesSlot slot = IWearablesEntity.from(player)
			.getWearablesSlot(packet.slotType, packet.index);
		
		// FIXME: Cursor stack always empty while in creative mode?
		// FIXME: Fix the runtime exceptions - resync player inventory and Wearables.
		if (!slot.canUnequip()) throw new RuntimeException("Can't unequip '" + slot.get() + "' from '" + slot + "'");
		ItemStack cursorStack     = player.inventory.getCursorStack();
		ItemStack currentEquipped = slot.get();
		if (cursorStack.isEmpty() && currentEquipped.isEmpty()) throw new RuntimeException("Both stacks are empty");
		if (!slot.canEquip(cursorStack)) throw new RuntimeException("Can't equip '" + cursorStack + "' into '" + slot + "'");
		
		// FIXME: Handle ItemStacks with amount > 1 properly.
		player.inventory.setCursorStack(currentEquipped.copy());
		slot.set(cursorStack.copy());
	}
	
	@Environment(EnvType.CLIENT)
	public void onUpdatePacket(PacketContext context, WearablesUpdatePacket packet)
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
