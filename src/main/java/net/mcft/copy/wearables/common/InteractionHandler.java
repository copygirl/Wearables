package net.mcft.copy.wearables.common;

import net.mcft.copy.wearables.api.IWearablesSlot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public class InteractionHandler
{
	public enum Action
	{
		LEFT,
		RIGHT,
		PICK_ITEM
	}
	
	public enum Result
	{
		SUCCESS,
		INVALID_ACTION,
		CANT_UNEQUIP,
		CANT_EQUIP,
		NO_CREATIVE
	}
	
	// FIXME: Somehow support creative container.
	//        (Client-side only, no cursor stack on server, separate interaction packet.)
	public Result onInteract(PlayerEntity player, IWearablesSlot slot, Action action)
	{
		PlayerInventory inventory = player.inventory;
		ItemStack currentEquipped = slot.get();
		ItemStack cursorStack     = inventory.getCursorStack();
		
		if (action == Action.PICK_ITEM) {
			if (!player.isCreative()) return Result.NO_CREATIVE;
			if (cursorStack.isEmpty()) inventory.setCursorStack(currentEquipped);
			return Result.SUCCESS;
		}
		
		// FIXME: Handle ItemStacks with amount > 1 properly.
		if (!slot.canUnequip()) return Result.CANT_UNEQUIP;
		if (cursorStack.isEmpty() && currentEquipped.isEmpty()) return Result.INVALID_ACTION;
		if (!slot.canEquip(cursorStack)) return Result.CANT_EQUIP;
		
		inventory.setCursorStack(currentEquipped.copy());
		slot.set(cursorStack.copy());
		
		return Result.SUCCESS;
	}
}
