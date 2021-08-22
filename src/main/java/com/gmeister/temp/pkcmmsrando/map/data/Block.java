package com.gmeister.temp.pkcmmsrando.map.data;

public class Block
{
	
	private String name;
	private CollisionConstant[][] collision;
	private Tile[][] tiles;
	private boolean building;
	
	public Block()
	{
		this.collision = new CollisionConstant[2][2];
		this.tiles = new Tile[4][4];
	}
	
	public CollisionConstant[][] getCollision()
	{ return this.collision; }
	
	public String getName()
	{ return this.name; }
	
	public void setName(String name)
	{ this.name = name; }
	
	public boolean isBuilding()
	{ return this.building; }
	
	public void setBuilding(boolean building)
	{ this.building = building; }
	
	public Tile[][] getTiles()
	{ return this.tiles; }
	
}