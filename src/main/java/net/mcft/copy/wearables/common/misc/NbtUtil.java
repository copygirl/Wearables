package net.mcft.copy.wearables.common.misc;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.stream.Collector.Characteristics;

import com.google.common.collect.Iterables;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;

/** Contains NBT related methods for manipulating NBT tags and item stacks. */
public final class NbtUtil
{
	private NbtUtil() {  }
	
	
	public static final String TAG_INDEX = "index";
	public static final String TAG_STACK = "stack";
	
	
	// Utility ItemStack / NBT manipulation methods
	
	/** Gets a Tag from the specified {@link ItemStack ItemStack}'s Tag data, or null if it doesn't exist.
	 *  Example: <pre>{@code NbtUtil.get(stack, "display", "color") }</pre> */
	public static Tag get(ItemStack stack, String... tags)
		{ return get(stack.getTag(), tags); }
	/** Gets a child Tag from the specified {@link CompoundTag CompoundTag}, or null if it doesn't exist.
	 *  Example: <pre>{@code NbtUtil.get(compound, "display", "color") }</pre> */
	public static Tag get(CompoundTag compound, String... tags)
	{
		if (compound == null) return null;
		String tag = null;
		for (int i = 0; i < tags.length; i++) {
			tag = tags[i];
			if (!compound.containsKey(tag)) return null;
			if (i == tags.length - 1) break;
			compound = compound.getCompound(tag);
		}
		return compound.getTag(tag);
	}
	
	/** Gets a value from the specified {@link ItemStack ItemStack's} Tag data, or the specified default if it doesn't exist.
	 *  Example: <pre>{@code NbtUtil.get(stack, -1, "display", "color") }</pre> */
	public static <T> T get(ItemStack stack, T defaultValue, String... tags)
		{ return get(stack.getTag(), defaultValue, tags); }
	/** Gets a value from the specified {@link CompoundTag CompoundTag}, or the specified default if it doesn't exist.
	 *  Example: <pre>{@code NbtUtil.get(compound, -1, "display", "color") }</pre> */
	public static <T> T get(CompoundTag compound, T defaultValue, String... tags)
	{
		Tag tag = get(compound, tags);
		return ((tag != null) ? getTagValue(tag) : defaultValue);
	}
	
	/** Gets a {@link ListTag ListTag} from the specified {@link ItemStack ItemStack's} Tag data, or an empty dummy Tag if it doesn't exist.
	 *  Example: <pre>{@code NbtUtil.getList(stack, "display", "lore") }</pre> */
	 public static ListTag getList(ItemStack stack, String... tags)
	 	{ return getList(stack.getTag(), tags); }
	/** Gets a child {@link ListTag ListTag} from the specified CompoundTag, or an empty dummy Tag if it doesn't exist.
	 *  Example: <pre>{@code NbtUtil.getList(compound, "display", "lore") }</pre> */
	public static ListTag getList(CompoundTag compound, String... tags)
	{
		Tag tag = get(compound, tags);
		return ((tag != null) ? (ListTag)tag : new ListTag());
	}
	
	/** Gets a {@link CompoundTag CompoundTag} from the specified {@link ItemStack ItemStack's} Tag data, or an empty dummy Tag if it doesn't exist.
	 *  Example: <pre>{@code NbtUtil.getCompound(stack, "display") }</pre> */
	 public static CompoundTag getCompound(ItemStack stack, String... tags)
	 	{ return getCompound(stack.getTag(), tags); }
	/** Gets a child {@link CompoundTag CompoundTag} from the specified CompoundTag, or an empty dummy Tag if it doesn't exist.
	 *  Example: <pre>{@code NbtUtil.getCompound(compound, "display") }</pre> */
	public static CompoundTag getCompound(CompoundTag compound, String... tags)
	{
		Tag tag = get(compound, tags);
		return ((tag != null) ? (CompoundTag)tag : new CompoundTag());
	}
	
	/** Returns if the specified {@link ItemStack ItemStack's} Tag data has a certain child Tag.
	 *  Example: <pre>{@code NbtUtil.has(stack, "display", "color") }</pre> */
	public static boolean has(ItemStack stack, String... tags)
		{ return has(stack.getTag(), tags); }
	/** Returns if the specified {@link CompoundTag CompoundTag} has a certain child Tag.
	 *  Example: <pre>{@code NbtUtil.has(compound, "display", "color") }</pre> */
	public static boolean has(CompoundTag compound, String... tags)
		{ return (get(compound, tags) != null); }
	
