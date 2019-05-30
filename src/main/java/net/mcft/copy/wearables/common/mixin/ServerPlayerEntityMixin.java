package net.mcft.copy.wearables.common.mixin;

import java.util.OptionalInt;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.mcft.copy.wearables.common.WearablesArmorSlot;

import net.minecraft.container.ContainerListener;
import net.minecraft.container.NameableContainerProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin
	extends PlayerEntity
	implements ContainerListener
{
	private ServerPlayerEntityMixin()
		{ super(null, null); }
	
	@Inject(method="openContainer", at=@At("TAIL"))
	public void openContainer(NameableContainerProvider provider, CallbackInfoReturnable<OptionalInt> info)
		{ WearablesArmorSlot.replaceVanillaArmorSlots(this, this.playerContainer); }
}
