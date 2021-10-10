package com.gmeister.temp.pkcmmsrando.map.data;

public enum Direction
{
	UP("NORTH", 0, -1),
	DOWN("SOUTH", 0, 1),
	LEFT("WEST", -1, 0),
	RIGHT("EAST", 1, 0);
	
	private String cardinalName;
	private int dx;
	private int dy;
	
	Direction(String cardinalName, int dx, int dy)
	{
		this.cardinalName = cardinalName;
		this.dx = dx;
		this.dy = dy;
	}

	public int getDx()
	{ return this.dx; }

	public int getDy()
	{ return this.dy; }

	public String getCardinalName()
	{ return this.cardinalName; }
}
