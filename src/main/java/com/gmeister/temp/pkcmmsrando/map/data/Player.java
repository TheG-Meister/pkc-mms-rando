package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.gmeister.temp.pkcmmsrando.map.data.OverworldPosition.PositionMovementResult;

/*
 * A player has a bunch of flags they can obtain, as well as stuff like items, pokemon, etc.
 * They can move across maps, battle trainers, interact with anything, etc.
 */

public final class Player
{
	
	public static final class PlayerMovementResult
	{
		public Player player;
		public List<Warp> warpsUsed;
		public List<MapConnection> connectionsUsed;
		public List<Flag> necessaryFlags;
		
		public PlayerMovementResult()
		{
			this.warpsUsed = new ArrayList<>();
			this.connectionsUsed = new ArrayList<>();
		}
		
		public PlayerMovementResult(Player player)
		{
			this.player = player;
			this.warpsUsed = new ArrayList<>();
			this.connectionsUsed = new ArrayList<>();
		}
		
		public PlayerMovementResult(Player player, List<Warp> warpsUsed, List<MapConnection> connectionsUsed, List<Flag> necessaryFlags)
		{
			this.player = player;
			this.warpsUsed = warpsUsed;
			this.connectionsUsed = connectionsUsed;
			this.necessaryFlags = necessaryFlags;
		}
	}
	
	public static final class PlayerMapTravelResult
	{
		public boolean[][] tilesAccessed;
		public List<Warp> warpsAccessed;
		public java.util.Map<MapConnection, List<OverworldPosition>> connectionsAccessed;
		
		public PlayerMapTravelResult(boolean[][] tilesAccessed, List<Warp> warpsAccessed,
				java.util.Map<MapConnection, List<OverworldPosition>> connectionsAccessed)
		{
			this.tilesAccessed = tilesAccessed;
			this.warpsAccessed = warpsAccessed;
			this.connectionsAccessed = connectionsAccessed;
		}
	}
	
	private final OverworldPosition position;
	private final Direction facing;
	private final boolean sliding;
	
	private List<Flag> flags;
	
	public Player()
	{
		this.position = null;
		this.facing = null;
		this.sliding = false;
		this.flags = new ArrayList<>();
	}
	
	public Player(OverworldPosition position, Direction facing, boolean sliding, Flag... flags)
	{
		this.position = position;
		this.facing = facing;
		this.sliding = sliding;
		this.flags = new ArrayList<>(Arrays.asList(flags));
	}
	
	public Player(OverworldPosition position, Direction facing, boolean sliding, List<Flag> flags)
	{
		this.position = position;
		this.facing = facing;
		this.sliding = sliding;
		this.flags = new ArrayList<>(flags);
	}
	
	public Player(Player player)
	{
		this.position = player.position;
		this.facing = player.facing;
		this.sliding = player.sliding;
		this.flags = new ArrayList<>(player.flags);
	}
	
	public OverworldPosition getPosition()
	{ return this.position; }
	
	public Player setPosition(OverworldPosition position)
	{ return new Player(position, this.facing, this.sliding, this.flags); }

	public Direction getFacing()
	{ return this.facing; }
	
	public Player setFacing(Direction facing)
	{ return new Player(this.position, facing, this.sliding, this.flags); }

	public boolean isSliding()
	{ return this.sliding; }
	
	public Player setSliding(boolean sliding)
	{ return new Player(this.position, this.facing, sliding, this.flags); }

	public List<Flag> getFlags()
	{ return new ArrayList<>(this.flags); }
	
	public Player setFlags(List<Flag> flags)
	{ return new Player(this.position, this.facing, this.sliding, flags); }
	
