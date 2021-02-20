package com.gmeister.temp.pkcmmsrando.map.data;

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
	
	public Block[][] getBlocks()
	{ return this.blocks; }
	
}