package net.mcft.copy.wearables.common.mixin;

import net.mcft.copy.wearables.common.network.WearablesUpdatePacket;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.dimension.DimensionType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin
{
	@Inject(method="onPlayerConnect", at=@At("TAIL"))
	public void onPlayerConnect(ClientConnection conn, ServerPlayerEntity player, CallbackInfo info)
		{ WearablesUpdatePacket.sendForEntity(player, player.networkHandler::sendPacket); }
	
	@Inject(method="respawnPlayer", at=@At("TAIL"))
	public void respawnPlayer(ServerPlayerEntity player, DimensionType dimType, boolean boolean_1,
	                          CallbackInfoReturnable<ServerPlayerEntity> info)
		{ WearablesUpdatePacket.sendForEntity(player, player.networkHandler::sendPacket); }
}