	public PlayerMovementResult getMovement()
	{
		List<CoordEvent> coordEvents = this.position.getMap().getCoordEventsAt(this.position.getX(), this.position.getY());
		for (CoordEvent event : coordEvents)
		{
			PlayerMovementResult result = this.getMovement(event.getSimulatedCollision().getPermissionsForStep(this.facing, false), this.position);
			if (result != null) return result;
		}
		
		PlayerMovementResult stepOffResult = this.getMovement(this.position.getCollision().getPermissionsForStep(this.facing, false), this.position);
		if (stepOffResult != null) return stepOffResult;
		
		OverworldPosition nextPosition = this.position.move(this.facing);
		//check object events for the next tile
		if (!nextPosition.isWithinMap()) return new PlayerMovementResult(this.setSliding(false));
		for (ObjectEvent event : nextPosition.getMap().getObjectEvents())
			if (event.isPresentAt(nextPosition.getX(), nextPosition.getY()))
				return new PlayerMovementResult(this.setSliding(false));
		
		//check coord events for the next tile
		List<CoordEvent> nextCoordEvents = this.position.getMap().getCoordEventsAt(this.position.getX(), this.position.getY());
		for (CoordEvent event : nextCoordEvents)
		{
			PlayerMovementResult result = this.getMovement(event.getSimulatedCollision().getPermissionsForStep(this.facing, true), nextPosition);
			if (result != null) return result;
		}
		
		//check movement permission for the next tile
		PlayerMovementResult stepOnResult = this.getMovement(nextPosition.getCollision().getPermissionsForStep(this.facing, true), nextPosition);
		if (stepOnResult != null) return stepOnResult;
		
		//return step
		return this.getStepMovement();
	}
	
	private PlayerMovementResult getMovement(CollisionPermission perm, OverworldPosition position)
	{
		PlayerMovementResult stop = new PlayerMovementResult(this.setSliding(false));
		
		if (!this.flags.containsAll(perm.getFlags())) return stop;
		else
		{
			Warp warp = position.getMap().findWarpAt(position.getX(), position.getY());
			
			if (PlayerMovementAction.WARP.equals(perm.getAction()) && warp != null && warp.getDestination() != null) return this.getWarpAction(warp);
			else if (!perm.isAllowed()) return stop;
			else if (PlayerMovementAction.HOP.equals(perm.getAction())) return this.getHopMovement();
			else return null;
		}
	}
	
	public Player move(Direction direction)
	{ return this.setFacing(direction).move(); }

	public Player move()
	{
		CollisionPermission perm = this.position.getCollision().getPermissionsForStep(this.facing, false);
		
		if (!this.flags.containsAll(perm.getFlags())) return this.setSliding(false);
		else
		{
			Player warpedPlayer = this.attemptWarp();
			
			if (PlayerMovementAction.WARP.equals(perm.getAction()) && warpedPlayer != null) return warpedPlayer;
			else if (!perm.isAllowed()) return this.setSliding(false);
			else if (PlayerMovementAction.HOP.equals(perm.getAction())) return this.hop();
			else
			{
				Player movedPlayer = this.setPosition(this.position.move(this.facing));
				if (!movedPlayer.position.isWithinMap()) return this.setSliding(false);
				CollisionPermission nextPerm = movedPlayer.position.getCollision().getPermissionsForStep(this.facing, true);
				
				if (!this.flags.containsAll(nextPerm.getFlags())) return this.setSliding(false);
				else if (this.position.getMap().hasCoordEventAt(this.position.getX(), this.position.getY())) return this.setSliding(false);
				else if (this.position.getMap().hasObjectEventAt(this.position.getX(), this.position.getY())) return this.setSliding(false);
				else
				{
					warpedPlayer = movedPlayer.attemptWarp();
					
					if (PlayerMovementAction.WARP.equals(nextPerm.getAction()) && warpedPlayer != null) return warpedPlayer;
					else if (!nextPerm.isAllowed()) return this.setSliding(false);
					else if (PlayerMovementAction.HOP.equals(nextPerm.getAction())) return this.hop();
					else return this.step(PlayerMovementAction.SLIDE.equals(nextPerm.getAction()));
				}
			}
		}
	}
	
	public PlayerMovementResult getWarpAction(Warp warp)
	{
		return new PlayerMovementResult(new Player(this.position.warpTo(warp), this.facing, false, this.flags), new ArrayList<>(Arrays.asList(warp)), null, null);
	}
	
