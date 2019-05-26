package net.mcft.copy.wearables.common.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin({ MobEntity.class, PlayerEntity.class })
public abstract class MobAndPlayerEntityMixin
	extends LivingEntity
{
	private MobAndPlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world)
		{ super(null, null); }
	
	
	@Inject(method="setEquippedStack", at=@At("HEAD"))
	public void onBeforeSetEquippedStack(EquipmentSlot slot, ItemStack stack, CallbackInfo info)
	{
		// Call IWearablesItem.onEquip
	}
	
	@Inject(method="setEquippedStack", at=@At("TAIL"))
	public void onAfterSetEquippedStack(EquipmentSlot slot, ItemStack stack, CallbackInfo info)
	{
		// Call IWearablesItem.onUnequip
	}
}