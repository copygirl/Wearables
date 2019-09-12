package net.mcft.copy.wearables.common.mixin.container;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.container.Container;
import net.minecraft.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;

@Mixin(Container.class)
public interface ContainerAccessor
{
	@Accessor
	public DefaultedList<ItemStack> getStackList();
	@Accessor
	public List<Slot> getSlotList();
	
	@Invoker
	public Slot invokeAddSlot(Slot slot);
}
