package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MapExploration
{
	
	private boolean[][] tilesAccessed;
	private Set<Warp> warpsAccessed;
	private java.util.Map<MapConnection, Set<OverworldPosition>> connectionsAccessed;
	
	public MapExploration(Map map)
	{
		this.tilesAccessed = new boolean[map.getBlocks()
				.getCollisionYCapacity()][map.getBlocks()
						.getCollisionXCapacity()];
		this.warpsAccessed = new HashSet<>();
		this.connectionsAccessed = new HashMap<>();
	}
	
	public MapExploration(boolean[][] tilesAccessed, Set<Warp> warpsAccessed,
			java.util.Map<MapConnection, Set<OverworldPosition>> connectionsAccessed)
	{
		super();
		this.tilesAccessed = tilesAccessed;
		this.warpsAccessed = warpsAccessed;
		this.connectionsAccessed = connectionsAccessed;
	}
	
	public boolean[][] getTilesAccessed()
	{ return this.tilesAccessed; }
	
	public void setTilesAccessed(boolean[][] tilesAccessed)
	{ this.tilesAccessed = tilesAccessed; }
	
	public Set<Warp> getWarpsAccessed()
	{ return this.warpsAccessed; }
	
	public void setWarpsAccessed(Set<Warp> warpsAccessed)
	{ this.warpsAccessed = warpsAccessed; }
	
	public java.util.Map<MapConnection, Set<OverworldPosition>> getConnectionsAccessed()
	{ return this.connectionsAccessed; }
	
	public void setConnectionsAccessed(java.util.Map<MapConnection, Set<OverworldPosition>> connectionsAccessed)
	{ this.connectionsAccessed = connectionsAccessed; }
	
}
