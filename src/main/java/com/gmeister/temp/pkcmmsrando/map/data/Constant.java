package com.gmeister.temp.pkcmmsrando.map.data;

public class Constant
{
	
	private String name;
	private byte value;
	
	public Constant(String name, byte value)
	{
		super();
		this.name = name;
		this.value = value;
	}
	
	public String getName()
	{ return this.name; }
	
	public void setName(String name)
	{ this.name = name; }
	
	public byte getValue()
	{ return this.value; }
	
	public void setValue(byte value)
	{ this.value = value; }
	
}