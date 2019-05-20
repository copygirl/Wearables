package net.mcft.copy.wearables.client;

import org.lwjgl.opengl.GL11;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.mcft.copy.wearables.WearablesCommon;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class GuiTexture extends Identifier
{
	public final int defaultWidth, defaultHeight;
	public final float scaleX, scaleY;
	
	public GuiTexture(String name, int defaultWidth, int defaultHeight)
	{
		super(WearablesCommon.MOD_ID, "textures/gui/" + name + ".png");
		this.defaultWidth  = defaultWidth;
		this.defaultHeight = defaultHeight;
		this.scaleX = 1.0F / defaultWidth;
		this.scaleY = 1.0F / defaultHeight;
	}
	public GuiTexture(String name, int defaultSize)
		{ this(name, defaultSize, defaultSize); }
	
	
	public void bind()
		{ MinecraftClient.getInstance().getTextureManager().bindTexture(this); }
	
	
	public void drawQuad(int x, int y, int w, int h, int u, int v, float zLevel)
	{
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder vb = tess.getBufferBuilder();
		vb.begin(GL11.GL_QUADS, VertexFormats.POSITION_UV);
		drawQuad(vb, x, y, x+w, y+h, u, v, u+w, v+h, zLevel);
		tess.draw();
	}
	public void drawQuad(int x, int y, int w, int h, int u, int v)
		{ drawQuad(x, y, w, h, u, v, 0); }
	
	public void drawBordered(int x, int y, int w, int h, int u, int v, int zLevel,
	                         int borderLeft, int borderTop, int borderRight, int borderBottom,
	                         int uWidth, int vHeight, int spacing, boolean drawInside)
	{
		int x1 = x + borderLeft;
		int x2 = x + w - borderRight;
		int x3 = x + w;
		
		int y1 = y + borderTop;
		int y2 = y + h - borderBottom;
		int y3 = y + h;
		
		int u1 = u + borderLeft;
		int u2 = u + borderLeft + spacing;
		int u3 = u + uWidth - borderRight - spacing;
		int u4 = u + uWidth - borderRight;
		int u5 = u + uWidth;
		
		int v1 = v + borderTop;
		int v2 = v + borderTop + spacing;
		int v3 = v + vHeight - borderBottom - spacing;
		int v4 = v + vHeight - borderBottom;
		int v5 = v + vHeight;
		
		int iw = w - borderLeft - borderRight;  // Inner width
		int ih = h - borderTop  - borderBottom; // Inner height
		
		int iuw = uWidth  - borderLeft - borderRight;  // Inner texture width
		int ivh = vHeight - borderTop  - borderBottom; // Inner texture height
		
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder vb = tess.getBufferBuilder();
		vb.begin(GL11.GL_QUADS, VertexFormats.POSITION_UV);
		
		// Corners
		drawQuad(vb, x , y , x1, y1, u , v , u1, v1, zLevel); // Top Left
		drawQuad(vb, x2, y , x3, y1, u4, v , u5, v1, zLevel); // Top Right
		drawQuad(vb, x , y2, x1, y3, u , v4, u1, v5, zLevel); // Bottom Left
		drawQuad(vb, x2, y2, x3, y3, u4, v4, u5, v5, zLevel); // Bottom Right
		
		for (int xx = 0; xx < iw; xx += iuw)
		for (int yy = 0; yy < ih; yy += ivh) {
			int xw = Math.min(iw, xx + iuw);
			int yh = Math.min(ih, yy + ivh);
			// Borders
			if (xx == 0) {
				drawQuad(vb, x , y1+yy, x1, y1+yh, u , v2, u1, v3, zLevel); // Left
				drawQuad(vb, x2, y1+yy, x3, y1+yh, u4, v2, u5, v3, zLevel); // Right
			}
			if (yy == 0) {
				drawQuad(vb, x1+xx, y , x1+xw, y1, u2, v , u3, v1, zLevel); // Top
				drawQuad(vb, x1+xx, y2, x1+xw, y3, u2, v4, u3, v5, zLevel); // Bottom
			}
			// Center
			if (drawInside)
				drawQuad(vb, x1+xx, y1+yy, x1+xw, y1+yh, u2, v2, u3, v3, zLevel);
		}
		
		tess.draw();
	}
	public void drawBordered(int x, int y, int w, int h, int u, int v, int zLevel,
	                         int border, int uWidth, int vHeight, int spacing, boolean drawInside)
		{ drawBordered(x, y, w, h, u, v, zLevel, border, border, border, border, uWidth, vHeight, spacing, drawInside); }
	public void drawBordered(int x, int y, int w, int h, int u, int v,
	                         int border, int uWidth, int vHeight, int spacing, boolean drawInside)
		{ drawBordered(x, y, w, h, u, v, 0, border, border, border, border, uWidth, vHeight, spacing, drawInside); }
	
	
	private void drawQuad(BufferBuilder vb, int x0, int y0, int x1, int y1,
	                                        int u0, int v0, int u1, int v1, float zLevel)
	{
		vb.vertex(x0, y1, zLevel).texture(u0 * this.scaleX, v1 * this.scaleY).next();
		vb.vertex(x1, y1, zLevel).texture(u1 * this.scaleX, v1 * this.scaleY).next();
		vb.vertex(x1, y0, zLevel).texture(u1 * this.scaleX, v0 * this.scaleY).next();
		vb.vertex(x0, y0, zLevel).texture(u0 * this.scaleX, v0 * this.scaleY).next();
	}
}
