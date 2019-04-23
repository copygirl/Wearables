package net.mcft.copy.wearables;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

import net.mcft.copy.wearables.api.IWearablesEntity;
import net.mcft.copy.wearables.api.IWearablesSlot;
import net.mcft.copy.wearables.api.WearablesAPI;
import net.mcft.copy.wearables.api.WearablesSlotType;
import net.mcft.copy.wearables.common.WearablesEntry;
import net.mcft.copy.wearables.common.network.NetUtil;
import net.mcft.copy.wearables.common.network.WearablesInteractPacket;
import net.mcft.copy.wearables.common.network.WearablesUpdatePacket;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class WearablesMod
	implements ModInitializer
	         , ClientModInitializer
{
	public final static String MOD_ID = "wearables";
	
	
	@Override
	public void onInitialize()
	{
		NetUtil.registerClientToServer(WearablesInteractPacket.class, (context, packet) -> {
			PlayerEntity player = context.getPlayer();
			WearablesSlotType slotType = WearablesAPI.findSlotType(packet.slotType);
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
		});
	}
	
	@Override
	public void onInitializeClient()
	{
		NetUtil.registerServerToClient(WearablesUpdatePacket.class, (context, packet) -> {
			Entity entity = context.getPlayer().world.getEntityById(packet.entityId);
			if (entity == null) throw new RuntimeException(
				"Got WearablesUpdatePacket for non-existent entity");
			if (!(entity instanceof IWearablesEntity)) throw new RuntimeException(
				"Got WearablesUpdatePacket for non-IWearablesEntity '" + entity.getClass() + "'");
			IWearablesEntity wearablesEntity = (IWearablesEntity)entity;
			
			for (WearablesEntry entry : packet.data) {
				WearablesSlotType slotType = WearablesAPI.findSlotType(entry.slotTypeName);
				if (slotType == null) throw new RuntimeException("slotType '" + entry.slotTypeName + "' not found");
				wearablesEntity.getWearablesSlot(slotType, entry.index).set(entry.stack);
			}
		});
	}
}
