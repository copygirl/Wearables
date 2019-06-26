package net.mcft.copy.wearables.common.mixin;

import com.mojang.authlib.GameProfile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.mcft.copy.wearables.api.IWearablesEntity;
import net.mcft.copy.wearables.common.WearablesArmorSlot;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.GameRules;
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
	
	@Invoker
   public abstract ItemEntity invokeDropItem(ItemStack stack, boolean explode, boolean setThrower);
	
	@Inject(method="dropInventory", at=@At("TAIL"))
	protected void dropInventory(CallbackInfo info)
	{
		if (this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) return;
		IWearablesEntity.from(this).getEquippedWearables().forEach(slot -> {
			invokeDropItem(slot.get(), true, false);
			slot.set(ItemStack.EMPTY);
		});
	}
}
