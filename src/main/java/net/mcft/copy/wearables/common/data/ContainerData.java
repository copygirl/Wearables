package net.mcft.copy.wearables.common.data;

import java.util.ArrayList;
import java.util.List;

import net.mcft.copy.wearables.api.WearablesRegion;
import net.mcft.copy.wearables.common.misc.Position;

public class ContainerData
{
	public List<RegionEntry> entries = new ArrayList<>();
	
	public static class RegionEntry
	{
		public String entityKey;
		public WearablesRegion region;
		public Position position;
	}
}
