package com.gmeister.temp.pkcmmsrando.map.data;

/**
 * Also known as a coord event in the disassembly
 * @author The_G_Meister
 *
 */
public class PositionEvent
{
	
	private OverworldPosition position;
	
	public PositionEvent(OverworldPosition position)
	{ this.position = position; }
	
	public OverworldPosition getPosition()
	{ return this.position; }
	
	public void setPosition(OverworldPosition position)
	{ this.position = position; }
	
}
