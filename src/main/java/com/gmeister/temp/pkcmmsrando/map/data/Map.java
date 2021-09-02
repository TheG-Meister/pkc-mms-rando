package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import com.gmeister.temp.pkcmmsrando.map.data.MapBlocks.Direction;
import com.gmeister.temp.pkcmmsrando.map.data.MapConnection.Cardinal;

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
	private HashMap<Cardinal, MapConnection> connections;
	
	public Map()
	{
		this.script = new ArrayList<>();
		this.warps = new ArrayList<>();
		this.connections = new HashMap<>();
		for (Cardinal cardinal : Cardinal.values()) connections.put(cardinal, null);
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
	
	public HashMap<Cardinal, MapConnection> getConnections()
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
	
	public boolean testMovement(int x1, int y1, int x2, int y2, ArrayList<Flag> flags)
	{
		if (flags == null) flags = new ArrayList<>();
		
		boolean[][] tilesToTest = new boolean[this.blocks.getYCapacity() * 2][this.blocks.getXCapacity() * 2];
		boolean[][] tilesTested = new boolean[this.blocks.getYCapacity() * 2][this.blocks.getXCapacity() * 2];
		boolean[][] tilesValid = new boolean[this.blocks.getYCapacity() * 2][this.blocks.getXCapacity() * 2];
		tilesToTest[y1][x1] = true;
		
		boolean changed;
		do
		{
			changed = false;
			
			for (int y = 0; y < tilesToTest.length; y++) for (int x = 0; x < tilesToTest[y].length; x++) if (!tilesTested[y][x] && tilesToTest[y][x])
			{
				CollisionConstant collision = this.blocks.getCollisionAt(x, y);
				
				for (Direction direction : Direction.values())
				{
					CollisionPermission perm = collision.getPermissionsForStep(direction, false);
					if (perm.isAllowed() && flags.containsAll(perm.getFlags()))
					{
						int nextX = x + direction.getDx();
						int nextY = y + direction.getDy();
						if (this.blocks.containsCollisionAt(nextX, nextY))
						{
							CollisionPermission nextPerm = this.blocks.getCollisionAt(nextX, nextY).getPermissionsForStep(direction, true);
							if (nextPerm.isAllowed() && flags.containsAll(nextPerm.getFlags()))
							{
								tilesToTest[nextY][nextX] = true;
								tilesValid[nextY][nextX] = true;
								changed = true;
							}
						}
					}
				}
				
				tilesToTest[y][x] = false;
				tilesTested[y][x] = true;
			}
		}
		while (changed && !tilesValid[y2][x2]);
		
		return tilesValid[y2][x2];
	}
	
	//public static ArrayList<ArrayList<Map>> 
	
	public static boolean testMovement(Map map1, int x1, int y1, Map map2, int x2, int y2, ArrayList<Flag> flags)
	{
		
		
		return false;
	}
	
}