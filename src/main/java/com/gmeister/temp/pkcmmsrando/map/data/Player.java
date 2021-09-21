package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.gmeister.temp.pkcmmsrando.map.data.MapBlocks.Direction;

/*
 * A player has a bunch of flags they can obtain, as well as stuff like items, pokemon, etc.
 * They can move across maps, battle trainers, interact with anything, etc.
 */

public class Player
{
	
	public static enum PlayerMovementAction
	{ HOP, SLIDE, WARP }
	
	public static final class OverworldPosition
	{
		private final Map map;
		private final int x;
		private final int y;
		
		public OverworldPosition(Map map, int x, int y)
		{
			super();
			this.map = map;
			this.x = x;
			this.y = y;
		}
		
		public OverworldPosition warpTo(Warp warp)
		{
			if (warp == null) throw new NullPointerException();
			return new OverworldPosition(warp.getMap(), warp.getX(), warp.getY());
		}
		
		public OverworldPosition moveThroughConnection(Direction direction)
		{
			MapConnection connection = this.map.getConnections().get(direction);
			if (connection == null) throw new NullPointerException("Map " + this.map.getConstName() + " has no connection to the " + direction.getCardinalName());
			
			switch (direction)
			{
				case UP:
					return new OverworldPosition(connection.getMap(), this.x + connection.getOffset() * Block.COLLISION_WIDTH, this.y + connection.getMap().getBlocks().getCollisionYCapacity());
				case DOWN:
					return new OverworldPosition(connection.getMap(), this.x + connection.getOffset() * Block.COLLISION_WIDTH, this.y - this.map.getBlocks().getCollisionYCapacity());
				case LEFT:
					return new OverworldPosition(connection.getMap(), this.x + connection.getMap().getBlocks().getCollisionXCapacity(), this.y + connection.getOffset() * Block.COLLISION_WIDTH);
				case RIGHT:
					return new OverworldPosition(connection.getMap(), this.x - this.map.getBlocks().getCollisionXCapacity(), this.y + connection.getOffset() * Block.COLLISION_WIDTH);
				default:
					throw new IllegalArgumentException("direction was not a valid Direction");
			}
		}
		
		public OverworldPosition set(int x, int y)
		{ return new OverworldPosition(this.map, x, y); }
		
		public OverworldPosition add(int x, int y)
		{ return new OverworldPosition(this.map, this.x + x, this.y + y); }
		
		public OverworldPosition move(Direction direction)
		{ return this.move(direction, 1); }
		
		public OverworldPosition move(Direction direction, int multiplier)
		{
			OverworldPosition position = new OverworldPosition(this.map, this.x + direction.getDx() * multiplier, this.y + direction.getDy() * multiplier);
			if (!position.isWithinMap()) position = position.moveThroughConnection(direction);
			if (!position.isWithinMap()) throw new IllegalArgumentException("This movement puts the position out of bounds.");
			else return position;
		}
		
		public CollisionConstant getCollision()
		{ return this.map.getBlocks().getCollisionAt(this.x, this.y); }
		
		public boolean isWithinMap()
		{
			if (this.x < 0) return false;
			else if (this.y < 0) return false;
			else if (this.x >= this.map.getBlocks().getCollisionXCapacity()) return false;
			else if (this.y >= this.map.getBlocks().getCollisionYCapacity()) return false;
			else return true;
		}

		public Map getMap()
		{ return this.map; }

		public int getX()
		{ return this.x; }

		public int getY()
		{ return this.y; }
	}
	
	private ArrayList<Flag> flags;
	private HashMap<Map, boolean[][]> playableAreas;
	
	private OverworldPosition position;
	//private Direction facing;
	
	public Player()
	{
		this.flags = new ArrayList<>();
		this.playableAreas = new HashMap<>();
		this.position = new OverworldPosition(null, 0, 0);
	}

	public ArrayList<Flag> getFlags()
	{ return this.flags; }

	public HashMap<Map, boolean[][]> getPlayableAreas()
	{ return this.playableAreas; }
	
	public void move(Direction direction)
	{
		boolean slide;
		do
		{
			slide = false;
			CollisionPermission perm = this.position.getCollision().getPermissionsForStep(direction, false);
			
			if (perm.getAction().equals(PlayerMovementAction.WARP) && this.attemptWarp(this.position));
			else if (perm.isAllowed() && this.flags.containsAll(perm.getFlags()))
			{
				if (perm.getAction().equals(PlayerMovementAction.HOP))
				{
					this.hop(direction);
					slide = this.checkSlide(direction);
				}
				else
				{
					OverworldPosition nextPosition = this.position.move(direction);
					CollisionPermission nextPerm = nextPosition.getCollision().getPermissionsForStep(direction, true);
					
					if (nextPerm.getAction().equals(PlayerMovementAction.WARP) && this.attemptWarp(nextPosition));
					else if (nextPerm.isAllowed() && this.flags.containsAll(nextPerm.getFlags()))
					{
						if (nextPerm.getAction().equals(PlayerMovementAction.HOP)) this.hop(direction);
						else this.move(direction);
						
						slide = this.checkSlide(direction);
					}
				}
			}
		}
		while (slide);
	}
	
	public boolean attemptWarp(OverworldPosition position)
	{
		//Find a warp for the provided position, warping to it and returning true if it exists
		for (Warp warp : position.getMap().getWarps()) if (warp.getX() == position.getX() && warp.getY() == position.getY())
		{
			this.position = position.warpTo(warp);
			return true;
		}
		
		//otherwise, return false
		return false;
	}
	
	public void step(Direction direction)
	{
		this.position = this.position.move(direction);
	}
	
	public void hop(Direction direction)
	{
		this.position = this.position.move(direction, 2);
	}
	
