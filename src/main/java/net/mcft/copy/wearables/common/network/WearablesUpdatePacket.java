package net.mcft.copy.wearables.common.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;

import net.mcft.copy.wearables.WearablesCommon;
import net.mcft.copy.wearables.api.IWearablesEntity;
import net.mcft.copy.wearables.common.WearablesEntry;
import net.mcft.copy.wearables.common.impl.WearablesSlotImpl;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public class WearablesUpdatePacket
	implements IPacket
{
	public static final Identifier ID = new Identifier(WearablesCommon.MOD_ID, "update");
	@Override public Identifier getID() { return ID; }
	
	
	public final List<WearablesEntry> data = new ArrayList<>();
	
	public int entityId;
	public boolean replaceAll;
	
	
	public WearablesUpdatePacket() {  }
	
	public WearablesUpdatePacket(Entity entity, boolean replaceAll)
	{
		this.entityId   = entity.getEntityId();
		this.replaceAll = replaceAll;
		((IWearablesEntity)entity).getEquippedWearables()
			.filter(slot -> !slot.getSlotType().isVanilla())
			.map(WearablesEntry::new)
			.forEach(this.data::add);
	}
	
	public WearablesUpdatePacket(WearablesSlotImpl slot)
	{
		this.entityId   = slot.getEntity().getEntityId();
		this.replaceAll = false;
		this.data.add(new WearablesEntry(slot));
	}
	
	
	/**
	 * If necessary, creates a WearablesUpdatePacket for all of the the specified
	 * entity's Wearables and sends it using the provided Consumer.
	 * 
	 * @param entity     The entity for which to send an update packet.
	 * @param sendIfNone If {@code false}, doesn't send a packet if the entity has no Wearables.
	 * @param sendPacket Function to call to send the resulting packet, if any.
	 */
	public static void sendForEntity(Entity entity, boolean sendIfNone,
	                                 Consumer<Packet<?>> sendPacket)
	{
		if ((entity instanceof IWearablesEntity) &&
		    (sendIfNone || ((IWearablesEntity)entity).hasWearables()))
			sendPacket.accept(NetUtil.toVanillaPacket(
				ServerSidePacketRegistry.INSTANCE,
				new WearablesUpdatePacket(entity, true)));
	}
	
	
	@Override
	public void read(PacketByteBuf buffer)
		throws IOException
	{
		this.data.clear();
		this.entityId   = buffer.readInt();
		this.replaceAll = buffer.readBoolean();
		int amount = buffer.readByte();
		for (int i = 0; i < amount; i++)
			data.add(WearablesEntry.createFromBuffer(buffer));
	}
	
	@Override
	public void write(PacketByteBuf buffer)
		throws IOException
	{
		buffer.writeInt(this.entityId);
		buffer.writeBoolean(this.replaceAll);
		buffer.writeByte(this.data.size());
		for (WearablesEntry entry : this.data)
			entry.write(buffer);
	}
}
