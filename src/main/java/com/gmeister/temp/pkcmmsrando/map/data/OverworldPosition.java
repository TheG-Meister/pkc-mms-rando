package com.gmeister.temp.pkcmmsrando.map.data;

import com.gmeister.temp.pkcmmsrando.map.data.MapBlocks.Direction;

/**
 * Holds a Map, x coordinate and y coordinate, and provides methods for travelling through MapConnections and Warps. <br>
 * <br>
 * Commonly used by overworld objects that have a defined location. Each instance is immutable.
 * @author The_G_Meister
 *
 */
public final class OverworldPosition
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
	
	public final OverworldPosition moveThroughConnection(Direction direction)
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
	
	public final CollisionConstant getCollision()
	{ return this.map.getBlocks().getCollisionAt(this.x, this.y); }
	
	public final boolean isWithinMap()
	{
		if (this.x < 0) return false;
		else if (this.y < 0) return false;
		else if (this.x >= this.map.getBlocks().getCollisionXCapacity()) return false;
		else if (this.y >= this.map.getBlocks().getCollisionYCapacity()) return false;
		else return true;
	}
	
	public final OverworldPosition set(int x, int y)
	{ return new OverworldPosition(this.map, x, y); }
	
	public final OverworldPosition add(int x, int y)
	{ return new OverworldPosition(this.map, this.x + x, this.y + y); }
	
	public final OverworldPosition move(Direction direction)
	{ return this.move(direction, 1); }
	
	public final OverworldPosition move(Direction direction, int multiplier)
	{
		OverworldPosition position = this.add(direction.getDx() * multiplier, direction.getDy() * multiplier);
		if (!position.isWithinMap() && position.map.getConnections().get(direction) != null) position = position.moveThroughConnection(direction);
		return position;
	}

	public final Map getMap()
	{ return this.map; }

	public final int getX()
	{ return this.x; }

	public final int getY()
	{ return this.y; }
}
