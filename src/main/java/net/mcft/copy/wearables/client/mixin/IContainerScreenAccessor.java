package net.mcft.copy.wearables.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.gui.ContainerProvider;
import net.minecraft.client.gui.ContainerScreen;
import net.minecraft.client.gui.MultiInputListener;
import net.minecraft.container.Container;
import net.minecraft.container.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin(ContainerScreen.class)
public interface IContainerScreenAccessor<T extends Container>
	extends ContainerProvider<T>, MultiInputListener
{
	@Accessor
	public int getLeft();
	@Accessor
	public int getTop();
	@Accessor
	public Slot getFocusedSlot();
	
	@Invoker
	public Slot invokeGetSlotAt(double pointX, double pointY);
	@Invoker
	public boolean invokeIsPointOverSlot(Slot slot, double pointX, double pointY);
	@Invoker
	public boolean invokeIsPointWithinBounds(int x, int y, int width, int height, double pointX, double pointY);
}
