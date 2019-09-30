package net.mcft.copy.wearables.common.mixin.container;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.mcft.copy.wearables.common.WearablesContainerData;

import net.minecraft.container.Container;
import net.minecraft.container.ContainerListener;

@Mixin(Container.class)
public abstract class ContainerMixin
	implements WearablesContainerData.IAccessor
{
	private WearablesContainerData _wearables_data;
	
	@Inject(method="addListener", at=@At("TAIL"))
	public void addListener(ContainerListener listener, CallbackInfo info)
	{
		if (this._wearables_data == null) {
			this._wearables_data = new WearablesContainerData((Container)(Object)this);
			this._wearables_data.addWearablesSlots();
		}
	}
	
	// WearablesContainerData.IAccessor implementation
	
	@Override
	public WearablesContainerData getWearablesData()
		{ return _wearables_data; }
}
