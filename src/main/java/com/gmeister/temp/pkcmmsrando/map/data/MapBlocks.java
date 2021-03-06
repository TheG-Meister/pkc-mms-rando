package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.Arrays;

import com.gmeister.temp.maps.MapOutOfBoundsException;

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