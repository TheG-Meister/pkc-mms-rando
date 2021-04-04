package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.ArrayList;

public class BlockSet
{

	private String name;
	private ArrayList<Block> blocks;
	private ArrayList<ArrayList<Block>> collGroups;

	public BlockSet()
	{
		this.blocks = new ArrayList<>();
		this.collGroups = new ArrayList<>();
	}

	/**
	 * Organises the blocks in this blockset by their collision constants.
	 *
	 * @return A List of Lists, each having a selection of blocks that all have the
	 *         same collision constants at each coordinate
	 */
	public ArrayList<ArrayList<Block>> updateCollGroups()
	{
		this.collGroups.clear();

		for (Block block : this.getBlocks())
		{
			ArrayList<Block> collGroup = null;
			for (ArrayList<Block> group : this.collGroups)
			{
				boolean same = true;
				Block tester = group.get(0);
				blockTesting:
				for (int y = 0; y < 2; y++) for (int x = 0; x < 2; x++)
					if (tester.getCollision()[y][x].getValue() != block.getCollision()[y][x].getValue())
				{
					same = false;
					break blockTesting;
				}
				if (same)
				{
					collGroup = group;
					break;
				}
			}

			if (collGroup == null)
			{
				collGroup = new ArrayList<>();
				this.collGroups.add(collGroup);
			}
			collGroup.add(block);
		}
		
		return this.collGroups;
	}

	public ArrayList<Block> getBlocks()
	{ return this.blocks; }

	public void setBlocks(ArrayList<Block> blocks)
	{ this.blocks = blocks; }

	public String getName()
	{ return this.name; }

	public void setName(String name)
	{ this.name = name; }
	
	public ArrayList<ArrayList<Block>> getCollGroups()
	{ return this.collGroups; }

}