	/** Adds or replaces a tag on the specified {@link ItemStack ItemStack's} Tag data, creating it and any parent Tags if necessary.
	 *  Example: <pre>{@code NbtUtil.set(stack, new NBTTagInt(0xFF0000), "display", "color") }</pre> */
	public static void set(ItemStack stack, Tag nbtTag, String... tags)
	{
		if (stack.isEmpty())
			throw new IllegalArgumentException("stack is empty");
		CompoundTag compound = stack.getTag();
		if (compound == null) stack.setTag(compound = new CompoundTag());
		set(compound, nbtTag, tags);
	}
	/** Adds or replaces a tag on the specified {@link CompoundTag CompoundTag}, creating parent Tags if necessary.
	 *  Example: <pre>{@code NbtUtil.set(compound, new NBTTagInt(0xFF0000), "display", "color") }</pre> */
	public static void set(CompoundTag compound, Tag nbtTag, String... tags)
	{
		if (compound == null)
			throw new IllegalArgumentException("compound is null");
		String tag = null;
		for (int i = 0; i < tags.length; i++) {
			tag = tags[i];
			if (i == tags.length - 1) break;
			if (!compound.containsKey(tag)) {
				CompoundTag child = new CompoundTag();
				compound.put(tag, child);
				compound = child;
			} else compound = compound.getCompound(tag);
		}
		compound.put(tag, nbtTag);
	}
	
	/** Adds or replaces a value on the specified {@link ItemStack ItemStack's} Tag data, creating it and any parent Tags if necessary.
	 *  Example: <pre>{@code NbtUtil.set(stack, 0xFF0000, "display", "color") }</pre> */
	public static <T> void set(ItemStack stack, T value, String... tags)
		{ set(stack, createTag(value), tags); }
	/** Adds or replaces a value on the specified {@link CompoundTag CompoundTag}, creating parent Tags if necessary.
	 *  Example: <pre>{@code NbtUtil.set(compound, 0xFF0000, "display", "color") }</pre> */
	public static <T> void set(CompoundTag compound, T value, String... tags)
		{ set(compound, createTag(value), tags); }
	
	/** Removes a certain Tag from the specified {@link ItemStack ItemStack's} Tag data.
	 *  Example: <pre>{@code NbtUtil.remove(stack, "display", "color") }</pre> */
	public static void remove(ItemStack stack, String... tags)
	{
		if (tags.length == 0) throw new IllegalArgumentException(
			"tags should have at least one element");
		if (!stack.hasTag()) return;
		
		CompoundTag compound = stack.getTag();
		remove(compound, tags);
		// If compound is empty, remove it from the stack.
		if (compound.isEmpty())
			stack.setTag(null);
	}
	/** Removes a certain Tag from the specified {@link CompoundTag CompoundTag}.
	 *  Example: <pre>{@code NbtUtil.remove(compound, "display", "color") }</pre> */
	public static void remove(CompoundTag compound, String... tags)
	{
		if (tags.length == 0) throw new IllegalArgumentException(
			"tags should have at least one element");
		if (compound == null) return;
		
		if (tags.length > 1) {
			Tag tag = compound.getTag(tags[0]);
			if (!(tag instanceof CompoundTag)) return;
			CompoundTag subCompound = (CompoundTag)tag;
			remove(subCompound, (String[])Arrays.copyOfRange(tags, 1, tags.length));
			// If subCompound is empty, remove it from the parent compound.
			if (!subCompound.isEmpty()) return;
		}
		compound.remove(tags[0]);
	}
	
	
	// CompoundTag / ListTag creation
	
	/** Creates a {@link CompoundTag CompoundTag} from the
	 *  specified name-value pairs. Skips pairs with a null value.
	 *  Example: <pre>{@code NbtUtil.createCompound("id", 1, "name", "copygirl") }</pre> */
	public static CompoundTag createCompound(Object... nameValuePairs)
		{ return addToCompound(new CompoundTag(), nameValuePairs); }
	
	/** Adds the specied name-value pairs as entries to the specified
	 *  {@link CompoundTag CompoundTag}. Skips pairs with a null value.
	 *  Example: <pre>{@code NbtUtil.addToCompound(compound, "id", 1, "name", "copygirl") }</pre> */
	public static CompoundTag addToCompound(CompoundTag compound, Object... nameValuePairs)
	{
		if (compound == null) throw new IllegalArgumentException("compound is null");
		for (int i = 0; i < nameValuePairs.length; i += 2) {
			String name = (String)nameValuePairs[i];
			Object value = nameValuePairs[i + 1];
			if (value == null) continue;
			compound.put(name, createTag(value));
		}
		return compound;
	}
	
