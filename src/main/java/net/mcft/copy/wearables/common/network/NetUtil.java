package net.mcft.copy.wearables.common.network;

import java.io.IOException;
import java.util.function.BiConsumer;

import io.netty.buffer.Unpooled;

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.PacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public final class NetUtil
{
	private NetUtil() {  }
	
	
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
		if (player == null) throw new IllegalArgumentException("player is null");
		if (packet == null) throw new IllegalArgumentException("packet is null");
		ServerSidePacketRegistry.INSTANCE.sendToPlayer(player,
			toVanillaPacket(ServerSidePacketRegistry.INSTANCE, packet));
	}
	
	public static void sendToPlayersTracking(Entity entity, IPacket packet, boolean sendToSelf)
	{
		if (entity == null) throw new IllegalArgumentException("entity is null");
		if (packet == null) throw new IllegalArgumentException("packet is null");
		if (entity.world == null) throw new IllegalArgumentException("entity.world is null");
		if (entity.world.isClient) throw new IllegalStateException("Called on client-side");
		
		ServerChunkManager chunkManager = (ServerChunkManager)entity.world.getChunkManager();
		Packet<?> vanillaPacket = toVanillaPacket(ServerSidePacketRegistry.INSTANCE, packet);
		
		if (sendToSelf) chunkManager.sendToNearbyPlayers(entity, vanillaPacket);
		else chunkManager.sendToOtherNearbyPlayers(entity, vanillaPacket);
	}
	
	public static void sendToServer(IPacket packet)
	{
		if (packet == null) throw new IllegalArgumentException("packet is null");
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