	private boolean checkSlide(Direction direction)
	{
		return PlayerMovementAction.SLIDE.equals(this.position.getCollision().getPermissionsForStep(direction, true).getAction());
	}
	
	public void testAllMovements()
	{
		/*
		 * I guess take each map in the hashmap
		 * re-perform all movement with current flags
		 * obtain new flags
		 * travel through warps and map connections, adding new maps where relevant
		 * Repeat
		 */
		int loop = 0;
		boolean changed;
		do
		{
			loop++;
			System.out.println("Loop " + loop);
			changed = false;
			
			ArrayList<Map> maps = new ArrayList<>(this.playableAreas.keySet());
			
			while (maps.size() > 0)
			{
				Map map = maps.remove(0);
				boolean[][] oldArea = this.playableAreas.get(map);
				boolean[][] newArea = map.expandMovement(oldArea, this.flags);
				
				if (map.getConstName().equals("MAHOGANY_TOWN"))
				{
					System.out.println(map.getConstName());
					System.out.println("Old area:");
					for (int y = 0; y < oldArea.length; y++)
					{
						for (int x = 0; x < oldArea[y].length; x++) System.out.print(oldArea[y][x] ? '1' : '0');
						System.out.println();
					}
					System.out.println();
					System.out.println("New area:");
					for (int y = 0; y < newArea.length; y++)
					{
						for (int x = 0; x < newArea[y].length; x++) System.out.print(newArea[y][x] ? '1' : '0');
						System.out.println();
					}
					System.out.println();
				}
				
				for (int i = 0; i < oldArea.length; i++) if (!Arrays.equals(oldArea[i], newArea[i]))
				{
					changed = true;
					this.playableAreas.put(map, newArea);
					
					for (Warp warp : map.getWarps()) if (newArea[warp.getY()][warp.getX()] && warp.hasAccessibleDestination() && warp.getDestination() != null)
					{
						Map destination = warp.getDestination().getMap();
						boolean[][] destinationArea;
						
						if (this.playableAreas.keySet().contains(destination)) destinationArea = this.playableAreas.get(destination);
						else
						{
							destinationArea = new boolean[destination.getBlocks().getYCapacity() * 2][destination.getBlocks().getXCapacity() * 2];
							maps.add(destination);
						}
						
						destinationArea[warp.getDestination().getY()][warp.getDestination().getX()] = true;
						this.playableAreas.put(destination, destinationArea);
					}
					
					//map connections
					for (Direction direction : Direction.values()) if (map.getConnections().get(direction) != null)
					{
						MapConnection connection = map.getConnections().get(direction);
						int offset = connection.getOffset() * 2;
						boolean[][] connectionArea;
						
						if (this.playableAreas.keySet().contains(connection.getMap())) connectionArea = this.playableAreas.get(connection.getMap());
						else
						{
							connectionArea = new boolean[connection.getMap().getBlocks().getYCapacity() * 2][connection.getMap().getBlocks().getXCapacity() * 2];
							maps.add(connection.getMap());
						}
						
						int start = Math.max(0, offset);
						
						switch (direction)
						{
							case UP:
							{
								int end = Math.min(map.getBlocks().getXCapacity() * 2, connection.getMap().getXCapacity() * 2 + offset);
								int y1 = 0;
								int y2 = connection.getMap().getBlocks().getYCapacity() * 2 - 1;
								for (int x1 = start, x2 = start - offset; x1 < end; x1++, x2++) if (newArea[y1][x1] &&
									map.getBlocks().getCollisionAt(x1, y1).canMoveTo(connection.getMap().getBlocks().getCollisionAt(x2, y2), direction, this.flags))
										connectionArea[y2][x2] = true;
								break;
							}
							case DOWN:
							{
								int end = Math.min(map.getBlocks().getXCapacity() * 2, connection.getMap().getXCapacity() * 2 + offset);
								int y1 = map.getBlocks().getYCapacity() * 2 - 1;
								int y2 = 0;
								for (int x1 = start, x2 = start - offset; x1 < end; x1++, x2++) if (newArea[y1][x1] &&
									map.getBlocks().getCollisionAt(x1, y1).canMoveTo(connection.getMap().getBlocks().getCollisionAt(x2, y2), direction, this.flags))
										connectionArea[y2][x2] = true;
								break;
							}
							case LEFT:
							{
								int end = Math.min(map.getBlocks().getYCapacity() * 2, connection.getMap().getYCapacity() * 2 + offset);
								int x1 = 0;
								int x2 = connection.getMap().getBlocks().getXCapacity() * 2 - 1;
								for (int y1 = start, y2 = start - offset; y1 < end; y1++, y2++) if (newArea[y1][x1] &&
									map.getBlocks().getCollisionAt(x1, y1).canMoveTo(connection.getMap().getBlocks().getCollisionAt(x2, y2), direction, this.flags))
										connectionArea[y2][x2] = true;
								break;
							}
							case RIGHT:
							{
								int end = Math.min(map.getBlocks().getYCapacity() * 2, connection.getMap().getYCapacity() * 2 + offset);
								int x1 = map.getBlocks().getXCapacity() * 2 - 1;
								int x2 = 0;
								for (int y1 = start, y2 = start - offset; y1 < end; y1++, y2++) if (newArea[y1][x1] &&
									map.getBlocks().getCollisionAt(x1, y1).canMoveTo(connection.getMap().getBlocks().getCollisionAt(x2, y2), direction, this.flags))
										connectionArea[y2][x2] = true;
								break;
							}
						}
						
						this.playableAreas.put(connection.getMap(), connectionArea);
					}
					
					break;
				}
			}
		}
		while (changed);
	}
	
}
