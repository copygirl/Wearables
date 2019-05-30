package net.mcft.copy.wearables.common.mixin;

import com.mojang.authlib.GameProfile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.mcft.copy.wearables.common.WearablesArmorSlot;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin
	extends LivingEntity
{
	private PlayerEntityMixin()
		{ super(null, null); }
	
	@Inject(method="<init>", at=@At("RETURN"))
	protected void init(World world, GameProfile profile, CallbackInfo info)
	{
		PlayerEntity player = (PlayerEntity)(Object)this;
		WearablesArmorSlot.replaceVanillaArmorSlots(player, player.playerContainer);
	}
}
