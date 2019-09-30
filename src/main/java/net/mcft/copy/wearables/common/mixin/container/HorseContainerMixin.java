package net.mcft.copy.wearables.common.mixin.container;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.mcft.copy.wearables.api.IWearablesContainerId;

import net.minecraft.container.Container;
import net.minecraft.container.HorseContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Lazy;

@Mixin(HorseContainer.class)
public abstract class HorseContainerMixin
	extends Container
	implements IWearablesContainerId
{
	@Shadow
	private HorseBaseEntity entity;
	
	private HorseContainerMixin()
		{ super(null, 0); }
	
	
	// IWearablesContainer implementation
	
	private final Lazy<Map<String, Entity>> wearables_entityMap =
		new Lazy<>(() -> ImmutableMap.<String, Entity>builder()
			.put("horse", getWearablesDefaultEntity())
			.build());
	
	@Override
	public Identifier getWearablesIdentifier()
		{ return new Identifier("survival"); }
	
	@Override
	public Entity getWearablesDefaultEntity()
		{ return this.entity; }
	
	@Override
	public Map<String, Entity> getWearablesEntityMap()
		{ return this.wearables_entityMap.get(); }
}
