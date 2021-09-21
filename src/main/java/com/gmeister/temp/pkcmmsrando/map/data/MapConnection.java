package com.gmeister.temp.pkcmmsrando.map.data;

public class MapConnection
{
	
	private Map map;
	private int offset;
	
	public MapConnection(Map map, int offset)
	{
		super();
		this.map = map;
		this.offset = offset;
	}
	
	public Map getMap()
	{ return this.map; }
	
	public void setMap(Map map)
	{ this.map = map; }
	
	public int getOffset()
	{ return this.offset; }
	
	public void setOffset(int offset)
	{ this.offset = offset; }
	
}