package net.mcft.copy.wearables.common.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.container.Slot;

@Mixin(Slot.class)
public interface ISlotAccessor
{
	@Accessor
	public int getInvSlot();
}