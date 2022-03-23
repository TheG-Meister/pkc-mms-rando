package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.gmeister.temp.pkcmmsrando.map.data.Player.PlayerMovementResult;

public class MapExplorer
{
	
	private static class MapExplorationEntry
	{
		private MapExploration mapExploration;
		private boolean[][] collisionToExplore;
		
		private MapExplorationEntry(Map map)
		{
			this.mapExploration = new MapExploration(map);
			this.collisionToExplore = new boolean[map.getBlocks().getCollisionYCapacity()][map.getBlocks().getCollisionXCapacity()];
		}
		
		private MapExplorationEntry(MapExploration mapExploration, boolean[][] collisionToExplore)
		{
			this.mapExploration = mapExploration;
			this.collisionToExplore = collisionToExplore;
		}
	}
	
	private Map map;
	private java.util.Map<Set<Flag>, MapExplorationEntry> mapExplorationTable;

	public MapExplorer(Map map)
	{
		super();
		this.map = map;
		this.mapExplorationTable = new HashMap<>();
	}

	public Map getMap()
	{ return this.map; }

	public void setMap(Map map)
	{ this.map = map; }
	
	//This is the more general case which gives solutions for every set of flags
	//Explore for every combination of flags necessary to explore the whole map
	public void explore()
	{
		this.explore(s -> true);
	}
	
	//This is good if you only want to know a specific set of flags
	//Explore for every combination of flags provided
	public void explore(Set<Flag> flags)
	{
		this.explore(s -> flags.containsAll(s));
	}
	
	private void explore(Predicate<Set<Flag>> flagSetCondition)
	{
		List<Set<Flag>> flagSets = new ArrayList<>();
		
		//Find all flag sets with any collision to update and add them all to flagSets
		flagSetTests:
		for (Set<Flag> flags : this.mapExplorationTable.keySet()) if (flagSetCondition.test(flags))
		{
			boolean[][] collisionToUpdate = this.mapExplorationTable.get(flags).collisionToExplore;
			for (int y = 0; y < collisionToUpdate.length; y++)
				for (int x = 0; x < collisionToUpdate[y].length; x++)
					if (collisionToUpdate[y][x])
			{
				flagSets.add(flags);
				continue flagSetTests;
			}
		}
		
		while (!flagSets.isEmpty())
		{
			//Find the smallest set of flags
			//This reduces the number of movements to simulate and avoids some bugs of double-simulating a tile
			Set<Flag> flags = flagSets.get(0);
			for (Set<Flag> set : flagSets) if (set.size() < flags.size()) flags = set;
			flagSets.remove(flags);
			
			//Perform map exploration for this set of flags and store the returned flag sets to update
			List<Set<Flag>> flagSetsToUpdate = this.updateExplorations(flags);
			
			//Add unique flag sets to update to the flagSets list
			for (Set<Flag> set : flagSetsToUpdate) if (!flagSets.contains(set) && flagSetCondition.test(flags)) flagSets.add(set);
		}
	}
	
