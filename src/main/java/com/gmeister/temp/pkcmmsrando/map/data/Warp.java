package com.gmeister.temp.pkcmmsrando.map.data;

public class Warp
{
	
	private int x;
	private int y;
	private Map mapTo;
	private int destinationIndex;
	
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
	
	public Map getMapTo()
	{ return this.mapTo; }
	
	public void setMapTo(Map mapTo)
	{ this.mapTo = mapTo; }

	public int getDestinationIndex()
	{ return this.destinationIndex; }

	public void setDestinationIndex(int destinationIndex)
	{ this.destinationIndex = destinationIndex; }
	
}