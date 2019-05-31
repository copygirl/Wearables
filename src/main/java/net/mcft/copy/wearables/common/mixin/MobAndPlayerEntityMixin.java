package net.mcft.copy.wearables.common.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.mcft.copy.wearables.api.IWearablesData;
import net.mcft.copy.wearables.api.IWearablesEntity;
import net.mcft.copy.wearables.api.IWearablesSlotType;
import net.mcft.copy.wearables.common.impl.WearablesDataImpl;
import net.mcft.copy.wearables.common.impl.WearablesSlotImplVanilla;

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
		if (slot.getType() == EquipmentSlot.Type.ARMOR) {
			if (ItemStack.areEqual(stack, getEquippedStack(slot))) info.cancel();
			else wearables_getSlot(slot).onBeforeSet();
		}
	}
	
	@Inject(method="setEquippedStack", at=@At("TAIL"))
	public void onAfterSetEquippedStack(EquipmentSlot slot, ItemStack stack, CallbackInfo info)
	{
		if (slot.getType() == EquipmentSlot.Type.ARMOR)
			wearables_getSlot(slot).onAfterSet();
	}
	
	
	private WearablesSlotImplVanilla wearables_getSlot(EquipmentSlot slot)
	{
		IWearablesSlotType slotType = ((WearablesDataImpl)IWearablesData.INSTANCE).getSlotType(slot);
		return (WearablesSlotImplVanilla)(IWearablesEntity.from(this)).getWearablesSlot(slotType, 0);
	}
}