package net.mcft.copy.wearables.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.mcft.copy.wearables.api.IWearablesContainerId;
import net.mcft.copy.wearables.client.WearablesRegionPopup;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.gui.screen.ingame.ContainerProvider;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.container.Container;
import net.minecraft.container.Slot;
import net.minecraft.entity.player.PlayerInventory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends Container>
	extends Screen
	implements ContainerProvider<T>
	         , WearablesRegionPopup.IAccessor
{
	private AbstractContainerScreenMixin()
		{ super(null); }
	
	@Shadow
	private PlayerInventory playerInventory;
	
	private WearablesRegionPopup _wearables_regionPopup;
	
	
	@Inject(method="init", at=@At("TAIL"))
	protected void init(CallbackInfo info)
	{
		if (getContainer() instanceof IWearablesContainerId)
			this._wearables_regionPopup = new WearablesRegionPopup(
				(AbstractContainerScreen<?>)(Object)this);
	}
	
	
	@Inject(method="isPointOverSlot", at=@At("HEAD"), cancellable=true)
	private void isPointOverSlot(Slot slot, double pointX, double pointY,
	                             CallbackInfoReturnable<Boolean> info)
	{
		if (this._wearables_regionPopup == null) return;
		if (this._wearables_regionPopup.isMouseOverBorder(pointX, pointY))
			info.setReturnValue(false);
	}
	
	@Inject(method="isClickOutsideBounds", at=@At("TAIL"), cancellable=true)
	private void isClickOutsideBounds(double pointX, double pointY, int left, int top,
	                                  int button, CallbackInfoReturnable<Boolean> info)
	{
		if (this._wearables_regionPopup == null) return;
		if (info.getReturnValueZ() && this._wearables_regionPopup.isMouseOver(pointX, pointY))
			info.setReturnValue(false);
	}
	
	
	@Inject(method="mouseClicked", at=@At("HEAD"), cancellable=true)
	private void mouseClicked(double mouseX, double mouseY, int button,
	                          CallbackInfoReturnable<Boolean> info)
	{
		if (this._wearables_regionPopup == null) return;
		if (this._wearables_regionPopup.mouseClicked(mouseX, mouseY, button))
			info.setReturnValue(true);
	}
	
	@Inject(method="drawSlot", at=@At("HEAD"), cancellable=true)
	private void drawSlot(Slot slot, CallbackInfo info)
	{
		// Skip rendering slots that are handled by WearablesRegionPopup.
		if ((this._wearables_regionPopup != null)
		 && (this._wearables_regionPopup.current != null)
		 && (slot == this._wearables_regionPopup.current.centerSlot))
		 	info.cancel();
	}
	
	@Inject(method="render", at=@At("HEAD"))
	public void renderAtHead(int mouseX, int mouseY, float tickDelta, CallbackInfo info)
	{
		if (this._wearables_regionPopup == null) return;
		this._wearables_regionPopup.update(mouseX, mouseY);
	}
	
	@Inject(method="render",
	        at=@At(value="INVOKE",
	               target="net/minecraft/client/gui/screen/ingame/AbstractContainerScreen.drawForeground(II)V",
	               shift=Shift.BEFORE))
	public void renderBeforeForeground(int mouseX, int mouseY, float tickDelta, CallbackInfo info)
	{
		if (this._wearables_regionPopup == null) return;
		this._wearables_regionPopup.render(mouseX, mouseY, tickDelta);
		GuiLighting.disable();
	}
	
	
	// WearablesRegionPopup.IAccessor implementation
	
	@Override
	public WearablesRegionPopup getWearablesRegionPopup()
		{ return this._wearables_regionPopup; }
}
