package net.mcft.copy.wearables.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.mcft.copy.wearables.api.IWearablesEntity;
import net.mcft.copy.wearables.api.IWearablesRegion;
import net.mcft.copy.wearables.client.WearablesRegionPopup;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.container.PlayerContainer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin
	extends AbstractInventoryScreen<PlayerContainer>
	implements RecipeBookProvider
{
	private InventoryScreenMixin()
		{ super(null, null, null); }
	
	
	@Inject(method="init", at=@At("TAIL"))
	protected void init(CallbackInfo info)
	{
		IWearablesEntity entity = (IWearablesEntity)playerInventory.player;
		for (IWearablesRegion region : entity.getWearablesRegions())
			this.children.add(new WearablesRegionPopup(this, region));
	}
	
	@Inject(method="render", remap=false, at=@At("HEAD"))
	public void render(int mouseX, int mouseY, float tickDelta, CallbackInfo info)
	{
		for (Element child : this.children)
			if (child instanceof WearablesRegionPopup)
				((WearablesRegionPopup)child).update(mouseX, mouseY);
	}
	
	@Inject(method="drawBackground", at=@At("TAIL"))
	public void drawBackground(float tickDelta, int mouseX, int mouseY, CallbackInfo info)
	{ 
		for (Element child : this.children)
			if (child instanceof WearablesRegionPopup)
				((WearablesRegionPopup)child).render(mouseX, mouseY, tickDelta);
	}
}