	public Player attemptWarp()
	{
		//Find a warp for the provided position, warping to it and returning true if it exists
		Warp warp = this.position.getMap().findWarpAt(this.position.getX(), this.position.getY());
		if (warp != null && warp.getDestination() != null) return this.setPosition(this.position.warpTo(warp.getDestination()));
		
		//otherwise, return false
		else return null;
	}
	
	public PlayerMovementResult getHopMovement()
	{
		PositionMovementResult movement = this.position.getMovement(this.facing, 2);
		boolean sliding = PlayerMovementAction.SLIDE.equals(movement.position.getCollision().getPermissionsForStep(this.facing, true).getAction());
		
		Player player = new Player(movement.position, this.facing, sliding, this.flags);
		List<MapConnection> connectionsUsed = null;
		if (movement.connectionUsed != null)
		{
			connectionsUsed = new ArrayList<>();
			connectionsUsed.add(movement.connectionUsed);
		}
		
		return new PlayerMovementResult(player, null, connectionsUsed, null);
	}
	
	public Player hop()
	{
		OverworldPosition nextPosition = this.position.move(this.facing, 2);
		boolean sliding = PlayerMovementAction.SLIDE.equals(nextPosition.getCollision().getPermissionsForStep(this.facing, true).getAction());
		return new Player(nextPosition, this.facing, sliding, this.flags);
	}
	
	public PlayerMovementResult getStepMovement()
	{
		PositionMovementResult movement = this.position.getMovement(this.facing, 1);
		boolean sliding = PlayerMovementAction.SLIDE.equals(movement.position.getCollision().getPermissionsForStep(this.facing, true).getAction());
		
		Player player = new Player(movement.position, this.facing, sliding, new ArrayList<>(this.flags));
		List<MapConnection> connectionsUsed = null;
		if (movement.connectionUsed != null)
		{
			connectionsUsed = new ArrayList<>();
			connectionsUsed.add(movement.connectionUsed);
		}
		
		return new PlayerMovementResult(player, null, connectionsUsed, null);
	}
	
	public Player step(boolean sliding)
	{ return new Player(this.position.move(this.facing), this.facing, sliding, this.flags); }
	
	public static void getAllAccesibleCollision(HashMap<Map, boolean[][]> accessibleCollision, ArrayList<Flag> flags)
	{
		ArrayList<Map> maps = new ArrayList<>(accessibleCollision.keySet());
		
		while (maps.size() > 0)
		{
			Map map = maps.remove(0);
			
			HashMap<Map, boolean[][]> accessibleCollisionFromMap = Player.getAccessibleCollision(map, accessibleCollision.get(map), flags);
			
			for (Map updatedMap : accessibleCollisionFromMap.keySet())
			{
				if (accessibleCollision.containsKey(updatedMap))
				{
					boolean[][] oldCollision = accessibleCollision.get(updatedMap);
					boolean[][] newCollision = accessibleCollisionFromMap.get(updatedMap);
					boolean changed = false;
					for (int y = 0; y < oldCollision.length; y++) for (int x = 0; x < oldCollision[y].length; x++) if (!oldCollision[y][x] && newCollision[y][x])
					{
						changed = true;
						oldCollision[y][x] = newCollision[y][x];
					}
					accessibleCollision.put(updatedMap, oldCollision);
					if (changed && map != updatedMap && !maps.contains(updatedMap)) maps.add(updatedMap);
				}
				else
				{
					accessibleCollision.put(updatedMap, accessibleCollisionFromMap.get(updatedMap));
					if (!maps.contains(updatedMap)) maps.add(updatedMap);
				}
			}
		}
	}
	
