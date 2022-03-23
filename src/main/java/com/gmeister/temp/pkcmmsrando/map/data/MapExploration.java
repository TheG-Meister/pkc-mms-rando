package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapExploration
{
	
	private boolean[][] tilesAccessed;
	private List<Warp> warpsAccessed;
	private java.util.Map<MapConnection, List<OverworldPosition>> connectionsAccessed;
	
	public MapExploration(Map map)
	{
		this.tilesAccessed = new boolean[map.getBlocks()
				.getCollisionYCapacity()][map.getBlocks()
						.getCollisionXCapacity()];
		this.warpsAccessed = new ArrayList<>();
		this.connectionsAccessed = new HashMap<>();
	}
	
	public MapExploration(boolean[][] tilesAccessed, List<Warp> warpsAccessed,
			java.util.Map<MapConnection, List<OverworldPosition>> connectionsAccessed)
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
	
	public List<Warp> getWarpsAccessed()
	{ return this.warpsAccessed; }
	
	public void setWarpsAccessed(List<Warp> warpsAccessed)
	{ this.warpsAccessed = warpsAccessed; }
	
	public java.util.Map<MapConnection, List<OverworldPosition>> getConnectionsAccessed()
	{ return this.connectionsAccessed; }
	
	public void setConnectionsAccessed(java.util.Map<MapConnection, List<OverworldPosition>> connectionsAccessed)
	{ this.connectionsAccessed = connectionsAccessed; }
	
}
