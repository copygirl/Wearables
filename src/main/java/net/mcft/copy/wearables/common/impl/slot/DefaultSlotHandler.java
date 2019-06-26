package net.mcft.copy.wearables.common.impl.slot;

import java.util.Collection;

import net.mcft.copy.wearables.WearablesCommon;
import net.mcft.copy.wearables.api.IWearablesSlot;
import net.mcft.copy.wearables.api.IWearablesSlotHandler;
import net.mcft.copy.wearables.api.WearablesSlotType;
import net.mcft.copy.wearables.common.WearablesEntityData;
import net.mcft.copy.wearables.common.WearablesEntityData.Entry;
import net.mcft.copy.wearables.common.impl.WearablesSlotImpl;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public final class DefaultSlotHandler
	implements IWearablesSlotHandler<Entity>
{
	public static final DefaultSlotHandler INSTANCE = new DefaultSlotHandler();
	private DefaultSlotHandler() {  }
	
	
	@Override
	public Collection<WearablesSlotType> getSlotTypes(EntityType<Entity> entityType)
		{ throw new UnsupportedOperationException(); }
	
	@Override
	public Identifier getIcon(WearablesSlotType slotType)
		{ return new Identifier(WearablesCommon.MOD_ID, "gui/icons/" + slotType.fullName.replaceAll("/", "_")); }
	
	
	@Override
	public ItemStack get(Entity entity, IWearablesSlot slot)
	{
		WearablesEntityData data = ((WearablesEntityData.IAccessor)entity).getWearablesData(false);
		return (data != null) ? data.get(slot.getSlotType(), slot.getIndex(), false).getStack()
		                      : ItemStack.EMPTY;
	}
	
	@Override
	public void set(Entity entity, IWearablesSlot slot, ItemStack value)
	{
		WearablesEntityData data = ((WearablesEntityData.IAccessor)entity).getWearablesData(true);
		Entry entry = data.get(slot.getSlotType(), slot.getIndex(), !value.isEmpty());
		
		slot.invokeBeforeUnequip(entry.getStack());
		if (entry != null) entry.setStack(value);
		slot.invokeAfterEquip(value);
		((WearablesSlotImpl)slot).sync();
	}
}
