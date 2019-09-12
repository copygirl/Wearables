package net.mcft.copy.wearables.client.mixin;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.spongepowered.asm.mixin.Mixin;

import net.mcft.copy.wearables.api.IWearablesContainerId;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen.CreativeContainer;
import net.minecraft.container.Container;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Lazy;

// FIXME: Currently unsupported, thus disabled.
@Mixin(CreativeContainer.class)
public abstract class CreativeContainerMixin
	extends Container
	implements IWearablesContainerId
{
	private CreativeContainerMixin()
		{ super(null, 0); }
	
	
	// IWearablesContainer implementation
	
	private final Lazy<Map<String, Entity>> wearables_entityMap =
		new Lazy<>(() -> ImmutableMap.<String, Entity>builder()
			.put("player", getWearablesDefaultEntity())
			.build());
	
	@Override
	public Identifier getWearablesIdentifier()
		{ return new Identifier("creative"); }
	
	@Override
	public Entity getWearablesDefaultEntity()
		{ return MinecraftClient.getInstance().player; }
	
	@Override
	public Map<String, Entity> getWearablesEntityMap()
		{ return this.wearables_entityMap.get(); }
}
