package net.mcft.copy.wearables;

import net.fabricmc.api.ClientModInitializer;

public class WearablesClient
	extends WearablesCommon
	implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		NETWORK.initializeClient();
	}
}
