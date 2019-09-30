package net.mcft.copy.wearables.common.impl;

import net.mcft.copy.wearables.api.IWearablesContainer;
import net.mcft.copy.wearables.common.WearablesContainerData;

import net.minecraft.container.Container;

public class WearablesContainerImpl
{
	public static IWearablesContainer from(Container container)
		{ return ((WearablesContainerData.IAccessor)container).getWearablesData(); }
}
