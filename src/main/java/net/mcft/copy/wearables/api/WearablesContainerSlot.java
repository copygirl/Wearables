package net.mcft.copy.wearables.api;

import net.minecraft.container.Slot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class WearablesContainerSlot
	extends Slot
{
	public final IWearablesSlot wearablesSlot;
	
	public WearablesContainerSlot(IWearablesSlot wearablesSlot, int x, int y)
	{
		super(null, 0, x, y);
		if (wearablesSlot == null) throw new IllegalArgumentException("wearablesSlot is null");
		this.wearablesSlot = wearablesSlot;
	}
	
	
	@Override
	public ItemStack getStack() { return wearablesSlot.get(); }
	
	@Override
	public void setStack(ItemStack value) { wearablesSlot.set(value); }
	
	@Override
	public ItemStack takeStack(int amount)
	{
		ItemStack stack = getStack();
		if ((stack.getCount() == 0) || (amount <= 0)) return ItemStack.EMPTY;
		stack = stack.copy();
		ItemStack result = stack.split(amount);
		setStack(stack);
		return result;
	}
	
	
	@Override
	public int getMaxStackAmount() { return 1; }
	
	@Override
	public void markDirty() {  }
	
	
	@Override
	public boolean canInsert(ItemStack stack)
		{ return wearablesSlot.canEquip(stack); }
	
	@Override
	public boolean canTakeItems(PlayerEntity player)
		{ return wearablesSlot.canUnequip(); }
}
