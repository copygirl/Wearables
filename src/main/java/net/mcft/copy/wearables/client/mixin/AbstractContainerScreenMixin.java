package net.mcft.copy.wearables.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.mcft.copy.wearables.client.IRegionPopupGetter;
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

// TODO: Skip drawing slots that are handled by Wearables?
@Environment(EnvType.CLIENT)
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends Container>
	extends Screen
	implements ContainerProvider<T>
	         , IRegionPopupGetter
{
	private AbstractContainerScreenMixin()
		{ super(null); }
	
	@Shadow
	private PlayerInventory playerInventory;
	
	
	private WearablesRegionPopup wearables_regionPopup;
	public WearablesRegionPopup getWearablesRegionPopup()
		{ return this.wearables_regionPopup; }
	
	
	@Inject(method="init", at=@At("TAIL"))
	protected void init(CallbackInfo info)
	{
		this.wearables_regionPopup = new WearablesRegionPopup(
			(AbstractContainerScreen<?>)(Object)this, playerInventory.player);
		this.children.add(this.wearables_regionPopup);
	}
	
	
	@Inject(method="isPointOverSlot", at=@At("HEAD"), cancellable=true)
	private void isPointOverSlot(Slot slot, double pointX, double pointY,
	                             CallbackInfoReturnable<Boolean> info)
	{
		if (this.wearables_regionPopup.isMouseOver(pointX, pointY))
			info.setReturnValue(false);
	}
	
	@Inject(method="isClickOutsideBounds", at=@At("TAIL"), cancellable=true)
	private void isClickOutsideBounds(double pointX, double pointY, int left, int top,
	                                  int button, CallbackInfoReturnable<Boolean> info)
	{
		if (info.getReturnValueZ() && this.wearables_regionPopup.isMouseOver(pointX, pointY))
			info.setReturnValue(false);
	}
	
	
	@Inject(method="mouseClicked", at=@At("HEAD"), cancellable=true)
	private void mouseClicked(double mouseX, double mouseY, int button,
	                          CallbackInfoReturnable<Boolean> info)
	{
		if (this.wearables_regionPopup.mouseClicked(mouseX, mouseY, button))
			info.setReturnValue(true);
	}
	
	
	@Inject(method="render", at=@At("HEAD"))
	public void renderAtHead(int mouseX, int mouseY, float tickDelta, CallbackInfo info)
	{
		this.wearables_regionPopup.update(mouseX, mouseY);
	}
	
	@Inject(method="render", at=@At(value="INVOKE", target="drawForeground", shift=Shift.BEFORE))
	public void renderBeforeForeground(int mouseX, int mouseY, float tickDelta, CallbackInfo info)
	{
		this.wearables_regionPopup.render(mouseX, mouseY, tickDelta);
		GuiLighting.disable();
	}
}
