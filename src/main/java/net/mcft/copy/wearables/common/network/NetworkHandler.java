package net.mcft.copy.wearables.common.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.PacketContext;

import net.mcft.copy.wearables.api.IWearablesData;
import net.mcft.copy.wearables.api.IWearablesEntity;
import net.mcft.copy.wearables.api.IWearablesSlot;
import net.mcft.copy.wearables.api.IWearablesSlotType;
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
		IWearablesSlotType slotType = IWearablesData.INSTANCE.getSlotType(packet.slotType);
		if (slotType == null) throw new RuntimeException("slotType '" + packet.slotType + "' not found");
		IWearablesSlot slot = ((IWearablesEntity)player).getWearablesSlot(slotType, packet.index);
		
		// FIXME: Fix the runtime exceptions - resync player inventory and wearables.
		if (!slot.canUnequip()) throw new RuntimeException();
		ItemStack cursorStack     = player.inventory.getCursorStack();
		ItemStack currentEquipped = slot.get();
		if (cursorStack.isEmpty() && currentEquipped.isEmpty()) throw new RuntimeException();
		if (!slot.canEquip(cursorStack)) throw new RuntimeException();
		
		// FIXME: Handle ItemStacks with amount > 1 properly.
		player.inventory.setCursorStack(currentEquipped);
		slot.set(cursorStack);
	}
	
	@Environment(EnvType.CLIENT)
	public void onUpdatePacket(PacketContext context, WearablesUpdatePacket packet)
	{
		Entity entity = context.getPlayer().world.getEntityById(packet.entityId);
		if (entity == null) throw new RuntimeException(
			"Got WearablesUpdatePacket for non-existent entity");
		if (!(entity instanceof IWearablesEntity)) throw new RuntimeException(
			"Got WearablesUpdatePacket for non-IWearablesEntity '" + entity.getClass() + "'");
		IWearablesEntity wearablesEntity = (IWearablesEntity)entity;
		
		for (WearablesEntry entry : packet.data) {
			IWearablesSlotType slotType = IWearablesData.INSTANCE.getSlotType(entry.slotTypeName);
			if (slotType == null) throw new RuntimeException(
				"slotType '" + entry.slotTypeName + "' not found");
			wearablesEntity.getWearablesSlot(slotType, entry.index).set(entry.stack);
		}
	}
}
