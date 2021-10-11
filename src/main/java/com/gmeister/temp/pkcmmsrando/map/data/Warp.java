package com.gmeister.temp.pkcmmsrando.map.data;

public class Warp
{
	
	private OverworldPosition position;
	private Warp destination;
	
	public Warp()
	{}
	
	public OverworldPosition getPosition()
	{ return this.position; }

	public void setPosition(OverworldPosition position)
	{ this.position = position; }

	public int getX()
	{ return this.position.getX(); }
	
	public int getY()
	{ return this.position.getY(); }
	
	public Map getMap()
	{ return this.position.getMap(); }
	
	public Warp getDestination()
	{ return this.destination; }
	
	public void setDestination(Warp destination)
	{ this.destination = destination; }
	
	public boolean hasAccessibleDestination()
	{
		//The destination is not accessible if it is null
		if (this.destination == null) return false;
		
		//The destination is not accessible if this warp is not on a map tile with a warp collision value
		int collision = this.getMap().getBlocks().getCollisionAt(this.getX(), this.getY()).getValue();
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

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.destination == null) ? 0 : this.destination.hashCode());
		result = prime * result + ((this.position == null) ? 0 : this.position.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Warp other = (Warp) obj;
		if (this.destination == null)
		{
			if (other.destination != null) return false;
		}
		else if (other.destination != null)
		{
			if (this.destination.position == null)
			{
				if (other.destination.position != null) return false;
			}
			else if (!this.destination.position.equals(other.destination.position)) return false;
		}
		if (this.position == null)
		{
			if (other.position != null) return false;
		}
		else if (!this.position.equals(other.position)) return false;
		return true;
	}

	@Override
	public String toString()
	{ return "Warp [from=" + this.position + ", to=" + this.destination.position + "]"; }
	
}