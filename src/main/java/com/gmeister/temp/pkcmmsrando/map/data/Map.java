package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.ArrayList;

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
	
	private String name;
	private ArrayList<String> script;
	private int xCapacity;
	private int yCapacity;
	private MapBlocks blocks;
	private ArrayList<Warp> warps;
	private TileSet tileSet;
	
	public Map()
	{
		this.script = new ArrayList<>();
		this.warps = new ArrayList<>();
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

	public ArrayList<String> getScript()
	{ return this.script; }

	public void setScript(ArrayList<String> script)
	{ this.script = script; }

	public ArrayList<Warp> getWarps()
	{ return this.warps; }

	public void setWarps(ArrayList<Warp> warps)
	{ this.warps = warps; }

	public MapBlocks getBlocks()
	{ return this.blocks; }

	public void setBlocks(MapBlocks blocks)
	{ this.blocks = blocks; }

	public TileSet getTileSet()
	{ return this.tileSet; }

	public void setTileSet(TileSet tileSet)
	{ this.tileSet = tileSet; }
	
}