package com.gmeister.temp.pkcmmsrando.map.data;

public class Warp
{
	
	private int x;
	private int y;
	private Map map;
	private Warp destination;
	
	public Warp()
	{}
	
	public int getX()
	{ return this.x; }
	
	public void setX(int x)
	{ this.x = x; }
	
	public int getY()
	{ return this.y; }
	
	public void setY(int y)
	{ this.y = y; }
	
	public Map getMap()
	{ return this.map; }
	
	public void setMap(Map map)
	{ this.map = map; }

	public Warp getDestination()
	{ return this.destination; }

	public void setDestination(Warp destination)
	{ this.destination = destination; }
	
}