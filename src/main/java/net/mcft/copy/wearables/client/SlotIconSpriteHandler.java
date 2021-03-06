package net.mcft.copy.wearables.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;

import net.mcft.copy.wearables.WearablesCommon;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SlotIconSpriteHandler
	implements ClientSpriteRegistryCallback
{
	public void registerSpriteCallback()
	{
		ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEX).register(this);
	}
	
	@Override
	public void registerSprites(SpriteAtlasTexture atlasTexture, Registry registry)
	{
		final int begin = "textures/".length();
		final int end   = ".png".length();
		ResourceManager resources = MinecraftClient.getInstance().getResourceManager();
		for (Identifier icon : resources.findResources("textures/gui/icons", path -> path.endsWith(".png"))) {
			if (!WearablesCommon.MOD_ID.equals(icon.getNamespace())) continue;
			registry.register(new Identifier(icon.getNamespace(),
				icon.getPath().substring(begin, icon.getPath().length() - end)));
		}
	}
}
