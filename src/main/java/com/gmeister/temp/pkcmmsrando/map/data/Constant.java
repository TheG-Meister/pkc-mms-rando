package com.gmeister.temp.pkcmmsrando.map.data;

public class Constant
{
	
	private String name;
	private int value;
	
	public Constant()
	{}
	
	public Constant(String name, int value)
	{
		this.name = name;
		this.value = value;
	}
	
	public String getName()
	{ return this.name; }
	
	public void setName(String name)
	{ this.name = name; }
	
	public int getValue()
	{ return this.value; }
	
	public void setValue(int value)
	{ this.value = value; }
	
}