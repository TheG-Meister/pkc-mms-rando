package com.gmeister.temp.pkcmmsrando.map.data;

public class Block
{
	
	public static final int COLLISION_WIDTH = 2;
	public static final int TILE_WIDTH = 4;
	
	private String name;
	private CollisionConstant[][] collision;
	private Tile[][] tiles;
	private boolean building;
	
	public Block()
	{
		this.collision = new CollisionConstant[Block.COLLISION_WIDTH][Block.COLLISION_WIDTH];
		this.tiles = new Tile[Block.TILE_WIDTH][Block.TILE_WIDTH];
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