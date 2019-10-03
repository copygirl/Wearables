package net.mcft.copy.wearables.common.mixin.container;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.mcft.copy.wearables.common.WearablesContainerData;
import net.mcft.copy.wearables.common.network.NetUtil;
import net.mcft.copy.wearables.common.network.WearablesContainerPacketS2C;

import net.minecraft.container.Container;
import net.minecraft.container.ContainerListener;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(Container.class)
public abstract class ContainerMixin
	implements WearablesContainerData.IAccessor
{
	private WearablesContainerData _wearables_data = null;
	
	@Inject(method="addListener", at=@At("TAIL"))
	public void addListener(ContainerListener listener, CallbackInfo info)
	{
		if (this._wearables_data == null) {
			this._wearables_data = new WearablesContainerData((Container)(Object)this);
			this._wearables_data.computeAndAddRegionEntries();
		}
		if ((listener instanceof ServerPlayerEntity)
		 && (this._wearables_data.getRegions() != null))
			NetUtil.sendToPlayer((ServerPlayerEntity)listener,
				new WearablesContainerPacketS2C(this._wearables_data));
	}
	
	// WearablesContainerData.IAccessor implementation
	
	@Override
	public WearablesContainerData getWearablesData(boolean create)
	{
		if (create && (this._wearables_data == null))
			this._wearables_data = new WearablesContainerData((Container)(Object)this);
		return this._wearables_data;
	}
}