	//This is private as it has a different context to the two public methods
	//Should only be run if all combinations of flags have been tested beforehand
	//Returns a list of flag sets that require further testing
	private List<Set<Flag>> updateExplorations(Set<Flag> flags)
	{
		List<Set<Flag>> flagSetsToUpdate = new ArrayList<>();
		
		int xCapacity = this.map.getBlocks().getCollisionXCapacity();
		int yCapacity = this.map.getBlocks().getCollisionYCapacity();
		
		MapExplorationEntry entry = this.mapExplorationTable.get(flags);
		if (entry == null) entry = new MapExplorationEntry(this.map);
		
		//Find results that this flag set contains all the flags of
		//Copy their accessed tiles into this result's accessed tiles
		for (Set<Flag> otherFlags : this.mapExplorationTable.keySet()) if (!otherFlags.equals(flags) && flags.containsAll(otherFlags))
		{
			MapExploration otherExploration = this.mapExplorationTable.get(otherFlags).mapExploration;
			for (int y = 0; y < yCapacity; y++)
				for (int x = 0; x < xCapacity; x++)
					entry.mapExploration.getTilesAccessed()[y][x] = entry.mapExploration.getTilesAccessed()[y][x] || otherExploration.getTilesAccessed()[y][x];
			entry.mapExploration.getConnectionsAccessed().putAll(otherExploration.getConnectionsAccessed());
			entry.mapExploration.getWarpsAccessed().addAll(otherExploration.getWarpsAccessed());
		}
		
		//Explore for a single set of flags only
		boolean mapChanged;
		do
		{
			mapChanged = false;
			
			for (int y = 0; y < yCapacity; y++) for (int x = 0; x < xCapacity; x++) if (entry.collisionToExplore[y][x])
			{
				OverworldPosition position = new OverworldPosition(map, x, y);
				
				for (Direction direction : Direction.values())
				{
					Player player = new Player(position, direction, false);
					
					Set<Flag> currentFlags = new HashSet<>(flags);
					MapExplorationEntry currentEntry = this.mapExplorationTable.get(currentFlags);
					
					boolean warped = false;
					do
					{
						PlayerMovementResult movement = player.getMovement();
						
						//If we don't have necessary flags, stop before the movement with current flags
						if (movement.necessaryFlags != null && !currentFlags.containsAll(movement.necessaryFlags))
						{
							//Stop the current player
							OverworldPosition currentPosition = player.getPosition();
							if (!position.equals(currentPosition) && !warped && currentPosition.getMap()
									.equals(this.map)
									&& !currentEntry.mapExploration.getTilesAccessed()[currentPosition
											.getY()][currentPosition.getX()])
							{
								currentEntry.collisionToExplore[currentPosition.getY()][currentPosition.getX()] = true;
								if (!currentFlags.equals(flags) && !flagSetsToUpdate.contains(flags)) flagSetsToUpdate.add(flags);
							}
							
							//Add the new required flags for this movement
							currentFlags.addAll(movement.necessaryFlags);
							
							//Update the current result to match the current flag set
							currentEntry = this.mapExplorationTable.get(currentFlags);
							if (currentEntry == null)
							{
								currentEntry = new MapExplorationEntry(map);
								this.mapExplorationTable.put(currentFlags, currentEntry);
							}
						}
						
						player = movement.player;
						
						if (movement.connectionsUsed != null) for (MapConnection connection : movement.connectionsUsed)
						{
							if (!currentEntry.mapExploration.getConnectionsAccessed().containsKey(connection))
								currentEntry.mapExploration.getConnectionsAccessed().put(connection, new ArrayList<>());
							currentEntry.mapExploration.getConnectionsAccessed().get(connection).add(movement.player.getPosition());
						}
						
						if (movement.warpsUsed != null && !movement.warpsUsed.isEmpty())
						{
							currentEntry.mapExploration.getWarpsAccessed().addAll(movement.warpsUsed);
							warped = true;
							break;
						}
					}
					while (player.isSliding());
					
					//This is duplicated code from above
					OverworldPosition currentPosition = player.getPosition();
					if (!position.equals(currentPosition) && !warped && currentPosition.getMap()
							.equals(this.map)
							&& !currentEntry.mapExploration.getTilesAccessed()[currentPosition
									.getY()][currentPosition.getX()])
					{
						currentEntry.collisionToExplore[currentPosition.getY()][currentPosition.getX()] = true;
						if (!currentFlags.equals(flags) && !flagSetsToUpdate.contains(flags)) flagSetsToUpdate.add(flags);
					}
				}
				
				mapChanged = true;
				entry.mapExploration.getTilesAccessed()[y][x] = true;
			}
		}
		while (mapChanged);
		
		return flagSetsToUpdate;
	}
	
}