	/** Creates a {@link ListTag ListTag} with the specified
	 *  values by calling {@link createTag createTag} on each.
	 *  All values have to be of the same type. Skips null values. */
	public static ListTag createList(Object... values)
		{ return addToList(new ListTag(), values); }
	
	/** Adds values to a {@link ListTag ListTag}. Skips null values. */
	public static ListTag addToList(ListTag list, Object... values)
	{
		if (list == null) throw new IllegalArgumentException("list is null");
		for (Object value : values) {
			if (value == null) continue;
			list.add(createTag(value));
		}
		return list;
	}
	
	
	// Reading / writing ItemStacks
	
	/** Writes an item stack to a {@link CompoundTag CompoundTag}. */
	public static CompoundTag writeItem(ItemStack item)
		{ return writeItem(item, true); }
	
	/** Writes an item stack to a {@link CompoundTag CompoundTag}. */
	public static CompoundTag writeItem(ItemStack item, boolean writeNullAsEmptyCompound)
	{
		return !item.isEmpty()
			? item.toTag(new CompoundTag())
			: (writeNullAsEmptyCompound ? new CompoundTag() : null);
	}
	
	/** Reads an item stack from a {@link CompoundTag CompoundTag}. */
	public static ItemStack readItem(CompoundTag compound)
	{
		return ((compound != null) && !compound.isEmpty())
			? ItemStack.fromTag(compound) : ItemStack.EMPTY;
	}
	
	
	/** Writes an item stack array to a {@link ListTag ListTag}. */
	public static ListTag writeItems(ItemStack[] items)
	{
		ListTag list = new ListTag();
		for (int i = 0; i < items.length; i++) {
			if (items[i] == null) continue;
			list.add(createCompound(
				TAG_INDEX, (short)i,
				TAG_STACK, writeItem(items[i])));
		}
		return list;
	}
	
	/** Reads items from a {@link ListTag ListTag} to an {@link ItemStack ItemStack} array. */
	public static ItemStack[] readItems(ListTag list, ItemStack[] items)
		{ return readItems(list, items, null); }
	
	/** Reads items from a {@link ListTag ListTag} to an {@link ItemStack ItemStack} array.
	 *  Any items falling outside the range of the items array
	 *  will get added to the invalid list if that's non-null. */
	public static ItemStack[] readItems(ListTag list, ItemStack[] items, List<ItemStack> invalid)
	{
		for (int i = 0; i < list.size(); i++) {
			CompoundTag compound = list.getCompoundTag(i);
			int index = compound.getShort(TAG_INDEX);
			ItemStack stack = readItem(compound.getCompound(TAG_STACK));
			if ((index >= 0) || (index < items.length))
				items[index] = stack;
			else if (invalid != null)
				invalid.add(stack);
		}
		return items;
	}
	
	
	// Other utility functions
	
