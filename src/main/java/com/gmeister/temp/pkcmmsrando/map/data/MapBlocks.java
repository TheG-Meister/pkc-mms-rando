package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.Arrays;

import com.gmeister.temp.maps.MapOutOfBoundsException;

public class MapBlocks
{
	
	public static enum Direction
	{
		UP(0, -1),
		DOWN(0, 1),
		LEFT(-1, 0),
		RIGHT(1, 0);
		
		private int dx;
		private int dy;
		
		Direction(int dx, int dy)
		{
			this.dx = dx;
			this.dy = dy;
		}

		public int getDx()
		{ return this.dx; }

		public int getDy()
		{ return this.dy; }
	}
	
	private String name;
	private int xCapacity;
	private int yCapacity;
	private Block[] blocks;
	
	public void fill(Block b)
	{ Arrays.fill(this.blocks, b); }
	
	public Block getAt(int x, int y)
	{
		if (this.isWithinBlocksAt(x, y)) return this.blocks[(y * this.xCapacity) + x];
		else throw new MapOutOfBoundsException("Map does not contain coordinates " + x + ", " + y);
	}
	
	public boolean isWithinBlocksAt(int x, int y)
	{ return !(x < 0 || x >= this.xCapacity || y < 0 || y >= this.yCapacity); }
	
	public void setAt(int x, int y, Block b)
	{
		if (this.isWithinBlocksAt(x, y)) this.blocks[(y * this.xCapacity) + x] = b;
		else throw new MapOutOfBoundsException("BooleanMap does not contain coordinates " + x + ", " + y);
	}
	
	public Constant getCollisionAt(int x, int y)
	{
		int blockX = Math.floorDiv(x, 2);
		int blockY = Math.floorDiv(y, 2);
		int collisionX = x % 2;
		int collisionY = y % 2;
		
		if (this.isWithinBlocksAt(blockX, blockY)) return this.blocks[(blockY * this.xCapacity) + blockX].getCollision()[collisionY][collisionX];
		else throw new MapOutOfBoundsException("Map does not contain coordinates " + x + ", " + y);
	}
	
	public Tile getTileAt(int x, int y)
	{
		int blockX = Math.floorDiv(x, 4);
		int blockY = Math.floorDiv(y, 4);
		int tileX = x % 4;
		int tileY = y % 4;
		
		if (this.isWithinBlocksAt(blockX, blockY)) return this.blocks[(blockY * this.xCapacity) + blockX].getTiles()[tileY][tileX];
		else throw new MapOutOfBoundsException("Map does not contain coordinates " + x + ", " + y);
	}
	
	public int moveFrom(int x, int y, Direction direction)
	{
		
		return 0;
	}

	public String getName()
	{ return this.name; }

	public void setName(String name)
	{ this.name = name; }

	public int getXCapacity()
	{ return this.xCapacity; }

	public void setXCapacity(int xCapacity)
	{ this.xCapacity = xCapacity; }

	public int getYCapacity()
	{ return this.yCapacity; }

	public void setYCapacity(int yCapacity)
	{ this.yCapacity = yCapacity; }

	public Block[] getBlocks()
	{ return this.blocks; }

	public void setBlocks(Block[] blocks)
	{ this.blocks = blocks; }
	
}