package net.mcft.copy.wearables.common.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;

import net.mcft.copy.wearables.WearablesMod;
import net.mcft.copy.wearables.api.IWearablesEntity;
import net.mcft.copy.wearables.common.WearablesEntry;
import net.mcft.copy.wearables.common.WearablesSlot;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public class WearablesUpdatePacket implements IPacket
{
	public static final Identifier ID = new Identifier(WearablesMod.MOD_ID, "update");
	@Override public Identifier getID() { return ID; }
	
	
	public final List<WearablesEntry> data = new ArrayList<>();
	
	public int entityId;
	
	
	public WearablesUpdatePacket() {  }
	
	public WearablesUpdatePacket(Entity entity)
	{
		this.entityId = entity.getEntityId();
		((IWearablesEntity)entity).getEquippedWearables()
			.filter(WearablesSlot.class::isInstance)
			.map(WearablesSlot.class::cast)
			.map(WearablesEntry::new)
			.forEach(data::add);
	}
	
	public WearablesUpdatePacket(WearablesSlot slot)
	{
		this.entityId = slot.getEntity().getEntityId();
		data.add(new WearablesEntry(slot));
	}
	
	
	/** If necessary, creates a WearablesUpdatePacket for all of the the specified entity's
	 *  Wearables and sends it using the provided Consumer. Will not send a packet if the
	 *  specified entity isn't an IWearablesEntity or has no equipped Wearables. */
	public static void sendForEntity(Entity entity, Consumer<Packet<?>> sendPacket)
	{
		if (!(entity instanceof IWearablesEntity)) return;
		IWearablesEntity wearablesEntity = (IWearablesEntity)entity;
		if (!wearablesEntity.hasWearables()) return;
		sendPacket.accept(NetUtil.toVanillaPacket(
			ServerSidePacketRegistry.INSTANCE,
			new WearablesUpdatePacket(entity)));
	}
	
	
	@Override
	public void read(PacketByteBuf buffer)
		throws IOException
	{
		data.clear();
		this.entityId = buffer.readInt();
		int amount = buffer.readByte();
		for (int i = 0; i < amount; i++)
			data.add(WearablesEntry.createFromBuffer(buffer));
	}
	
	@Override
	public void write(PacketByteBuf buffer)
		throws IOException
	{
		buffer.writeInt(this.entityId);
		buffer.writeByte(data.size());
		for (WearablesEntry entry : data)
			entry.write(buffer);
	}
}
