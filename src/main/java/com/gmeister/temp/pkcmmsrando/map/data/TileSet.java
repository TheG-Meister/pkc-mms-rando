package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.ArrayList;

public class TileSet
{
	
	private String name;
	private ArrayList<Tile> tiles;
	private BlockSet blockSet;
	
	public TileSet(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{ return this.name; }
	
	public void setName(String name)
	{ this.name = name; }
	
	public ArrayList<Tile> getTiles()
	{ return this.tiles; }

	public void setTiles(ArrayList<Tile> tiles)
	{ this.tiles = tiles; }

	public BlockSet getBlockSet()
	{ return this.blockSet; }

	public void setBlockSet(BlockSet blockSet)
	{ this.blockSet = blockSet; }
	
}