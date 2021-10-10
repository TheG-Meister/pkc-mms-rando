package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.Arrays;

public class MapBlocks
{
	
	private String name;
	private int xCapacity;
	private int yCapacity;
	private Block[] blocks;
	
	public void fill(Block b)
	{ Arrays.fill(this.blocks, b); }
	
	public Block getAt(int x, int y)
	{
		if (this.containsBlockAt(x, y)) return this.blocks[(y * this.xCapacity) + x];
		else throw new ArrayIndexOutOfBoundsException("Map does not contain coordinates " + x + ", " + y);
	}
	
	public boolean containsBlockAt(int x, int y)
	{ return !(x < 0 || x >= this.xCapacity || y < 0 || y >= this.yCapacity); }
	
	public void setAt(int x, int y, Block b)
	{
		if (this.containsBlockAt(x, y)) this.blocks[(y * this.xCapacity) + x] = b;
		else throw new ArrayIndexOutOfBoundsException("BooleanMap does not contain coordinates " + x + ", " + y);
	}
	
	public CollisionConstant getCollisionAt(int x, int y)
	{
		int blockX = Math.floorDiv(x, Block.COLLISION_WIDTH);
		int blockY = Math.floorDiv(y, Block.COLLISION_WIDTH);
		int collisionX = x % Block.COLLISION_WIDTH;
		int collisionY = y % Block.COLLISION_WIDTH;
		
		if (this.containsBlockAt(blockX, blockY)) return this.blocks[(blockY * this.xCapacity) + blockX].getCollision()[collisionY][collisionX];
		else throw new ArrayIndexOutOfBoundsException("Map does not contain coordinates " + x + ", " + y);
	}
	
	public boolean containsCollisionAt(int x, int y)
	{
		int blockX = Math.floorDiv(x, Block.COLLISION_WIDTH);
		int blockY = Math.floorDiv(y, Block.COLLISION_WIDTH);
		return !(blockX < 0 || blockX >= this.xCapacity || blockY < 0 || blockY >= this.yCapacity);
	}
	
	public int getCollisionXCapacity()
	{ return this.xCapacity * Block.COLLISION_WIDTH; }
	
	public int getCollisionYCapacity()
	{ return this.yCapacity * Block.COLLISION_WIDTH; }
	
	public Tile getTileAt(int x, int y)
	{
		int blockX = Math.floorDiv(x, Block.TILE_WIDTH);
		int blockY = Math.floorDiv(y, Block.TILE_WIDTH);
		int tileX = x % Block.TILE_WIDTH;
		int tileY = y % Block.TILE_WIDTH;
		
		if (this.containsBlockAt(blockX, blockY)) return this.blocks[(blockY * this.xCapacity) + blockX].getTiles()[tileY][tileX];
		else throw new ArrayIndexOutOfBoundsException("Map does not contain coordinates " + x + ", " + y);
	}
	
	public boolean containsTileAt(int x, int y)
	{
		int blockX = Math.floorDiv(x, Block.TILE_WIDTH);
		int blockY = Math.floorDiv(y, Block.TILE_WIDTH);
		return !(blockX < 0 || blockX >= this.xCapacity || blockY < 0 || blockY >= this.yCapacity);
	}
	
	public int getTileXCapacity()
	{ return this.xCapacity * Block.TILE_WIDTH; }
	
	public int getTileYCapacity()
	{ return this.yCapacity * Block.TILE_WIDTH; }

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