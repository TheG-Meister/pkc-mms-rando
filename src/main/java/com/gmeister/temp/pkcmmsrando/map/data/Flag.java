package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.ArrayList;

/**
 * A boolean stored within the game which can only be obtained once per playthrough.<br>
 * <br>
 * Flags are a type of constant. They are named within the disassembly and contain
 * @author The_G_Meister
 *
 */
public class Flag extends Constant
{

	/*
	 * Could require items as an object type
	 * Could be set from items
	 * Having a name is good, we use those a lot
	 * Hmmm each permission would have a name. A permissionS object is a bit weird as it's just a list of them
	 * A player will have a bunch of permissions, items and events. Items plus events lead to permissions
	 * Getting certain items and or events opens up other items and or events via the use of permissions?
	 */

	//https://github.com/pret/pokecrystal/blob/master/constants/event_flags.asm
	//https://github.com/pret/pokecrystal/blob/master/constants/engine_flags.asm

	private ArrayList<Flag> prerequisites;

	public Flag()
	{
		super();
		this.prerequisites = new ArrayList<>();
	}

	public Flag(String name, int value)
	{
		super(name, value);
		this.prerequisites = new ArrayList<>();
	}

	public Flag(Constant constant)
	{
		super(constant.getName(), constant.getValue());
		this.prerequisites = new ArrayList<>();
	}

	/**
	 * Get the list of prerequisites to this Flag.
	 * @return the list of prerequisites
	 */
	public ArrayList<Flag> getPrerequisites()
	{ return this.prerequisites; }

	public void setPrerequisites(ArrayList<Flag> prerequisites)
	{ this.prerequisites = new ArrayList<>(prerequisites); }

}