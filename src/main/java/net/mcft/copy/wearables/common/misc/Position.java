package net.mcft.copy.wearables.common.misc;

public final class Position
{
	public final int x, y;
	
	public Position(int x, int y)
		{ this.x = x; this.y = y; }
	
	
	public Position add(Position other)
		{ return add(other.x, other.y); }
	public Position add(int x, int y)
		{ return new Position(this.x + x, this.y + y); }
	
	public Position subtract(Position other)
		{ return subtract(other.x, other.y); }
	public Position subtract(int x, int y)
		{ return new Position(this.x - x, this.y - y); }
	
	
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Position)) return false;
		Position pos = (Position)obj;
		return (pos.x == this.x) && (pos.y == this.y);
	}
	
	@Override
	public int hashCode()
		{ return this.x ^ this.y << 8; }
	
	@Override
	public String toString()
		{ return "[" + this.x + ":" + this.y + "]"; }
}
