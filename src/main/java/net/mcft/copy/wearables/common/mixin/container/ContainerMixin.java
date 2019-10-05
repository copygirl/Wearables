package net.mcft.copy.wearables.common.mixin.container;

import org.spongepowered.asm.mixin.Mixin;

import net.mcft.copy.wearables.common.WearablesContainerData;

import net.minecraft.container.Container;

@Mixin(Container.class)
public abstract class ContainerMixin
	implements WearablesContainerData.IAccessor
{
	private WearablesContainerData _wearables_data = null;
	
	// WearablesContainerData.IAccessor implementation
	
	@Override
	public WearablesContainerData getWearablesData(boolean create)
	{
		if (create && (this._wearables_data == null))
			this._wearables_data = new WearablesContainerData((Container)(Object)this);
		return this._wearables_data;
	}
}
