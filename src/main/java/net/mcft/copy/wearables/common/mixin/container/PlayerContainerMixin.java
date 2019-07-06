package net.mcft.copy.wearables.common.mixin.container;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.mcft.copy.wearables.api.IWearablesContainer;

import net.minecraft.container.CraftingContainer;
import net.minecraft.container.PlayerContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.util.Identifier;
import net.minecraft.util.Lazy;

@Mixin(PlayerContainer.class)
public abstract class PlayerContainerMixin
	extends CraftingContainer<CraftingInventory>
	implements IWearablesContainer
{
	@Shadow
   private PlayerEntity owner;
	
	private PlayerContainerMixin()
		{ super(null, 0); }
	
	
	// IWearablesContainer implementation
	
	private final Lazy<Map<String, Entity>> wearables_entityMap =
		new Lazy<>(() -> ImmutableMap.<String, Entity>builder()
			.put("player", getWearablesDefaultEntity())
			.build());
	
	@Override
	public Identifier getWearablesIdentifier()
		{ return new Identifier("survival"); }
	
	@Override
	public Entity getWearablesDefaultEntity()
		{ return this.owner; }
	
	@Override
	public Map<String, Entity> getWearablesEntityMap()
		{ return this.wearables_entityMap.get(); }
}
