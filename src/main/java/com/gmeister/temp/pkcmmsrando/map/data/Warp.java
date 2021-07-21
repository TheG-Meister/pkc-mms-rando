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
	
	public boolean hasAccessibleDestination()
	{
		//The destination is not accessible if it is null
		if (this.destination == null) return false;
		
		//The destination is not accessible if this warp is not on a map tile with a warp collision value   
		int collision = this.map.getBlocks().getCollisionAt(this.x, this.y).getValue();
		if (((collision & 0xf0) == 0x70) || collision == 0x60 || collision == 0x68) return true;
		else return false;
	}
	
}