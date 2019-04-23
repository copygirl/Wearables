package net.mcft.copy.wearables.common.mixin;

import java.util.function.Consumer;

import net.mcft.copy.wearables.common.network.WearablesUpdatePacket;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.server.network.EntityTrackerEntry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTrackerEntry.class)
public abstract class EntityTrackerEntryMixin
{
	@Shadow
	private Entity entity;
	
	@Inject(method="sendPackets", at=@At("TAIL"))
	public void sendPackets(Consumer<Packet<?>> sendPacket, CallbackInfo info)
		{ WearablesUpdatePacket.sendForEntity(this.entity, sendPacket); }
}
