package com.gmeister.temp.pkcmmsrando.map.data;

public class Block
{
	
	private String name;
	private Constant[][] collision;
	private Tile[][] tiles;
	private boolean building;
	
	public Block()
	{
		this.collision = new Constant[2][2];
		this.tiles = new Tile[4][4];
	}
	
	public Constant[][] getCollision()
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

	public static Block[] makeSampleBlockset()
	{
		Block[] blocks = new Block[16];
		for (byte i = 0; i < blocks.length; i++)
		{
			blocks[i] = new Block();
			for (int y = 0, j = 0; y < 2; y++) for (int x = 0; x < 2; x++, j++) blocks[i].getCollision()[y][x] = new Constant("Collision", (byte) ((i >>> j) & 0b1));
		}
		return blocks;
	}
	
}