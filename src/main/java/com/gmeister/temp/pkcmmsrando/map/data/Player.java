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
					return new OverworldPosition(connection.getMap(), this.x - connection.getOffset() * Block.COLLISION_WIDTH, this.y + connection.getMap().getBlocks().getCollisionYCapacity());
				case DOWN:
					return new OverworldPosition(connection.getMap(), this.x - connection.getOffset() * Block.COLLISION_WIDTH, this.y - this.map.getBlocks().getCollisionYCapacity());
				case LEFT:
					return new OverworldPosition(connection.getMap(), this.x + connection.getMap().getBlocks().getCollisionXCapacity(), this.y - connection.getOffset() * Block.COLLISION_WIDTH);
				case RIGHT:
					return new OverworldPosition(connection.getMap(), this.x - this.map.getBlocks().getCollisionXCapacity(), this.y - connection.getOffset() * Block.COLLISION_WIDTH);
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
			if (!position.isWithinMap() && position.map.getConnections().get(direction) != null) position = position.moveThroughConnection(direction);
			return position;
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
			
			if (PlayerMovementAction.WARP.equals(perm.getAction()) && this.attemptWarp(this.position));
			else if (perm.isAllowed() && this.flags.containsAll(perm.getFlags()))
			{
				if (PlayerMovementAction.HOP.equals(perm.getAction()))
				{
					this.hop(direction);
					slide = this.checkSlide(direction);
				}
				else
				{
					OverworldPosition nextPosition = this.position.move(direction);
					if (nextPosition.isWithinMap())
					{
						CollisionPermission nextPerm = nextPosition.getCollision().getPermissionsForStep(direction, true);
						
						if (PlayerMovementAction.WARP.equals(nextPerm.getAction()) && this.attemptWarp(nextPosition));
						else if (nextPerm.isAllowed() && this.flags.containsAll(nextPerm.getFlags()))
						{
							if (PlayerMovementAction.HOP.equals(nextPerm.getAction())) this.hop(direction);
							else this.step(direction);
							
							slide = this.checkSlide(direction);
						}
					}
				}
			}
		}
		while (slide);
	}
	
	public boolean attemptWarp(OverworldPosition position)
	{
		//Find a warp for the provided position, warping to it and returning true if it exists
		for (Warp warp : position.getMap().getWarps()) if (warp.getX() == position.getX() && warp.getY() == position.getY()) if (warp.getDestination() != null)
		{
			this.position = position.warpTo(warp.getDestination());
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
		ArrayList<Map> maps = new ArrayList<>(this.playableAreas.keySet());
		
		while (maps.size() > 0)
		{
			Map map = maps.remove(0);
			
			boolean[][] oldCollisionsValid = this.playableAreas.get(map);
			
			boolean[][] collisionsToTest = new boolean[oldCollisionsValid.length][];
			for (int y = 0; y < oldCollisionsValid.length; y++) collisionsToTest[y] = Arrays.copyOf(oldCollisionsValid[y], oldCollisionsValid[y].length);
			boolean[][] collisionsTested = new boolean[collisionsToTest.length][collisionsToTest[0].length];
			boolean[][] collisionsValid = new boolean[collisionsToTest.length][collisionsToTest[0].length];
			
			boolean mapChanged;
			do
			{
				mapChanged = false;
				
				for (int y = 0; y < collisionsToTest.length; y++) for (int x = 0; x < collisionsToTest[y].length; x++) if (!collisionsTested[y][x] && collisionsToTest[y][x])
				{
					OverworldPosition position = new OverworldPosition(map, x, y);
					for (Direction direction : Direction.values())
					{
						this.position = position;
						this.move(direction);
						if (!position.equals(this.position))
						{
							if (this.position.getMap().equals(map)) collisionsToTest[this.position.getY()][this.position.getX()] = true;
							else
							{
								boolean[][] nextMapCollision;
								
								if (this.playableAreas.keySet().contains(this.position.getMap())) nextMapCollision = this.playableAreas.get(this.position.getMap());
								else
								{
									nextMapCollision = new boolean[this.position.getMap().getBlocks().getCollisionYCapacity()][this.position.getMap().getBlocks().getCollisionXCapacity()];
									maps.add(this.position.getMap());
								}
								
								if (!nextMapCollision[this.position.getY()][this.position.getX()])
								{
									nextMapCollision[this.position.getY()][this.position.getX()] = true;
									this.playableAreas.put(this.position.getMap(), nextMapCollision);
									if (!maps.contains(this.position.getMap())) maps.add(this.position.getMap());
								}
							}
						}
					}
					
					mapChanged = true;
					collisionsTested[y][x] = true;
					collisionsValid[y][x] = true;
				}
			}
			while (mapChanged);
			
			this.playableAreas.put(map, collisionsValid);
			
			if (map.getConstName().equals("ECRUTEAK_CITY"))
			{
				System.out.println("Old area:");
				for (int y = 0; y < oldCollisionsValid.length; y++)
				{
					for (int x = 0; x < oldCollisionsValid[y].length; x++) System.out.print(oldCollisionsValid[y][x] ? '1' : '0');
					System.out.println();
				}
				System.out.println();
				System.out.println("New area:");
				for (int y = 0; y < collisionsValid.length; y++)
				{
					for (int x = 0; x < collisionsValid[y].length; x++) System.out.print(collisionsValid[y][x] ? '1' : '0');
					System.out.println();
				}
				System.out.println();
			}
		}
	}
	
}
