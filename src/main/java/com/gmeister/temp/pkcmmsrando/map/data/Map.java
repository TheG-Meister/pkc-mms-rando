package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.Arrays;

import com.gmeister.temp.maps.MapOutOfBoundsException;

public class Map
{
	
	//from data/maps/maps.asm
	//name (data/maps/attributes.asm)
	//tileset (a TILESET_* constant)
	//environment (TOWN, ROUTE, INDOOR, CAVE, ENVIRONMENT_5, GATE, or DUNGEON)
	//location: a LANDMARK_* constant
	//music: a MUSIC_* constant
	//phone service flag: TRUE to prevent phone calls
	//time of day: a PALETTE_* constant
	//fishing group: a FISHGROUP_* constant
	
	private int xCapacity;
	private int yCapacity;
	private Block[][] blocks;
	
	public Map(int xCapacity, int yCapacity)
	{
		this.xCapacity = xCapacity;
		this.yCapacity = yCapacity;
		this.blocks = new Block[yCapacity][xCapacity];
	}
	
	public int getXCapacity()
	{ return this.xCapacity; }
	
	public int getYCapacity()
	{ return this.yCapacity; }
	
	public void fill(Block b)
	{ for (int y = 0; y < this.yCapacity; y++) Arrays.fill(this.blocks[y], b); }
	
	public Block[][] getBlocks()
	{ return this.blocks; }
	
	public Block getAt(int x, int y)
	{
		if (this.isWithinBlocksAt(x, y)) return this.blocks[y][x];
		else throw new MapOutOfBoundsException("Map does not contain coordinates " + x + ", " + y);
	}
	
	public boolean isWithinBlocksAt(int x, int y)
	{
		return !(x < 0 || x >= this.xCapacity || y < 0 || y >= this.yCapacity);
	}
	
	public void setAt(int x, int y, Block b)
	{
		if (this.isWithinBlocksAt(x, y)) this.blocks[y][x] = b;
		else throw new MapOutOfBoundsException("BooleanMap does not contain coordinates " + x + ", " + y);
	}
	
}