	/**
	 * Returns the primitive value of a {@link Tag Tag}, casted to the return type.
	 * @exception IllegalArgumentException Thrown if tag is null or not a primitive Tag.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getTagValue(Tag tag)
	{
		if (tag == null) throw new IllegalArgumentException("tag is null");
		
		if (tag instanceof ByteTag)      return (T)(Object)((ByteTag)tag).getByte();
		if (tag instanceof ShortTag)     return (T)(Object)((ShortTag)tag).getShort();
		if (tag instanceof IntTag)       return (T)(Object)((IntTag)tag).getInt();
		if (tag instanceof LongTag)      return (T)(Object)((LongTag)tag).getLong();
		if (tag instanceof FloatTag)     return (T)(Object)((FloatTag)tag).getFloat();
		if (tag instanceof DoubleTag)    return (T)(Object)((DoubleTag)tag).getDouble();
		if (tag instanceof StringTag)    return (T)(Object)((StringTag)tag).asString();
		if (tag instanceof ByteArrayTag) return (T)((ByteArrayTag)tag).getByteArray();
		if (tag instanceof IntArrayTag)  return (T)((IntArrayTag)tag).getIntArray();
		
		throw new IllegalArgumentException(Tag.TYPES[tag.getType()] + " isn't a primitive tag");
	}
	
	/**
	 * Creates and returns a primitive {@link Tag Tag} from a value. <p>
	 * 
	 * If the value is..
	 * <ul>
	 *   <li>.. already a {@link Tag Tag}, it is returned directly.</li>
	 *   <li>.. an {@link ItemStack ItemStack}, it is serialized using {@link ItemStack#toTag toTag}.</li>
	 *   <li>.. an {@link INbtSerializable INbtSerializable}, it is serialized.</li>
	 *   <li>.. a Collection, createTag is called on each element, added to a {@link ListTag ListTag}.</li>
	 *   <li>.. a primitive type (e.g. Byte), a primitive {@link Tag Tag} (e.g. {@link ByteTag ByteTag}) is returned.</li>
	 * </ul><p>
	 * 
	 * If the value is none of these, an exception is thrown.
	 * 
	 * @param value The value to be turned into a Tag.
	 * @exception IllegalArgumentException Thrown if value is null or not a supported type.
	 */
	public static Tag createTag(Object value)
	{
		if (value == null)
			throw new IllegalArgumentException("value is null");
		
		if (value instanceof Tag) return (Tag)value;
		if (value instanceof ItemStack)
			return ((ItemStack)value).toTag(new CompoundTag());
		if (value instanceof INbtSerializable)
			return ((INbtSerializable<?>)value).serializeToTag();
		if (value instanceof Collection) return ((Collection<?>)value)
			.stream().map(NbtUtil::createTag).collect(toList());
		
		if (value instanceof Byte)    return new ByteTag((Byte)value);
		if (value instanceof Short)   return new ShortTag((Short)value);
		if (value instanceof Integer) return new IntTag((Integer)value);
		if (value instanceof Long)    return new LongTag((Long)value);
		if (value instanceof Float)   return new FloatTag((Float)value);
		if (value instanceof Double)  return new DoubleTag((Double)value);
		if (value instanceof String)  return new StringTag((String)value);
		if (value instanceof byte[])  return new ByteArrayTag((byte[])value);
		if (value instanceof int[])   return new IntArrayTag((int[])value);
		
		throw new IllegalArgumentException("Can't create an NBT tag of value: " + value);
	}
	
	
	/** Returns the specified {@link INbtSerializable INbtSerializable}
	 *  value instance deserialized from the specified {@link Tag Tag}. */
	public static <N extends Tag, T extends INbtSerializable<N>> T asValue(N tag, T value)
	{
		if (tag == null) throw new IllegalArgumentException("tag is null");
		if (value == null) throw new IllegalArgumentException("value is null");
		value.deserializeFromTag(tag);
		return value;
	}
	
	/** Returns a list of {@link INbtSerializable INbtSerializable} values instantiated
	 *  using the value supplier from the specified {@link ListTag ListTag}. */
	@SuppressWarnings("unchecked")
	public static <N extends Tag, T extends INbtSerializable<N>> List<T> asList(
		ListTag list, Supplier<T> valueSupplier)
	{
		return list.stream()
			.map(tag -> asValue((N)tag, valueSupplier.get()))
			.collect(Collectors.toList());
	}
	
	
	// Iterable / Stream related functions
	
	/** Returns an iterable of entries in the specified {@link CompoundTag CompoundTag}. */
	public static Iterable<CompoundEntry> iterate(CompoundTag compound)
		{ return Iterables.transform(compound.getKeys(), key -> new CompoundEntry(key, compound.getTag(key))); }
	
	/** Returns a stream of entries in the specified {@link CompoundTag CompoundTag}. */
	public static Stream<CompoundEntry> stream(CompoundTag compound)
		{ return StreamSupport.stream(NbtUtil.iterate(compound).spliterator(), false); }
	
	/** Returns a collector that accumulates the the input elements into a new {@link ListTag ListTag}. */
	public static <T> Collector<T, ListTag, ListTag> toList()
	{
		return Collector.of(ListTag::new,
			(list, element) ->
				list.add(createTag(element)),
			(left, right) -> {
				for (Tag tag : right)
					left.add(tag);
				return left;
			}, Characteristics.IDENTITY_FINISH);
	}
	
	/** Returns a collector that accumulates the the input
	 *  {@link Tag Tags} into a new {@link CompoundTag CompoundTag}. */
	public static <T> Collector<T, CompoundTag, CompoundTag> toCompound(
		Function<T, String> keyMapper, Function<T, Tag> tagMapper)
	{
		return Collector.of(CompoundTag::new,
			(compound, element) ->
				compound.put(keyMapper.apply(element), tagMapper.apply(element)),
			(left, right) -> {
				for (String key : right.getKeys())
					left.put(key, right.getTag(key));
				return left;
			}, Characteristics.IDENTITY_FINISH);
	}
	
	public static class CompoundEntry
	{
		public final String key;
		public final Tag tag;
		public CompoundEntry(String key, Tag tag)
			{ this.key = key; this.tag = tag; }
	}
}
