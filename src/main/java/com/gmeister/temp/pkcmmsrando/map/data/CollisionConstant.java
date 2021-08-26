package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.ArrayList;

import com.gmeister.temp.pkcmmsrando.map.data.MapBlocks.Direction;

public class CollisionConstant extends Constant
{
	
	private ArrayList<Direction> movementTableDirections;
	private ArrayList<CollisionPermission> movementTableStepOnPermissions;
	private ArrayList<CollisionPermission> movementTableStepOffPermissions;
	
	public CollisionConstant()
	{
		super();
		this.setUpLists();
	}
	
	public CollisionConstant(String name, int value)
	{
		super(name, value);
		this.setUpLists();
	}
	
	public CollisionConstant(Constant constant)
	{
		super(constant.getName(), constant.getValue());
		this.setUpLists();
	}
	
	private void setUpLists()
	{
		this.movementTableDirections = new ArrayList<>();
		this.movementTableStepOnPermissions = new ArrayList<>();
		this.movementTableStepOffPermissions = new ArrayList<>();
		
		for (Direction direction : Direction.values())
		{
			this.movementTableDirections.add(direction);
			this.movementTableStepOnPermissions.add(null);
			this.movementTableStepOffPermissions.add(null);
		}
	}
	
	public CollisionPermission getPermissionsForStep(Direction direction, boolean stepOn)
	{
		if (direction == null) throw new NullPointerException();
		if (!this.movementTableDirections.contains(direction)) throw new IllegalStateException();
		
		if (stepOn) return this.movementTableStepOnPermissions.get(this.movementTableDirections.indexOf(direction));
		else return this.movementTableStepOffPermissions.get(this.movementTableDirections.indexOf(direction));
	}
	
	public void setPermissionsForStep(Direction direction, boolean stepOn, CollisionPermission permission)
	{
		if (direction == null) throw new NullPointerException();
		if (!this.movementTableDirections.contains(direction)) throw new IllegalStateException();
		
		if (stepOn) this.movementTableStepOnPermissions.set(this.movementTableDirections.indexOf(direction), permission);
		else this.movementTableStepOffPermissions.set(this.movementTableDirections.indexOf(direction), permission);
	}
	
}
