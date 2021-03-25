package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.ArrayList;

public class TileSet
{
	
	private String name;
	private ArrayList<Tile> tiles;
	
	public TileSet(String name)
	{
		this.name = name;
		this.tiles = new ArrayList<>();
	}
	
	public String getName()
	{ return this.name; }
	
	public void setName(String name)
	{ this.name = name; }
	
	public ArrayList<Tile> getTiles()
	{ return this.tiles; }
	
}