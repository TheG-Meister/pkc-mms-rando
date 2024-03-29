package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.ArrayList;
import java.util.List;

public class CollisionPermission
{
	
	private String name;
	private boolean allowed;
	private PlayerMovementAction action;
	private ArrayList<Flag> flags;
	
	public CollisionPermission()
	{
		this.name = null;
		this.allowed = false;
		this.flags = new ArrayList<>();
	}
	
	public CollisionPermission(String name, boolean allowed, List<Flag> flags)
	{
		this.name = name;
		this.allowed = allowed;
		this.flags = new ArrayList<>(flags);
	}
	
	public String getName()
	{ return this.name; }
	
	public void setName(String name)
	{ this.name = name; }
	
	public boolean isAllowed()
	{ return this.allowed; }
	
	public void setAllowed(boolean allowed)
	{ this.allowed = allowed; }
	
	public PlayerMovementAction getAction()
	{ return this.action; }

	public void setAction(PlayerMovementAction action)
	{ this.action = action; }

	public ArrayList<Flag> getFlags()
	{ return this.flags; }
	
}
