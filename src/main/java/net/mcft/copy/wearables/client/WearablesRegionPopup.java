package net.mcft.copy.wearables.client;

import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import com.mojang.blaze3d.platform.GlStateManager;

import org.lwjgl.opengl.GL11;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.mcft.copy.wearables.api.IWearablesContainerId;
import net.mcft.copy.wearables.api.IWearablesEntity;
import net.mcft.copy.wearables.api.IWearablesSlot;
import net.mcft.copy.wearables.api.WearablesContainerSlot;
import net.mcft.copy.wearables.api.WearablesSlotType;
import net.mcft.copy.wearables.api.IWearablesContainer.RegionEntry;
import net.mcft.copy.wearables.client.mixin.IContainerScreenAccessor;
import net.mcft.copy.wearables.common.WearablesContainerData;
import net.mcft.copy.wearables.common.data.EntityTypeData;
import net.mcft.copy.wearables.common.data.WearablesData;
import net.mcft.copy.wearables.common.impl.slot.DefaultSlotHandler;
import net.mcft.copy.wearables.common.misc.Position;

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
import net.minecraft.container.Slot;
import net.minecraft.entity.Entity;
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
	public static final int Z_LEVEL     = 300;
	
	
	private final MinecraftClient _client    = MinecraftClient.getInstance();
	private final ClientPlayerEntity _player = this._client.player;
	
	private int _version;
	
	public final AbstractContainerScreen<?> screen;
	public final IContainerScreenAccessor<?> accessor;
	public final IWearablesContainerId containerId;
	public final WearablesContainerData data;
	
	public boolean isVisible = false;
	public RegionEntry current;
	public Position pos;
	public int width, height;
	
	
	public WearablesRegionPopup(AbstractContainerScreen<?> screen)
	{
		if (screen == null) throw new IllegalArgumentException("screen is null");
		if (!(screen.getContainer() instanceof IWearablesContainerId)) throw new IllegalArgumentException(
			"'" + screen.getContainer() + "' isn't a IWearablesContainer");
		
		this._version = WearablesData.INSTANCE.version;
		
		this.screen      = screen;
		this.accessor    = (IContainerScreenAccessor<?>)screen;
		this.containerId = (IWearablesContainerId)screen.getContainer();
		this.data        = WearablesContainerData.from(screen.getContainer());
		
		hide();
	}
	
	public void show(RegionEntry entry)
	{
		this.isVisible = true;
		this.current   = entry;
		
		IWearablesEntity wearablesEntity = IWearablesEntity.from(entry.entity);
		SortedSet<IWearablesSlot> slots = new TreeSet<>();
		wearablesEntity.getSupportedWearablesSlots(entry.region).forEach(slots::add);
		wearablesEntity.getEquippedWearables(entry.region).forEach(slots::add);
		// TODO: Optionally filter for which slots are currently relevant.
		
		int centerIndex = entry.slots.indexOf(entry.centerSlot);
		// Show all the slots.
		for (int i = 0; i < entry.slots.size(); i++) {
			Slot slot = entry.slots.get(i);
			if (slot == entry.centerSlot) continue;
			slot.xPosition = entry.centerSlot.xPosition + (i - centerIndex) * SLOT_SIZE;
			slot.yPosition = entry.centerSlot.yPosition;
		}
		
		Slot firstSlot = entry.slots.get(0);
		Slot lastSlot  = entry.slots.get(entry.slots.size() - 1);
		this.pos = new Position(firstSlot.xPosition - BORDER_SIZE - 1,
		                        firstSlot.yPosition - BORDER_SIZE - 1);
		this.width  = lastSlot.xPosition - firstSlot.xPosition + SLOT_SIZE + BORDER_SIZE * 2;
		this.height = lastSlot.yPosition - firstSlot.yPosition + SLOT_SIZE + BORDER_SIZE * 2;
	}
	
	private Identifier getIcon(Entity entity, WearablesSlotType slotType)
	{
		return Optional.ofNullable(EntityTypeData.from(entity).slotTypes.get(slotType))
		               .map(slotTypeData -> slotTypeData.handler)
		               .orElse(DefaultSlotHandler.INSTANCE)
		               .getIcon(slotType);
	}
	
	public void hide()
	{
		if (!this.isVisible) return;
		
		if (this.current != null) {
			// Re-hide all the slots.
			for (Slot slot : this.current.slots) {
				if (slot == this.current.centerSlot) continue;
				slot.xPosition = -10000;
				slot.yPosition = -10000;
			}
		}
		
		this.isVisible = false;
		this.current   = null;
		this.pos    = null;
		this.width  = 0;
		this.height = 0;
	}
	
	
	@Override
	public boolean isMouseOver(double pointX, double pointY)
	{
		return this.isVisible && isWithinScreenSpace(
			this.pos.x, this.pos.y, this.width, this.height, pointX, pointY);
	}
	
	public boolean isMouseOverBorder(double pointX, double pointY)
	{
		return this.isVisible
		    && isWithinScreenSpace(this.pos.x, this.pos.y, this.width, this.height, pointX, pointY)
		    && !isWithinScreenSpace(this.pos.x + BORDER_SIZE, this.pos.y + BORDER_SIZE,
		                            this.width - BORDER_SIZE, this.height - BORDER_SIZE, pointX, pointY);
	}
	
	private boolean isOverSlot(Position slotPos, double pointX, double pointY)
		{ return isWithinScreenSpace(slotPos.x, slotPos.y, SLOT_SIZE, SLOT_SIZE, pointX, pointY); }
	
	private boolean isWithinScreenSpace(int x, int y, int width, int height, double pointX, double pointY)
		{ return isWithinGlobalSpace(x + accessor.getLeft(), y + accessor.getTop(), width, height, pointX, pointY); }
	
	private boolean isWithinGlobalSpace(int x, int y, int width, int height, double pointX, double pointY)
		{ return (pointX >= x) && (pointX < x + width) && (pointY >= y) && (pointY < y + height); }
	
	
	// @Override
	// public boolean mouseClicked(double mouseX, double mouseY, int button) {  }
	
	// @Override
	// public boolean mouseReleased(double mouseX, double mouseY, int button) {  }
	
	
	public void update(int mouseX, int mouseY)
	{
		if (this._version != WearablesData.INSTANCE.version) {
			this._version = WearablesData.INSTANCE.version;
			hide();
		}
		
		if (this.isVisible && !isMouseOver(mouseX, mouseY)) hide();
		
		if (!this.isVisible && !accessor.hoveredElement(mouseX, mouseY).isPresent())
			for (RegionEntry entry : this.data.getRegions())
				if (isOverSlot(entry.position, mouseX, mouseY))
					{ show(entry); break; }
	}
	
	@Override
	public void render(int mouseX, int mouseY, float tickDelta)
	{
		GuiLighting.enableForItems();
		GlStateManager.disableLighting();
		
		drawRegionSlots();
		if (this.isVisible) drawPopup(mouseX, mouseY);
	}
	
	private void drawRegionSlots()
	{
		// Draw empty slots.
		REGION_TEX.bind();
		for (RegionEntry entry : this.data.getRegions())
			if (entry != this.current)
				drawEmptySlot(entry.position);
		
		// Draw slot icons.
		this._client.getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
		for (RegionEntry entry : this.data.getRegions())
			if ((entry != this.current) && entry.centerSlot.getStack().isEmpty())
				drawIcon(entry.position, getIcon(entry.entity, entry.centerSlot.wearablesSlot.getSlotType()));
		
		// Draw item stacks.
		for (RegionEntry entry : this.data.getRegions())
			if (entry != this.current)
				drawItemStack(entry.position, entry.centerSlot.getStack());
	}
	
	private void drawPopup(int mouseX, int mouseY)
	{
		// Draw popup border.
		GlStateManager.depthFunc(GL11.GL_ALWAYS);
		REGION_TEX.bind();
		REGION_TEX.drawBordered(this.pos.x, this.pos.y, this.width, this.height,
		                        0, 0, Z_LEVEL, BORDER_SIZE, 30, 30, 2, false);
		GlStateManager.depthFunc(GL11.GL_LEQUAL);
		
		// Draw empty slots.
		for (Slot slot : this.current.slots)
			drawEmptySlot(new Position(slot.xPosition, slot.yPosition));
		
		// Draw slot icons.
		this._client.getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
		for (WearablesContainerSlot slot : this.current.slots)
			if (slot.getStack().isEmpty())
				drawIcon(new Position(slot.xPosition, slot.yPosition),
				         getIcon(this.current.entity, slot.wearablesSlot.getSlotType()));
		
		// Draw item stacks.
		for (Slot slot : this.current.slots)
			drawItemStack(new Position(slot.xPosition, slot.yPosition), slot.getStack());
		
		// Draw highlight.
		Slot slot = this.accessor.getFocusedSlot();
		if (slot != null) {
			int x = slot.xPosition;
			int y = slot.yPosition;
			GlStateManager.disableLighting();
			GlStateManager.disableDepthTest();
			GlStateManager.colorMask(true, true, true, false);
			fillGradient(x, y, x + SLOT_SIZE - 2, y + SLOT_SIZE - 2, 0x80FFFFFF, 0x80FFFFFF);
			GlStateManager.colorMask(true, true, true, true);
			GlStateManager.enableLighting();
			GlStateManager.enableDepthTest();
		}
	}
	
	
	private void drawEmptySlot(Position pos)
		{ REGION_TEX.drawQuad(pos.x - 1, pos.y - 1, SLOT_SIZE, SLOT_SIZE, 6, 32, Z_LEVEL); }
	
	private void drawIcon(Position pos, Identifier icon)
		{ blit(pos.x, pos.y, Z_LEVEL, 16, 16, this._client.getSpriteAtlas().getSprite(icon)); }
	
	private final ItemRenderer _itemRenderer = this._client.getItemRenderer();
	private final TextRenderer _textRenderer = this._client.textRenderer;
	private void drawItemStack(Position pos, ItemStack stack)
	{
		if (stack.isEmpty()) return;
		this._itemRenderer.zOffset = Z_LEVEL - 100.0F;
		this._itemRenderer.renderGuiItem(this._player, stack, pos.x, pos.y);
		this._itemRenderer.renderGuiItemOverlay(this._textRenderer, stack, pos.x, pos.y, null);
		this._itemRenderer.zOffset = 0.0F;
	}
}
