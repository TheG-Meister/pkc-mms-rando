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
	
	public boolean isAdjacentTo(Warp warp)
	{
		if (this.equals(warp)) return true;
		else if (this.getMap() == null || warp.getMap() == null) return false;
		else if (!this.getMap().equals(warp.getMap())) return false;
		else if (Math.abs(this.getX() - warp.getX()) + Math.abs(this.getY() - warp.getY()) > 1) return false;
		else return true;
	}
	
	/**
	 * Returns whether this warp is paired with the provided warp.<br>
	 * <br>
	 * Warps are classed as paired if they are on a shared map, adjacent and lead to either the same destination or adjacent destinations on a shared map
	 * @param warp
	 * @return
	 */
	public boolean isPairedWith(Warp warp)
	{
		if (!this.isAdjacentTo(warp)) return false;
		else if (this.getDestination() == null ^ warp.getDestination() == null) return false;
		else if (this.getDestination() == null && warp.getDestination() == null) return true;
		else if (!this.getDestination().isAdjacentTo(warp.getDestination())) return false;
		else return true;
	}
	
}