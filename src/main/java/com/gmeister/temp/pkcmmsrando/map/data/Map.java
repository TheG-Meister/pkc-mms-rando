package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;

import com.gmeister.temp.pkcmmsrando.map.data.MapBlocks.Direction;

public class Map
{
	
	//from data/maps/maps.asm
	//name (data/maps/attributes.asm)
	//tileset (a TILESET_* constant)
	//environment (TOWN, ROUTE, INDOOR, CAVE, ENVIRONMENT_5, GATE, or DUNGEON)
	//location: a LANDMARK_* constant
	//music: a MUSIC_* constant
	//phone service flag: TRUE to prevent phone calls
	//time of day: a PALETTE_* constant
	//fishing group: a FISHGROUP_* constant
	
	private String name;
	private String constName;
	private ArrayList<String> script;
	private int xCapacity;
	private int yCapacity;
	private MapBlocks blocks;
	private ArrayList<Warp> warps;
	private TileSet tileSet;
	private HashMap<Direction, MapConnection> connections;
	
	public Map()
	{
		this.script = new ArrayList<>();
		this.warps = new ArrayList<>();
		this.connections = new HashMap<>();
		for (Direction direction : Direction.values()) connections.put(direction, null);
	}

	public String getName()
	{ return this.name; }

	public void setName(String name)
	{ this.name = name; }

	public String getConstName()
	{ return this.constName; }

	public void setConstName(String constName)
	{ this.constName = constName; }

	public int getXCapacity()
	{ return this.xCapacity; }

	public void setXCapacity(int xCapacity)
	{ this.xCapacity = xCapacity; }

	public int getYCapacity()
	{ return this.yCapacity; }

	public void setYCapacity(int yCapacity)
	{ this.yCapacity = yCapacity; }

	public ArrayList<String> getScript()
	{ return this.script; }

	public void setScript(ArrayList<String> script)
	{ this.script = script; }

	public ArrayList<Warp> getWarps()
	{ return this.warps; }

	public void setWarps(ArrayList<Warp> warps)
	{ this.warps = warps; }

	public MapBlocks getBlocks()
	{ return this.blocks; }

	public void setBlocks(MapBlocks blocks)
	{ this.blocks = blocks; }

	public TileSet getTileSet()
	{ return this.tileSet; }

	public void setTileSet(TileSet tileSet)
	{ this.tileSet = tileSet; }
	
	public HashMap<Direction, MapConnection> getConnections()
	{ return this.connections; }

	public void writeWarpsToScript()
	{
		Pattern warpEventPattern = Pattern.compile("\\twarp_event\\s+");
		
		int count = 0;
		for (int i = 0; i < this.getScript().size(); i++)
		{
			String line = this.getScript().get(i);
			
			if (warpEventPattern.matcher(line).find())
			{
				if (this.getWarps().size() <= count) throw new IllegalStateException();
				Warp warp = this.getWarps().get(count);
				StringBuilder builder = new StringBuilder();
				builder.append("\twarp_event ");
				builder.append(warp.getX()).append(", ");
				builder.append(warp.getY()).append(", ");
				if (warp.getDestination() == null) builder.append(warp.getMap().getConstName()).append(", ").append(-1);
				else
				{
					if (warp.getDestination().getMap() == null) throw new IllegalStateException();
					builder.append(warp.getDestination().getMap().getConstName()).append(", ");
					builder.append(warp.getDestination().getMap().getWarps().indexOf(warp.getDestination()) + 1);
				}
				
				this.getScript().set(i, builder.toString());
				count++;
			}
		}
	}
	
	public boolean[][] beginMovement(int x, int y, ArrayList<Flag> flags)
	{
		boolean[][] tilesToTest = new boolean[this.blocks.getYCapacity() * 2][this.blocks.getXCapacity() * 2];
		tilesToTest[y][x] = true;
		return this.expandMovement(tilesToTest, flags);
	}
	
	public boolean[][] expandMovement(boolean[][] tiles, ArrayList<Flag> flags)
	{
		if (tiles.length != this.blocks.getYCapacity() * 2) throw new IllegalArgumentException("The provded boolean array was not equal in size to this map.");
		if (flags == null) flags = new ArrayList<>();
		
		boolean[][] tilesToTest = new boolean[tiles.length][];
		for (int y = 0; y < tiles.length; y++) tilesToTest[y] = Arrays.copyOf(tiles[y], tiles[y].length);
		
		boolean[][] tilesTested = new boolean[this.blocks.getYCapacity() * 2][this.blocks.getXCapacity() * 2];
		boolean[][] tilesValid = new boolean[this.blocks.getYCapacity() * 2][this.blocks.getXCapacity() * 2];
		
		boolean changed;
		do
		{
			changed = false;
			
			for (int y = 0; y < tilesToTest.length; y++)
			{
				if (tilesToTest[y].length != this.blocks.getXCapacity() * 2) throw new IllegalArgumentException("The provded boolean array was not equal in size to this map.");
				for (int x = 0; x < tilesToTest[y].length; x++) if (!tilesTested[y][x] && tilesToTest[y][x])
				{
					CollisionConstant collision = this.blocks.getCollisionAt(x, y);
					
					for (Direction direction : Direction.values())
					{
						if (collision.getPermissionsForStep(direction, false).getName().toUpperCase().equals("HOP"))
						{
							int nextX = x + direction.getDx() * 2;
							int nextY = y + direction.getDy() * 2;
							if (this.blocks.containsCollisionAt(nextX, nextY))
							{
								
							}
						}
						int nextX = x + direction.getDx();
						int nextY = y + direction.getDy();
						if (this.blocks.containsCollisionAt(nextX, nextY))
						{
							if (this.blocks.getCollisionAt(x, y).canMoveTo(this.blocks.getCollisionAt(nextX, nextY), direction, flags))
							{
								tilesToTest[nextY][nextX] = true;
								tilesValid[nextY][nextX] = true;
								changed = true;
							}
						}
					}
					
					tilesToTest[y][x] = false;
					tilesTested[y][x] = true;
				}
			}
		}
		while (changed);
		
		return tilesValid;
	}
	
}