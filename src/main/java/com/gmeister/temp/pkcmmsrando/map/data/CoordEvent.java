package com.gmeister.temp.pkcmmsrando.map.data;

public class CoordEvent
{
	
	private OverworldPosition position;
	private CollisionConstant simulatedCollision;
	
	public CoordEvent(OverworldPosition position, CollisionConstant simulatedCollision)
	{
		this.position = position;
		this.simulatedCollision = simulatedCollision;
	}

	public OverworldPosition getPosition()
	{ return this.position; }
	
	public void setPosition(OverworldPosition position)
	{ this.position = position; }

	public CollisionConstant getSimulatedCollision()
	{ return this.simulatedCollision; }

	public void setSimulatedCollision(CollisionConstant simulatedCollision)
	{ this.simulatedCollision = simulatedCollision; }
	
}
