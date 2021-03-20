package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.ArrayList;

public class BlockSet
{
	
	private String name;
	private ArrayList<Block> blocks;
	
	public BlockSet()
	{
		this.blocks = new ArrayList<>();
	}

	public ArrayList<Block> getBlocks()
	{ return this.blocks; }

	public String getName()
	{ return this.name; }

	public void setName(String name)
	{ this.name = name; }
	
}