package net.mcft.copy.wearables.common.network;

import java.io.IOException;
import java.util.function.BiConsumer;

import io.netty.buffer.Unpooled;

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.PacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public final class NetworkUtil
{
	private NetworkUtil() {  }
	
	
	// Registry
	
	public static <T extends IPacket> void registerClientToServer(
		Class<T> clazz, BiConsumer<PacketContext, T> consumer)
			{ register(ServerSidePacketRegistry.INSTANCE, clazz, consumer); }
	
	public static <T extends IPacket> void registerServerToClient(
		Class<T> clazz, BiConsumer<PacketContext, T> consumer)
			{ register(ClientSidePacketRegistry.INSTANCE, clazz, consumer); }
	
	public static <T extends IPacket> void registerBiDirectional(
		Class<T> clazz, BiConsumer<PacketContext, T> consumer)
	{
		registerClientToServer(clazz, consumer);
		registerServerToClient(clazz, consumer);
	}
	
	private static <T extends IPacket> void register(
		PacketRegistry registry, Class<T> clazz,
		BiConsumer<PacketContext, T> consumer)
	{
		Identifier id;
		try { id = (Identifier)clazz.getField("ID").get(null); }
		catch (Exception ex) { throw new RuntimeException(ex); }
		
		registry.register(id, (context, buffer) -> {
			try {
				T packet = clazz.newInstance();
				packet.read(buffer);
				context.getTaskQueue().execute(() ->
					consumer.accept(context, packet));
			} catch (Exception ex) { throw new RuntimeException(ex); }
		});
	}
	
	
	// Sending packets
	
	public static void sendToPlayer(PlayerEntity player, IPacket packet)
	{
		ServerSidePacketRegistry.INSTANCE.sendToPlayer(player,
			toVanillaPacket(ServerSidePacketRegistry.INSTANCE, packet));
	}
	
	public static void sendToServer(IPacket packet)
	{
		ClientSidePacketRegistry.INSTANCE.sendToServer(
			toVanillaPacket(ClientSidePacketRegistry.INSTANCE, packet));
	}
	
	
	// Packet utility
	
	public static Packet<?> toVanillaPacket(PacketRegistry packetRegistry, IPacket packet)
	{
		if (packet == null) throw new IllegalArgumentException("packet is null");
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		try { packet.write(buffer); }
		catch (IOException ex) { throw new RuntimeException(ex); }
		
		return packetRegistry.toPacket(packet.getID(), buffer);
	}
}
