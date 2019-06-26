package net.mcft.copy.wearables.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.mcft.copy.wearables.client.IRegionPopupGetter;
import net.mcft.copy.wearables.client.WearablesRegionPopup;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;

@Environment(EnvType.CLIENT)
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin
{
	// Why the heck does it not call super.isClickOutsideBounds??!
	@Inject(method="isClickOutsideBounds", at=@At("TAIL"), cancellable=true)
	private void isClickOutsideBounds(double pointX, double pointY, int left, int top,
	                                  int button, CallbackInfoReturnable<Boolean> info)
	{
		WearablesRegionPopup popup = ((IRegionPopupGetter)this).getWearablesRegionPopup();
		if (info.getReturnValueZ() && popup.isMouseOver(pointX, pointY))
			info.setReturnValue(false);
	}
}
