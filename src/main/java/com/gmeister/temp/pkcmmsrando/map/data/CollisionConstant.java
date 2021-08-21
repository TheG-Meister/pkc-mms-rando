package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.ArrayList;
import java.util.List;

import com.gmeister.temp.pkcmmsrando.map.data.MapBlocks.Direction;

public class CollisionConstant extends Constant
{
	
	private ArrayList<Direction> movementTableDirectionCol;
	private ArrayList<Boolean> movementTableStepOnCol;
	private ArrayList<Boolean> movementTableIsPossibleCol;
	private ArrayList<ArrayList<Flag>> movementTableFlagsCol;
	//private ArrayList<Player states>
	
	public CollisionConstant()
	{
		super();
		this.setUpLists();
	}
	
	public CollisionConstant(String name, byte value)
	{
		super(name, value);
		this.setUpLists();
	}
	
	private void setUpLists()
	{
		this.movementTableDirectionCol = new ArrayList<>();
		this.movementTableStepOnCol = new ArrayList<>();
		this.movementTableFlagsCol = new ArrayList<>();
		
		boolean[] booleans = {false, true};
		
		for (boolean b : booleans) for (Direction direction : Direction.values())
		{
			this.movementTableDirectionCol.add(direction);
			this.movementTableStepOnCol.add(b);
			this.movementTableFlagsCol.add(new ArrayList<>());
		}
	}
	
	public ArrayList<Flag> getFlagsForMovement(Direction direction, boolean stepOn)
	{
		ArrayList<Flag> output = null;
		
		for (int i = 0; i < this.movementTableDirectionCol.size(); i++) if (this.movementTableDirectionCol.get(i).equals(direction) && this.movementTableStepOnCol.get(i) == stepOn)
		{
			output = this.movementTableFlagsCol.get(i);
			break;
		}
		
		if (output == null) throw new IllegalStateException("Could not find flags for the specified direction and step");
		
		return output;
	}
	
	public boolean allowsMovement(Direction direction, boolean stepOn, List<Flag> flags)
	{
		return flags.containsAll(this.getFlagsForMovement(direction, stepOn));
	}
	
}
