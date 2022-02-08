package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.ArrayList;
import java.util.List;

public class Disassembly
{
	private ArrayList<CollisionConstant> collisionConstants;
	private ArrayList<Flag> engineFlags;
	private ArrayList<Flag> eventFlags;
	private ArrayList<TileSet> tileSets;
	private ArrayList<Map> maps;
	private List<Constant> mapDataConstants;
	
	public Disassembly()
	{}
	
	public ArrayList<CollisionConstant> getCollisionConstants()
	{ return this.collisionConstants; }
	
	public void setCollisionConstants(ArrayList<CollisionConstant> collisionConstants)
	{ this.collisionConstants = collisionConstants; }
	
	public ArrayList<Flag> getEngineFlags()
	{ return this.engineFlags; }
	
	public void setEngineFlags(ArrayList<Flag> engineFlags)
	{ this.engineFlags = engineFlags; }
	
	public ArrayList<Flag> getEventFlags()
	{ return this.eventFlags; }
	
	public void setEventFlags(ArrayList<Flag> eventFlags)
	{ this.eventFlags = eventFlags; }
	
	public ArrayList<TileSet> getTileSets()
	{ return this.tileSets; }
	
	public void setTileSets(ArrayList<TileSet> tileSets)
	{ this.tileSets = tileSets; }
	
	public ArrayList<Map> getMaps()
	{ return this.maps; }
	
	public void setMaps(ArrayList<Map> maps)
	{ this.maps = maps; }

	public List<Constant> getMapDataConstants()
	{ return this.mapDataConstants; }

	public void setMapDataConstants(List<Constant> mapDataConstants)
	{ this.mapDataConstants = mapDataConstants; }
}
