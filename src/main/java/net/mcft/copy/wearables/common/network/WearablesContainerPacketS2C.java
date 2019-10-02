package net.mcft.copy.wearables.common.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.mcft.copy.wearables.WearablesCommon;
import net.mcft.copy.wearables.api.IWearablesEntity;
import net.mcft.copy.wearables.api.IWearablesSlot;
import net.mcft.copy.wearables.api.WearablesContainerSlot;
import net.mcft.copy.wearables.api.WearablesRegion;
import net.mcft.copy.wearables.api.WearablesSlotType;
import net.mcft.copy.wearables.api.IWearablesContainer.RegionEntry;
import net.mcft.copy.wearables.common.WearablesContainerData;
import net.mcft.copy.wearables.common.misc.Position;

import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.World;

public class WearablesContainerPacketS2C
	implements IPacket
{
	public static final Identifier ID = new Identifier(WearablesCommon.MOD_ID, "container");
	@Override public Identifier getID() { return ID; }
	
	
	public int containerSyncId;
	public Collection<NetRegionEntry> regions;
	
	
	public WearablesContainerPacketS2C() {  }
	
	public WearablesContainerPacketS2C(WearablesContainerData data)
	{
		this.containerSyncId = data.getContainer().syncId;
		this.regions = data.getRegions().stream()
			.map(NetRegionEntry::new)
			.collect(Collectors.toList());
	}
	
	
	@Override
	public void read(PacketByteBuf buffer)
		throws IOException
	{
		this.containerSyncId = buffer.readInt();
		this.regions = IntStream.range(0, buffer.readByte())
			.mapToObj(i -> new NetRegionEntry(buffer))
			.collect(Collectors.toList());
	}
	
	@Override
	public void write(PacketByteBuf buffer)
		throws IOException
	{
		buffer.writeInt(this.containerSyncId);
		buffer.writeByte(this.regions.size());
		for (NetRegionEntry entry : this.regions)
			entry.write(buffer);
	}
	
	
	public static class NetRegionEntry
		implements INetSerializable
	{
		public int entityId;
		public Position position;
		public WearablesRegion region;
		public Collection<NetSlotDefinition> slots;
		
		
		public NetRegionEntry(RegionEntry entry)
		{
			this.entityId = entry.entity.getEntityId();
			this.position = entry.position;
			this.region   = entry.region;
			this.slots    = entry.slots.stream()
				.map(NetSlotDefinition::new)
				.collect(Collectors.toList());
		}
		
		public NetRegionEntry(PacketByteBuf buffer)
		{
			try { this.read(buffer); }
			catch (IOException e) { throw new RuntimeException(e); }
		}
		
		
		public RegionEntry toRegionEntry(World world)
		{
			Entity entity = world.getEntityById(this.entityId);
			if (entity == null) {
				WearablesCommon.LOGGER.warn("[wearables:onContainerPacket] Unknown entity key '{}'", this.entityId);
				return null;
			}
			List<WearablesContainerSlot> slots = new ArrayList<>();
			for (NetSlotDefinition slot : this.slots) {
				IWearablesSlot wearablesSlot = IWearablesEntity.from(entity)
					.getWearablesSlot(slot.slotType, slot.index, true);
				int x = this.position.x + 4 + slots.size() * 18;
				int y = this.position.y + 4;
				slots.add(new WearablesContainerSlot(wearablesSlot, x, y));
			}
			return new RegionEntry(entity, this.position, this.region, slots);
		}
		
		
		@Override
		public void read(PacketByteBuf buffer)
			throws IOException
		{
			this.entityId = buffer.readInt();
			this.position = new Position(buffer.readInt(), buffer.readInt());
			this.region   = new WearablesRegion(buffer.readString());
			this.slots    = IntStream.range(0, buffer.readByte())
				.mapToObj(i -> new NetSlotDefinition(buffer))
				.collect(Collectors.toList());
		}
		
		@Override
		public void write(PacketByteBuf buffer)
			throws IOException
		{
			buffer.writeInt(this.entityId);
			buffer.writeInt(this.position.x);
			buffer.writeInt(this.position.y);
			buffer.writeString(this.region.name);
			
			buffer.writeByte(this.slots.size());
			for (NetSlotDefinition slot : this.slots)
				slot.write(buffer);
		}
	}
	
	public static class NetSlotDefinition
		implements INetSerializable
	{
		public WearablesSlotType slotType;
		public int index;
		public int order;
		
		
		public NetSlotDefinition(WearablesContainerSlot slot)
		{
			this.slotType = slot.wearablesSlot.getSlotType();
			this.index    = slot.wearablesSlot.getIndex();
			this.order    = slot.wearablesSlot.getOrder();
		}
		
		public NetSlotDefinition(PacketByteBuf buffer)
		{
			try { this.read(buffer); }
			catch (IOException e) { throw new RuntimeException(e); }
		}
		
		
		@Override
		public void read(PacketByteBuf buffer)
			throws IOException
		{
			this.slotType = new WearablesSlotType(buffer.readString());
			this.index    = buffer.readInt();
			this.order    = buffer.readInt();
		}
		
		@Override
		public void write(PacketByteBuf buffer)
			throws IOException
		{
			buffer.writeString(this.slotType.fullName);
			buffer.writeInt(this.index);
			buffer.writeInt(this.order);
		}
	}
}
