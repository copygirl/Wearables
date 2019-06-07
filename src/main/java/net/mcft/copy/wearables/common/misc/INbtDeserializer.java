package net.mcft.copy.wearables.common.misc;

import net.minecraft.nbt.Tag;

public interface INbtDeserializer<T, N extends Tag>
{
	public T deserializeFromTag(N tag);
}
