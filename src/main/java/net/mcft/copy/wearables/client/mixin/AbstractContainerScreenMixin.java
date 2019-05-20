package net.mcft.copy.wearables.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.gui.screen.ingame.ContainerProvider;
import net.minecraft.container.Container;
import net.minecraft.container.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends Container>
	extends Screen implements ContainerProvider<T>
{
	private AbstractContainerScreenMixin()
		{ super(null); }
	
	
	@Inject(method="isPointOverSlot", at=@At("HEAD"), cancellable=true)
	private void isPointOverSlot(Slot slot, double pointX, double pointY,
	                             CallbackInfoReturnable<Boolean> info)
	{
		for (Element child : this.children)
			if (child.isMouseOver(pointX, pointY))
				info.setReturnValue(false);
	}
	
	@Inject(method="mouseReleased", at=@At("HEAD"), cancellable=true)
	public void mouseReleased(double mouseX, double mouseY, int button,
	                          CallbackInfoReturnable<Boolean> info)
	{
		// FIXME: Fix messing with dragging due to cancellation.
		if (super.mouseReleased(mouseX, mouseY, button))
			info.setReturnValue(true);
	}
}
