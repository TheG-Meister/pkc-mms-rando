package com.gmeister.temp.pkcmaprando;

public class Block
{
	
	private byte[][] collision;
	
	public Block(byte tl, byte tr, byte bl, byte br)
	{
		this.collision = new byte[][] {{tl, tr}, {bl, br}};
	}
	
	public byte[][] getCollision()
	{
		return this.collision;
	}
	
}