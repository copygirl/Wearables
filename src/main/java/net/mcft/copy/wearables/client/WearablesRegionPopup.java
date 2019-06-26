package net.mcft.copy.wearables.client;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.mojang.blaze3d.platform.GlStateManager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.mcft.copy.wearables.api.IWearablesContainer;
import net.mcft.copy.wearables.api.IWearablesEntity;
import net.mcft.copy.wearables.api.IWearablesSlot;
import net.mcft.copy.wearables.api.WearablesRegion;
import net.mcft.copy.wearables.api.WearablesSlotType;
import net.mcft.copy.wearables.client.mixin.IContainerScreenAccessor;
import net.mcft.copy.wearables.common.data.EntityTypeData;
import net.mcft.copy.wearables.common.data.WearablesData;
import net.mcft.copy.wearables.common.data.WearablesData.ContainerData;
import net.mcft.copy.wearables.common.impl.slot.DefaultSlotHandler;
import net.mcft.copy.wearables.common.misc.Position;
import net.mcft.copy.wearables.common.network.NetUtil;
import net.mcft.copy.wearables.common.network.WearablesInteractPacket;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

// TODO: Render item stack tooltip.
// TODO: Reimplement highlighted slots.
@Environment(EnvType.CLIENT)
public class WearablesRegionPopup
	extends DrawableHelper
	implements Drawable, Element
{
	public static final GuiTexture REGION_TEX = new GuiTexture("region", 64);
	public static final int BORDER_SIZE = 4;
	public static final int SLOT_SIZE   = 18;
	public static final int Z_LEVEL     = 200;
	
	
	private final MinecraftClient _client    = MinecraftClient.getInstance();
	private final ClientPlayerEntity _player = this._client.player;
	private final PlayerInventory _inventory = this._player.inventory;
	
	public final IContainerScreenAccessor<?> screen;
	public final IWearablesContainer wearCont;
	public final Entity entity;
	
	private int _version;
	private ContainerData _data;
	
	public boolean isVisible = false;
	public List<RegionEntry> regions;
	
	public Position pos;
	public int width, height;
	public WearablesRegion curRegion;
	public List<SlotEntry> slots;
	
	
	public WearablesRegionPopup(AbstractContainerScreen<?> screen, Entity entity)
	{
		this.screen   = (IContainerScreenAccessor<?>)screen;
		this.wearCont = IWearablesContainer.from(screen);
		this.entity   = entity;
		init();
	}
	
	public void init()
	{
		this._version = WearablesData.INSTANCE.version;
		
		Class<?> clazz = screen.getClass();
		do {
			this._data = WearablesData.INSTANCE.containers.get(clazz);
			clazz = clazz.getSuperclass();
		} while ((this._data == null) && AbstractContainerScreen.class.isAssignableFrom(clazz));
		
		IWearablesEntity wearablesEntity = IWearablesEntity.from(entity);
		this.regions = this._data.regionPositions.entrySet().stream()
			.map(entry -> wearablesEntity.getSupportedWearablesSlots(entry.getKey())
				.reduce((a, b) -> Math.abs(a.getOrder()) < Math.abs(b.getOrder()) ? a : b)
				.map(slot -> new RegionEntry(entry.getKey(), entry.getValue(), slot, getIcon(slot.getSlotType()))))
			.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
		
		hide();
	}
	
	public void show(WearablesRegion region, Position slotPos)
	{
		this.isVisible = true;
		this.curRegion = region;
		
		IWearablesEntity wearablesEntity = IWearablesEntity.from(entity);
		SortedSet<IWearablesSlot> slots = new TreeSet<>();
		wearablesEntity.getSupportedWearablesSlots(region).forEach(slots::add);
		wearablesEntity.getEquippedWearables(region).forEach(slots::add);
		// TODO: Optionally filter for which slots are currently relevant.
		
		Iterator<IWearablesSlot> iter = slots.iterator();
		this.slots = IntStream.range(0, slots.size()).mapToObj(i -> {
				IWearablesSlot slot = iter.next();
				Position pos = new Position(BORDER_SIZE + i * SLOT_SIZE, BORDER_SIZE);
				return new SlotEntry(slot, pos, getIcon(slot.getSlotType()));
			}).collect(Collectors.toList());
		if (this.slots.isEmpty()) { hide(); return; }
		
		int centerIndex = -1;
		int minAbsOrder = Integer.MAX_VALUE;
		for (int i = 0; i < this.slots.size(); i++) {
			int order = Math.abs(this.slots.get(i).slot.getOrder());
			if (order >= minAbsOrder) break;
			centerIndex = i;
			minAbsOrder = order;
		}
		
		int x = screen.getLeft() + slotPos.x - BORDER_SIZE - SLOT_SIZE * centerIndex;
		int y = screen.getTop()  + slotPos.y - BORDER_SIZE;
		this.pos    = new Position(x, y);
		this.width  = BORDER_SIZE * 2 + SLOT_SIZE * this.slots.size();
		this.height = BORDER_SIZE * 2 + SLOT_SIZE;
	}
	
	private Identifier getIcon(WearablesSlotType slotType)
	{
		return Optional.ofNullable(EntityTypeData.from(this.entity).slotTypes.get(slotType))
		               .map(slotTypeData -> slotTypeData.handler)
		               .orElse(DefaultSlotHandler.INSTANCE)
		               .getIcon(slotType);
	}
	
	public void hide()
	{
		if (!this.isVisible) return;
		
		this.isVisible = false;
		this.pos   = null;
		this.width = this.height = 0;
		
		this.curRegion = null;
		this.slots     = null;
	}
	
	
	@Override
	public boolean isMouseOver(double pointX, double pointY)
	{
		return this.isVisible && isWithinGlobalSpace(
			this.pos.x, this.pos.y, this.width, this.height, pointX, pointY);
	}
	
	private boolean isOverSlot(Position slotPos, double pointX, double pointY)
		{ return isWithinScreenSpace(slotPos.x, slotPos.y, SLOT_SIZE, SLOT_SIZE, pointX, pointY); }
	
	private boolean isWithinScreenSpace(int x, int y, int width, int height, double pointX, double pointY)
		{ return isWithinGlobalSpace(x + screen.getLeft(), y + screen.getTop(), width, height, pointX, pointY); }
	
	private boolean isWithinGlobalSpace(int x, int y, int width, int height, double pointX, double pointY)
		{ return (pointX >= x) && (pointX < x + width) && (pointY >= y) && (pointY < y + height); }
	
	private Optional<SlotEntry> findSlotAt(int mouseX, int mouseY)
	{
		Position pos = this.pos.subtract(screen.getLeft(), screen.getTop());
		return this.slots.stream().filter(entry -> isOverSlot(pos.add(entry.pos), mouseX, mouseY)).findFirst();
	}
	
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		if (!isMouseOver(mouseX, mouseY)) return false;
		
		// FIXME: Fix click-hold-release over real slot causing item to be dropped there.
		
		Optional<SlotEntry> entry = findSlotAt((int)mouseX, (int)mouseY);
		if (!entry.isPresent()) return true;
		IWearablesSlot slot = entry.get().slot;
		
		// TODO: Handle pick keybind if it's a key, too!
		// if (this._client.options.keyPickItem.matchesMouse(button)) {
		// 	if (this._player.isCreative() && this._inventory.getCursorStack().isEmpty()) {
		// 		ItemStack stack = slot.get().copy();
		// 		stack.setAmount(stack.getMaxAmount());
		// 		this._inventory.setCursorStack(stack);
		// 	}
		// 	return true;
		// }
		
		// FIXME: Handle different mouse buttons properly.
		if (!slot.canUnequip()) return true;
		ItemStack cursorStack     = this._inventory.getCursorStack();
		ItemStack currentEquipped = slot.get();
		if (cursorStack.isEmpty() && currentEquipped.isEmpty()) return true;
		if (!slot.canEquip(cursorStack)) return true;
		
		// FIXME: Handle ItemStacks with amount > 1 properly.
		this._inventory.setCursorStack(currentEquipped.copy());
		slot.set(cursorStack.copy());
		
		NetUtil.sendToServer(new WearablesInteractPacket(slot));
		
		return true;
	}
	
	// @Override
	// public boolean mouseReleased(double mouseX, double mouseY, int button) {  }
	
	
	public void update(int mouseX, int mouseY)
	{
		if (this._version != WearablesData.INSTANCE.version) init();
		if (this._data == null) return;
		if (!this.wearCont.allowWearablesPopup()) { hide(); return; }
		
		if (this.isVisible && !isMouseOver(mouseX, mouseY)) hide();
		
		if (!this.isVisible && !screen.hoveredElement(mouseX, mouseY).isPresent())
			for (RegionEntry entry : this.regions)
				if (isOverSlot(entry.pos, mouseX, mouseY))
					{ show(entry.region, entry.pos); break; }
	}
	
	@Override
	public void render(int mouseX, int mouseY, float tickDelta)
	{
		if (!this.wearCont.allowWearablesPopup()) return;
		
		GuiLighting.enableForItems();
		GlStateManager.disableLighting();
		
		drawRegionSlots();
		if (this.isVisible) drawPopup(mouseX, mouseY);
	}
	
	private void drawRegionSlots()
	{
		// Draw empty slots.
		GlStateManager.disableDepthTest();
		REGION_TEX.bind();
		for (RegionEntry entry : this.regions)
			if (entry.region != this.curRegion)
				drawEmptySlot(entry.pos);
		GlStateManager.enableDepthTest();
		
		// Draw slot icons.
		this._client.getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
		for (RegionEntry entry : this.regions)
			if ((entry.region != this.curRegion) && entry.centerSlot.get().isEmpty())
				drawIcon(entry.pos, entry.icon);
		
		// Draw item stacks.
		for (RegionEntry entry : this.regions)
			if (entry.region != this.curRegion)
				drawItemStack(entry.pos, entry.centerSlot.get());
	}
	
	private void drawPopup(int mouseX, int mouseY)
	{
		Position pos = this.pos.subtract(screen.getLeft(), screen.getTop());
		
		// Draw popup border.
		GlStateManager.disableDepthTest();
		REGION_TEX.bind();
		REGION_TEX.drawBordered(pos.x, pos.y, this.width, this.height,
		                        0, 0, Z_LEVEL, BORDER_SIZE, 30, 30, 2, false);
		GlStateManager.enableDepthTest();
		
		// Draw empty slots.
		for (SlotEntry entry : this.slots)
			drawEmptySlot(pos.add(entry.pos));
		
		// Draw slot icons.
		this._client.getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
		for (SlotEntry entry : this.slots)
			if (entry.slot.get().isEmpty())
				drawIcon(pos.add(entry.pos), entry.icon);
		
		// Draw item stacks.
		for (SlotEntry entry : this.slots)
			drawItemStack(pos.add(entry.pos), entry.slot.get());
		
		// Draw highlight.
		Optional<SlotEntry> entry = findSlotAt(mouseX, mouseY);
		if (entry.isPresent()) {
			Position p = pos.add(entry.get().pos);
			GlStateManager.disableLighting();
			GlStateManager.disableDepthTest();
			GlStateManager.colorMask(true, true, true, false);
			fillGradient(p.x + 1, p.y + 1, p.x + SLOT_SIZE - 1, p.y + SLOT_SIZE - 1, 0x80FFFFFF, 0x80FFFFFF);
			GlStateManager.colorMask(true, true, true, true);
			GlStateManager.enableLighting();
			GlStateManager.enableDepthTest();
		}
	}
	
	
	private void drawEmptySlot(Position pos)
		{ REGION_TEX.drawQuad(pos.x, pos.y, SLOT_SIZE, SLOT_SIZE, 6, 32, Z_LEVEL); }
	
	private void drawIcon(Position pos, Identifier icon)
		{ blit(pos.x + 1, pos.y + 1, Z_LEVEL, 16, 16, this._client.getSpriteAtlas().getSprite(icon)); }
	
	private final ItemRenderer _itemRenderer = this._client.getItemRenderer();
	private final TextRenderer _textRenderer = this._client.textRenderer;
	private void drawItemStack(Position pos, ItemStack stack)
	{
		if (stack.isEmpty()) return;
		this._itemRenderer.zOffset = Z_LEVEL - 100.0F;
		this._itemRenderer.renderGuiItem(this._player, stack, pos.x + 1, pos.y + 1);
		this._itemRenderer.renderGuiItemOverlay(this._textRenderer, stack, pos.x + 1, pos.y + 1, null);
		this._itemRenderer.zOffset = 0.0F;
	}
	
	
	public static final class RegionEntry
	{
		public final WearablesRegion region;
		public final Position pos;
		public final IWearablesSlot centerSlot;
		public final Identifier icon;
		
		public RegionEntry(WearablesRegion region, Position pos, IWearablesSlot slot, Identifier icon)
			{ this.region = region; this.pos = pos; this.centerSlot = slot; this.icon = icon; }
	}
	
	public static final class SlotEntry
	{
		public final IWearablesSlot slot;
		public final Position pos;
		public final Identifier icon;
		
		public SlotEntry(IWearablesSlot slot, Position pos, Identifier icon)
			{ this.pos = pos; this.slot = slot; this.icon = icon; }
	}
}
