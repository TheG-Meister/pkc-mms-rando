package com.gmeister.temp.pkcmmsrando.map.data;

public class Flag
{
	
	/*
	 * Could require items as an object type
	 * Could be set from items
	 * Having a name is good, we use those a lot
	 * Hmmm each permission would have a name. A permissionS object is a bit weird as it's just a list of them
	 * A player will have a bunch of permissions, items and events. Items plus events lead to permissions
	 * Getting certain items and or events opens up other items and or events via the use of permissions?
	 */
	
	private String name;
	
	public Flag(String name)
	{ this.name = name; }
	
	public String getName()
	{ return this.name; }
	
	public void setName(String name)
	{ this.name = name; }
	
}