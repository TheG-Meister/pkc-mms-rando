package com.gmeister.temp.pkcmmsrando.map.data;

public class ObjectEvent
{
	
	/*
;\1: x: left to right, starts at 0
;\2: y: top to bottom, starts at 0
;\3: sprite: a SPRITE_* constant
;\4: movement function: a SPRITEMOVEDATA_* constant
;\5, \6: movement radius: x, y
;\7, \8: hour limits: h1, h2 (0-23)
;  * if h1 < h2, the object_event will only appear from h1 to h2
;  * if h1 > h2, the object_event will not appear from h2 to h1
;  * if h1 == h2, the object_event will always appear
;  * if h1 == -1, h2 is treated as a time-of-day value:
;    a combo of MORN, DAY, and/or NITE, or -1 to always appear
;\9: color: a PAL_NPC_* constant, or 0 for sprite default
;\<10>: function: a OBJECTTYPE_* constant
;\<11>: sight range: applies to OBJECTTYPE_TRAINER
;\<12>: script pointer
;\<13>: event flag: an EVENT_* constant, or -1 to always appear
	 */
	
	private OverworldPosition position;
	//This sprite will become invisible and intangible when this flag is set
	private Flag flag;
	private Constant moveData; 
	
	public ObjectEvent(OverworldPosition position, Flag flag, Constant moveData)
	{
		this.position = position;
		this.flag = flag;
		this.moveData = moveData;
	}
	
	public OverworldPosition getPosition()
	{ return this.position; }
	
	public void setPosition(OverworldPosition position)
	{ this.position = position; }
	
	public Flag getFlag()
	{ return this.flag; }
	
	public void setFlag(Flag flag)
	{ this.flag = flag; }

	public Constant getMoveData()
	{ return this.moveData; }

	public void setMoveData(Constant moveData)
	{ this.moveData = moveData; }
	
}
