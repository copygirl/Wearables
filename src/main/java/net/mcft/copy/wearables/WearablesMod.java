package net.mcft.copy.wearables;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;

import net.mcft.copy.wearables.api.IWearablesData;
import net.mcft.copy.wearables.api.IWearablesEntity;
import net.mcft.copy.wearables.api.IWearablesSlot;
import net.mcft.copy.wearables.api.IWearablesSlotType;
import net.mcft.copy.wearables.common.WearablesEntry;
import net.mcft.copy.wearables.common.data.DataManager;
import net.mcft.copy.wearables.common.network.NetUtil;
import net.mcft.copy.wearables.common.network.WearablesInteractPacket;
import net.mcft.copy.wearables.common.network.WearablesUpdatePacket;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceType;

public class WearablesMod
	implements ModInitializer
	         , ClientModInitializer
{
	public static final String MOD_ID = "wearables";
	
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	
	
	@Override
	public void onInitialize()
	{
		ResourceManagerHelper.get(ResourceType.DATA).registerReloadListener(new DataManager());
		
		NetUtil.registerClientToServer(WearablesInteractPacket.class, (context, packet) -> {
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
				IWearablesSlotType slotType = IWearablesData.INSTANCE.getSlotType(entry.slotTypeName);
				if (slotType == null) throw new RuntimeException("slotType '" + entry.slotTypeName + "' not found");
				wearablesEntity.getWearablesSlot(slotType, entry.index).set(entry.stack);
			}
		});
	}
}
