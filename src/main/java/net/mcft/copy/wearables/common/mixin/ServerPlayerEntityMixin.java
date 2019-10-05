package net.mcft.copy.wearables.common.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.mcft.copy.wearables.api.IWearablesEntity;
import net.mcft.copy.wearables.common.WearablesContainerData;
import net.mcft.copy.wearables.common.network.NetUtil;
import net.mcft.copy.wearables.common.network.WearablesContainerPacketS2C;
import net.minecraft.container.Container;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.DefaultedList;
import net.minecraft.world.GameRules;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin
	extends PlayerEntity
{
	private ServerPlayerEntityMixin()
		{ super(null, null); }
	
	@Inject(method="copyFrom", at=@At("TAIL"))
	public void copyFrom(ServerPlayerEntity other, boolean changeDimension, CallbackInfo info)
	{
		if (!changeDimension && !other.isSpectator()
		 && !this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) return;
		IWearablesEntity self = IWearablesEntity.from(this);
		// FIXME: Desync and re-equipment sounds occur due to this?
		IWearablesEntity.from(other).getEquippedWearables().forEach(slot ->
			self.getWearablesSlot(slot.getSlotType(), slot.getIndex(), true).set(slot.get()));
	}
	
	@Inject(method="onContainerRegistered", at=@At("HEAD"))
	public void onContainerRegistered(Container container, DefaultedList<ItemStack> contents, CallbackInfo info)
	{
		WearablesContainerData data = WearablesContainerData.from(container);
		data.computeAndAddRegionEntries();
		if (data.getRegions() != null)
			NetUtil.sendToPlayer(this, new WearablesContainerPacketS2C(data));
	}
}
