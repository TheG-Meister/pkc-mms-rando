package com.gmeister.temp.pkcmmsrando.map.data;

public class Block
{
	
	private byte[][] collision;
	
	public Block(byte tl, byte tr, byte bl, byte br)
	{ this.collision = new byte[][]{{tl, tr}, {bl, br}}; }
	
	public byte[][] getCollision()
	{ return this.collision; }
	
	public static Block[] makeSampleBlockset()
	{
		Block[] blocks = new Block[16];
		for (byte i = 0; i < blocks.length; i++) blocks[i] = new Block((byte) (i & 0b1), (byte) ((i >>> 1) & 0b1),
				(byte) ((i >>> 2) & 0b1), (byte) ((i >>> 3) & 0b1));
		return blocks;
	}
	
}