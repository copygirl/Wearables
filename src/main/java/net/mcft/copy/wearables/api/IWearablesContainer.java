package net.mcft.copy.wearables.api;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import net.mcft.copy.wearables.common.impl.WearablesContainerImpl;
import net.mcft.copy.wearables.common.misc.Position;

import net.minecraft.container.Container;
import net.minecraft.entity.Entity;

public interface IWearablesContainer
{
	public Collection<RegionEntry> getRegions();
	
	
	public static IWearablesContainer from(Container container)
		{ return new WearablesContainerImpl(container); }
	
	
	public static class RegionEntry
	{
		public final Entity entity;
		public final Position position;
		public final WearablesRegion region;
		public final ImmutableList<WearablesContainerSlot> slots;
		
		public RegionEntry(Entity entity, Position position, WearablesRegion region,
		                   Collection<WearablesContainerSlot> slots)
		{
			if (entity == null) throw new IllegalArgumentException("entity is null");
			if (position == null) throw new IllegalArgumentException("position is null");
			if (region == null) throw new IllegalArgumentException("region is null");
			if (slots == null) throw new IllegalArgumentException("slots is null");
			this.entity   = entity;
			this.position = position;
			this.region   = region;
			this.slots    = ImmutableList.copyOf(slots);
		}
	}
}
