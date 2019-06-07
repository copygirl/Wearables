package net.mcft.copy.wearables.common.misc;

import net.minecraft.nbt.Tag;

public interface INbtSerializable<N extends Tag>
{
	public N createTag();
	
	public void serializeToTag(N tag);
	
	public default N serializeToTag()
	{
		N tag = createTag();
		serializeToTag(tag);
		return tag;
	}
}
