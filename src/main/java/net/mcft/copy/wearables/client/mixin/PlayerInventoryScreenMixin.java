package net.mcft.copy.wearables.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.mcft.copy.wearables.api.WearablesAPI;
import net.mcft.copy.wearables.api.WearablesRegion;
import net.mcft.copy.wearables.client.WearablesRegionPopup;

import net.minecraft.client.gui.InputListener;
import net.minecraft.client.gui.ingame.AbstractPlayerInventoryScreen;
import net.minecraft.client.gui.ingame.PlayerInventoryScreen;
import net.minecraft.client.gui.ingame.RecipeBookProvider;
import net.minecraft.container.PlayerContainer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(PlayerInventoryScreen.class)
public abstract class PlayerInventoryScreenMixin
	extends AbstractPlayerInventoryScreen<PlayerContainer>
	implements RecipeBookProvider
{
	private PlayerInventoryScreenMixin()
		{ super(null, null, null); }
	
	
	@Inject(method="init", at=@At("TAIL"))
	protected void init(CallbackInfo info)
	{
		for (WearablesRegion region : WearablesAPI.getRegions())
			this.children.add(new WearablesRegionPopup(this, region));
	}
	
	@Inject(method="render", remap=false, at=@At("HEAD"))
	public void render(int mouseX, int mouseY, float tickDelta, CallbackInfo info)
	{
		for (InputListener child : this.children)
			if (child instanceof WearablesRegionPopup)
				((WearablesRegionPopup)child).update(mouseX, mouseY);
	}
	
	@Inject(method="drawBackground", at=@At("TAIL"))
	public void drawBackground(float tickDelta, int mouseX, int mouseY, CallbackInfo info)
	{ 
		for (InputListener child : this.children)
			if (child instanceof WearablesRegionPopup)
				((WearablesRegionPopup)child).render(mouseX, mouseY, tickDelta);
	}
}
