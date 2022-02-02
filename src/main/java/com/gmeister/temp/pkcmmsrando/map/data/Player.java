package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
		
		public PlayerMovementResult(Player player, List<Warp> warpsUsed)
		{
			this.player = player;
			this.warpsUsed = warpsUsed;
		}
	}
	
	private final OverworldPosition position;
	private final Direction facing;
	private final boolean sliding;
	
	private ArrayList<Flag> flags;
	
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
	
	public Player(OverworldPosition position, Direction facing, boolean sliding, ArrayList<Flag> flags)
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

	public ArrayList<Flag> getFlags()
	{ return new ArrayList<>(this.flags); }
	
	public Player setFlags(ArrayList<Flag> flags)
	{ return new Player(this.position, this.facing, this.sliding, flags); }
	
	public PlayerMovementResult getMovement()
	{
		Player player;
		List<Warp> warpsUsed = new ArrayList<>();
		
		CollisionPermission perm = this.position.getCollision().getPermissionsForStep(this.facing, false);
		
		if (!this.flags.containsAll(perm.getFlags())) player = this.setSliding(false);
		else
		{
			Warp warp = this.position.getMap().findWarpAt(this.position.getX(), this.position.getY());
			
			if (PlayerMovementAction.WARP.equals(perm.getAction()) && warp != null && warp.getDestination() != null)
			{
				player = this.setSliding(false).setPosition(this.position.warpTo(warp.getDestination()));
				warpsUsed.add(warp);
			}
			else if (!perm.isAllowed()) player = this.setSliding(false);
			else if (PlayerMovementAction.HOP.equals(perm.getAction())) player = this.hop();
			else
			{
				Player movedPlayer = this.setPosition(this.position.move(this.facing));
				if (!movedPlayer.position.isWithinMap()) player = this.setSliding(false);
				CollisionPermission nextPerm = movedPlayer.position.getCollision().getPermissionsForStep(this.facing, true);
				
				if (!this.flags.containsAll(nextPerm.getFlags())) player = this.setSliding(false);
				else if (this.position.getMap().hasCoordEventAt(this.position.getX(), this.position.getY())) player = this.setSliding(false);
				else if (this.position.getMap().hasObjectEventAt(this.position.getX(), this.position.getY())) player = this.setSliding(false);
				else
				{
					warp = movedPlayer.position.getMap().findWarpAt(this.position.getX(), this.position.getY());
					
					if (PlayerMovementAction.WARP.equals(perm.getAction()) && warp != null && warp.getDestination() != null)
					{
						player = movedPlayer.setSliding(false).setPosition(this.position.warpTo(warp.getDestination()));
						warpsUsed.add(warp);
					}
					else if (!nextPerm.isAllowed()) player = this.setSliding(false);
					else if (PlayerMovementAction.HOP.equals(nextPerm.getAction())) player = this.hop();
					else player = this.step(PlayerMovementAction.SLIDE.equals(nextPerm.getAction()));
				}
			}
		}
		
		return new PlayerMovementResult(player, warpsUsed);
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
	
	public Player attemptWarp()
	{
		//Find a warp for the provided position, warping to it and returning true if it exists
		Warp warp = this.position.getMap().findWarpAt(this.position.getX(), this.position.getY());
		if (warp != null && warp.getDestination() != null) return this.setPosition(this.position.warpTo(warp.getDestination()));
		
		//otherwise, return false
		else return null;
	}
	
	public Player hop()
	{
		OverworldPosition nextPosition = this.position.move(this.facing, 2);
		boolean sliding = PlayerMovementAction.SLIDE.equals(nextPosition.getCollision().getPermissionsForStep(this.facing, true).getAction());
		return new Player(nextPosition, this.facing, sliding, this.flags);
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
	
}
