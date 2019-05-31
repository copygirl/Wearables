package net.mcft.copy.wearables.common.mixin;

import net.mcft.copy.wearables.common.network.WearablesUpdatePacket;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin
{
	@Inject(method="onPlayerConnect", at=@At("TAIL"))
	public void onPlayerConnect(ClientConnection conn, ServerPlayerEntity player, CallbackInfo info)
		{ WearablesUpdatePacket.sendForEntity(player, false, player.networkHandler::sendPacket); }
	
	@Inject(method="respawnPlayer", at=@At("TAIL"), locals=LocalCapture.CAPTURE_FAILHARD)
	public void respawnPlayer(ServerPlayerEntity oldPlayer, DimensionType dimType, boolean boolean_1,
	                          CallbackInfoReturnable<ServerPlayerEntity> info, BlockPos blockPos_1, boolean boolean_2,
	                          ServerPlayerInteractionManager interactionManager, ServerPlayerEntity newPlayer)
	{
		// TODO: Keep Wearables if keepInventory gamerule is on.
		WearablesUpdatePacket.sendForEntity(newPlayer, true, newPlayer.networkHandler::sendPacket);
	}
}
