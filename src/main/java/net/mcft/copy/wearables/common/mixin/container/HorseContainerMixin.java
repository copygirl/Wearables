package net.mcft.copy.wearables.common.mixin.container;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.mcft.copy.wearables.api.IWearablesContainerId;

import net.minecraft.container.Container;
import net.minecraft.container.HorseContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.Identifier;
import net.minecraft.util.Lazy;

@Mixin(HorseContainer.class)
public abstract class HorseContainerMixin
	extends Container
	implements IWearablesContainerId
{
	private HorseContainerMixin()
		{ super(null, 0); }
	
	@Shadow
	private HorseBaseEntity entity;
	
	
	private PlayerEntity _wearables_player;
	
	@Inject(method="<init>", at=@At("RETURN"))
	protected void init(int syncId, PlayerInventory playerInventory, Inventory horseInventory,
	                    final HorseBaseEntity entity, CallbackInfo info)
		{ this._wearables_player = playerInventory.player; }
	
	
	// IWearablesContainer implementation
	
	private final Lazy<Map<String, Entity>> wearables_entityMap =
		new Lazy<>(() -> ImmutableMap.<String, Entity>builder()
			.put("horse", getWearablesDefaultEntity())
			.put("player", this._wearables_player)
			.build());
	
	@Override
	public Identifier getWearablesIdentifier()
		{ return new Identifier("horse"); }
	
	@Override
	public Entity getWearablesDefaultEntity()
		{ return this.entity; }
	
	@Override
	public Map<String, Entity> getWearablesEntityMap()
		{ return this.wearables_entityMap.get(); }
}
