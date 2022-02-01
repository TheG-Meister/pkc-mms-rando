package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

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
	private List<ObjectEvent> objectEvents;
	
	public Map()
	{
		this.script = new ArrayList<>();
		this.warps = new ArrayList<>();
		this.connections = new HashMap<>();
		for (Direction direction : Direction.values()) this.connections.put(direction, null);
		this.objectEvents = new ArrayList<>();
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
	
	public List<ObjectEvent> getObjectEvents()
	{ return this.objectEvents; }

	public void setObjectEvents(List<ObjectEvent> objectEvents)
	{ this.objectEvents = objectEvents; }

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
	
	public Warp findWarpAt(int x, int y)
	{
		for (Warp warp : this.getWarps()) if (warp.getX() == x && warp.getY() == y) return warp;
		return null;
	}
	
	public ArrayList<Map> getConnectingMaps()
	{
		ArrayList<Map> maps = new ArrayList<>();
		for (MapConnection connection : this.connections.values()) if (connection != null)
		{
			Map connectionMap = connection.getMap();
			if (connectionMap != null && !this.equals(connectionMap) && !maps.contains(connectionMap))
				maps.add(connectionMap);
		}
		
		for (Warp warp : this.warps) if (warp != null && warp.getDestination() != null)
		{
			Map destinationMap = warp.getDestination().getMap();
			if (destinationMap != null && !this.equals(destinationMap) && !maps.contains(destinationMap))
				maps.add(destinationMap);
		}
		
		return maps;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.constName == null) ? 0 : this.constName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Map other = (Map) obj;
		if (this.constName == null)
		{
			if (other.constName != null) return false;
		}
		else if (!this.constName.equals(other.constName)) return false;
		return true;
	}

	@Override
	public String toString()
	{ return this.constName; }
	
}