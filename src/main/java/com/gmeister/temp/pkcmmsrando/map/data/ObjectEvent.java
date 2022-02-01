package com.gmeister.temp.pkcmmsrando.map.data;

public class ObjectEvent
{
	
	private OverworldPosition position;
	//This sprite will become invisible and intangible when this flag is set
	private Flag flag;
	
	public ObjectEvent(OverworldPosition position, Flag flag)
	{
		this.position = position;
		this.flag = flag;
	}
	
	public OverworldPosition getPosition()
	{ return this.position; }
	
	public void setPosition(OverworldPosition position)
	{ this.position = position; }
	
	public Flag getFlag()
	{ return this.flag; }
	
	public void setFlag(Flag flag)
	{ this.flag = flag; }
	
}
