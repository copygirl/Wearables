package net.mcft.copy.wearables.common.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.mcft.copy.wearables.api.IWearablesEntity;

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
	
	
	@Inject(method="setEquippedStack", at=@At("HEAD"), cancellable=true)
	public void onBeforeSetEquippedStack(EquipmentSlot slot, ItemStack stack, CallbackInfo info)
	{
		if (IWearablesEntity.is(this) && (slot.getType() == EquipmentSlot.Type.ARMOR)) {
			ItemStack previous = getEquippedStack(slot);
			if (ItemStack.areEqual(stack, previous)) info.cancel();
			else IWearablesEntity.from(this).getWearablesSlot(slot).invokeBeforeUnequip(previous);
		}
	}
	
	@Inject(method="setEquippedStack", at=@At("TAIL"))
	public void onAfterSetEquippedStack(EquipmentSlot slot, ItemStack stack, CallbackInfo info)
	{
		if (IWearablesEntity.is(this) && (slot.getType() == EquipmentSlot.Type.ARMOR))
			IWearablesEntity.from(this).getWearablesSlot(slot).invokeAfterEquip(stack);
	}
}
