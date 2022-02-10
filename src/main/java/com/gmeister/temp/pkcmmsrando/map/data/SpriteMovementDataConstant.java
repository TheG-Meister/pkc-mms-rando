package com.gmeister.temp.pkcmmsrando.map.data;

public class SpriteMovementDataConstant extends Constant
{
	
	private boolean big;
	
	public SpriteMovementDataConstant()
	{ super(); }
	
	public SpriteMovementDataConstant(String name, int value, boolean big)
	{
		super(name, value);
		this.big = big;
	}
	
	public boolean isBig()
	{ return this.big; }
	
	public void setBig(boolean big)
	{ this.big = big; }
	
}
