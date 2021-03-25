package com.gmeister.temp.pkcmmsrando.map.data;

public class Tile
{
	
	private String name;
	//palette
	//pixels
	private boolean building;
	
	public Tile()
	{}
	
	public Tile(String name, boolean building)
	{
		this.name = name;
		this.building = building;
	}
	
	public String getName()
	{ return this.name; }
	
	public void setName(String name)
	{ this.name = name; }
	
	public boolean getBuilding()
	{ return this.building; }
	
	public void setBuilding(boolean building)
	{ this.building = building; }
	
}
