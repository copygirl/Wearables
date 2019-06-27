package net.mcft.copy.wearables;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;

import net.mcft.copy.wearables.api.IWearablesItemHandler;
import net.mcft.copy.wearables.api.IWearablesSlotHandler;

import net.mcft.copy.wearables.common.data.DataManager;
import net.mcft.copy.wearables.common.network.NetworkHandler;
import net.mcft.copy.wearables.common.impl.item.VanillaArmorItemHandler;
import net.mcft.copy.wearables.common.impl.slot.LivingEntityEquipmentSlotHandler;

import net.minecraft.entity.LivingEntity;

public class WearablesCommon
	implements ModInitializer
{
	public static final String MOD_ID = "wearables";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	
	public static final DataManager    DATA    = new DataManager();
	public static final NetworkHandler NETWORK = new NetworkHandler();
	
	
	@Override
	public void onInitialize()
	{
		DATA.registerReloadListener();
		NETWORK.initializeCommon();
		
		IWearablesItemHandler.register(VanillaArmorItemHandler.INSTANCE);
		IWearablesSlotHandler.register(LivingEntity.class, LivingEntityEquipmentSlotHandler.INSTANCE);
	}
}
