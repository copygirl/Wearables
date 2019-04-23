package net.mcft.copy.wearables.common.misc;

import net.minecraft.nbt.Tag;

public interface INbtSerializable<T extends Tag>
{
	public T serializeToTag();
	
	public void deserializeFromTag(T tag);
}
