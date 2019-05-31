package net.mcft.copy.wearables.common;

import net.mcft.copy.wearables.api.IWearablesEntity;
import net.mcft.copy.wearables.api.IWearablesSlot;
import net.mcft.copy.wearables.common.mixin.ISlotAccessor;

import net.minecraft.container.Container;
import net.minecraft.container.Slot;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class WearablesArmorSlot extends Slot
{
	private final Slot _base;
	
	public final EquipmentSlot vanillaSlot;
	public final IWearablesSlot wearablesSlot;
	
	public WearablesArmorSlot(Slot base, EquipmentSlot vanillaSlot, IWearablesSlot wearablesSlot)
	{
		super(base.inventory, ((ISlotAccessor)base).getInvSlot(), base.xPosition, base.yPosition);
		this.id    = base.id;
		this._base = base;
		this.vanillaSlot   = vanillaSlot;
		this.wearablesSlot = wearablesSlot;
	}
	
	@Override
	public int getMaxStackAmount() { return this._base.getMaxStackAmount(); }
	
	@Override
	public String getBackgroundSprite() { return this._base.getBackgroundSprite(); }
	
	@Override
	public boolean canInsert(ItemStack stack)
		{ return this.wearablesSlot.canEquip(stack); }
	
	@Override
	public boolean canTakeItems(PlayerEntity player)
		{ return this.wearablesSlot.canUnequip(); }
	
	
	/** Replaces Vanilla armor slots in the specified container by WearablesArmorSlot
	 *  wrappers, whill will override the equipment and unequipment behavior. */
	public static void replaceVanillaArmorSlots(PlayerEntity player, Container container)
	{
		IWearablesEntity wearablesEntity = IWearablesEntity.from(player);
		EquipmentSlot vanillaSlot;
		for (int i = 0; i < container.slotList.size(); i++) {
			Slot slot = container.slotList.get(i);
			if (!(slot instanceof WearablesArmorSlot) &&
			    ((vanillaSlot = getEquipmentSlot(slot, player)) != null))
				container.slotList.set(i, new WearablesArmorSlot(
					slot, vanillaSlot, wearablesEntity.getWearablesSlot(vanillaSlot)));
		}
	}
	
	private static EquipmentSlot getEquipmentSlot(Slot slot, PlayerEntity player)
	{
		if (slot.inventory != player.inventory) return null;
		switch (((ISlotAccessor)slot).getInvSlot()) {
			case 39: return EquipmentSlot.HEAD;
			case 38: return EquipmentSlot.CHEST;
			case 37: return EquipmentSlot.LEGS;
			case 36: return EquipmentSlot.FEET;
			default: return null;
		}
	}
}
