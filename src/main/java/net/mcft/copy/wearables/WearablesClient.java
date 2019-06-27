package net.mcft.copy.wearables;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.mcft.copy.wearables.api.WearablesScreenRegistry;
import net.mcft.copy.wearables.client.SlotIconSpriteHandler;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

@Environment(EnvType.CLIENT)
public class WearablesClient
	extends WearablesCommon
	implements ClientModInitializer
{
	public final SlotIconSpriteHandler SPRITES = new SlotIconSpriteHandler();
	
	@Override
	public void onInitializeClient()
	{
		SPRITES.registerSpriteCallback();
		NETWORK.initializeClient();
		
		WearablesScreenRegistry.register("SurvivalInventoryScreen", InventoryScreen.class);
		WearablesScreenRegistry.register("CreativeInventoryScreen", CreativeInventoryScreen.class);
	}
}