	public static HashMap<Map, boolean[][]> getAccessibleCollision(Map map, boolean[][] collisionToTest, ArrayList<Flag> flags)
	{
		HashMap<Map, boolean[][]> accessibleCollision = new HashMap<>();
		
		boolean[][] newCollisionToTest = new boolean[collisionToTest.length][];
		for (int y = 0; y < collisionToTest.length; y++) newCollisionToTest[y] = Arrays.copyOf(collisionToTest[y], collisionToTest[y].length);
		boolean[][] collisionTested = new boolean[newCollisionToTest.length][newCollisionToTest[0].length];
		boolean[][] collisionValid = new boolean[newCollisionToTest.length][newCollisionToTest[0].length];
		
		boolean mapChanged;
		do
		{
			mapChanged = false;
			
			for (int y = 0; y < newCollisionToTest.length; y++) for (int x = 0; x < newCollisionToTest[y].length; x++) if (!collisionTested[y][x] && newCollisionToTest[y][x])
			{
				OverworldPosition position = new OverworldPosition(map, x, y);
				for (Direction direction : Direction.values())
				{
					Player player = new Player(position, direction, false, flags);
					do player = player.move();
					while (player.isSliding());
					OverworldPosition nextPosition = player.getPosition();
					
					if (!position.equals(nextPosition))
					{
						if (nextPosition.getMap().equals(map)) newCollisionToTest[nextPosition.getY()][nextPosition.getX()] = true;
						else
						{
							boolean[][] nextMapCollision;
							
							if (accessibleCollision.keySet().contains(nextPosition.getMap())) nextMapCollision = accessibleCollision.get(nextPosition.getMap());
							else nextMapCollision = new boolean[nextPosition.getMap().getBlocks().getCollisionYCapacity()][nextPosition.getMap().getBlocks().getCollisionXCapacity()];
							
							if (!nextMapCollision[nextPosition.getY()][nextPosition.getX()])
							{
								nextMapCollision[nextPosition.getY()][nextPosition.getX()] = true;
								accessibleCollision.put(nextPosition.getMap(), nextMapCollision);
							}
						}
					}
				}
				
				mapChanged = true;
				collisionTested[y][x] = true;
				collisionValid[y][x] = true;
			}
		}
		while (mapChanged);
		
		accessibleCollision.put(map, collisionValid);
		
		return accessibleCollision;
	}
	
	public static PlayerMapTravelResult getMapTravelData(Map map, boolean[][] collisionToTest, List<Flag> flags)
	{
		List<Warp> warpsAccessed = new ArrayList<>();
		java.util.Map<MapConnection, List<OverworldPosition>> connectionsAccessed = new HashMap<>();
		
		boolean[][] newCollisionToTest = new boolean[collisionToTest.length][];
		for (int y = 0; y < collisionToTest.length; y++) newCollisionToTest[y] = Arrays.copyOf(collisionToTest[y], collisionToTest[y].length);
		boolean[][] collisionTested = new boolean[newCollisionToTest.length][newCollisionToTest[0].length];
		boolean[][] collisionValid = new boolean[newCollisionToTest.length][newCollisionToTest[0].length];
		
		boolean mapChanged;
		do
		{
			mapChanged = false;
			
			for (int y = 0; y < newCollisionToTest.length; y++) for (int x = 0; x < newCollisionToTest[y].length; x++) if (!collisionTested[y][x] && newCollisionToTest[y][x])
			{
				OverworldPosition position = new OverworldPosition(map, x, y);
				for (Direction direction : Direction.values())
				{
					Player player = new Player(position, direction, false, flags);
					
					boolean warped = false;
					do
					{
						PlayerMovementResult movement = player.getMovement();
						player = movement.player;
						
						if (movement.connectionsUsed != null) for (MapConnection connection : movement.connectionsUsed)
						{
							if (!connectionsAccessed.containsKey(connection)) connectionsAccessed.put(connection, new ArrayList<>());
							connectionsAccessed.get(connection).add(movement.player.getPosition());
						}
						
						if (movement.warpsUsed != null && !movement.warpsUsed.isEmpty())
						{
							warpsAccessed.addAll(movement.warpsUsed);
							warped = true;
							break;
						}
					}
					while (player.isSliding());
					
					OverworldPosition nextPosition = player.getPosition();
					if (!position.equals(nextPosition) && !warped && nextPosition.getMap().equals(map)) newCollisionToTest[nextPosition.getY()][nextPosition.getX()] = true;
				}
				
				mapChanged = true;
				collisionTested[y][x] = true;
				collisionValid[y][x] = true;
			}
		}
		while (mapChanged);
		
		return new PlayerMapTravelResult(collisionValid, warpsAccessed, connectionsAccessed);
	}
	
}
