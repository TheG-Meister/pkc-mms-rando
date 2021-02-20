package com.gmeister.temp.pkcmmsrando.map;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

import com.gmeister.temp.maps.BooleanMap;
import com.gmeister.temp.maps.ReferencePoint;
import com.gmeister.temp.pkcmmsrando.map.data.Block;
import com.gmeister.temp.pkcmmsrando.map.data.Blockset;
import com.gmeister.temp.pkcmmsrando.map.data.Constant;
import com.gmeister.temp.pkcmmsrando.map.data.Map;

public class Notes
{
	
	public static void main(String... args) throws FileNotFoundException, IOException
	{
		File constantsFile = Paths.get("D:\\Users\\The_G_Meister\\Documents\\_MY SHIT\\Pokecrystal\\pokecrystal-speedchoice-master\\constants/collision_constants.asm").toFile();
		ArrayList<Constant> constants = Constant.importConstants(constantsFile);
		
		File blocksetFile = Paths.get("D:\\Users\\The_G_Meister\\Documents\\_MY SHIT\\Pokecrystal\\pokecrystal-speedchoice-master\\data/tilesets/johto_collision.asm").toFile();
		Blockset blockset = Blockset.importBlockset(blocksetFile, constants);
		//for (int i = 0; i < blockset.getBlocks().size(); i++) System.out.println(Arrays.deepToString(blockset.getBlocks().get(i).getCollision()));
		
		Map map = new Map(30, 9);
		/*
		Path mapPath = Paths.get("D:\\Users\\The_G_Meister\\Documents\\_MY SHIT\\Pokecrystal\\pokecrystal-speedchoice-master\\maps/Route29.blk");
		byte[] mapBytes = Files.readAllBytes(mapPath);
		for (int y = 0; y < map.getYCapacity(); y++) for (int x = 0, i = 0; x < map.getXCapacity(); x++, i++)
		{
			//System.out.println(mapBytes[i] & 0xFF);
			map.getBlocks()[y][x] = blockset.getBlocks().get(mapBytes[i] & 0xFF);
		}
		*/
		
		Block path = blockset.getBlocks().get(0x01);
		Block grass = blockset.getBlocks().get(0x02);
		Block tallGrass = blockset.getBlocks().get(0x03);
		Block trees = blockset.getBlocks().get(0x05);
		Block water = blockset.getBlocks().get(0x35);
		
		for (int y = 0; y < map.getYCapacity(); y++) for (int x = 0; x < map.getXCapacity(); x++) map.getBlocks()[y][x] = trees;
		for (int y = 1; y < 8; y++) for (int x = 1; x < 8; x++) map.getBlocks()[y][x] = tallGrass;
		
		BooleanMap ellipse = BooleanMapGenerators.makeEllipse(19, 7, true);
		for (int y = 0; y < ellipse.getYCapacity(); y++) for (int x = 0; x < ellipse.getXCapacity(); x++) if (ellipse.getAt(x, y)) map.getBlocks()[y + 1][x + 9] = grass;
		BooleanMap lake = BooleanMapGenerators.makeBlob(20, new Random());
		for (int y = 0; y < lake.getYCapacity(); y++) for (int x = 0; x < lake.getXCapacity(); x++) if (lake.getAt(x, y)) map.getBlocks()[y][x + 15] = water;
		BooleanMap pathMap = BooleanMapGenerators.makeSARWBetween(new ReferencePoint(0, 3), new ReferencePoint(29, 4), new BooleanMap(map.getXCapacity(), map.getYCapacity(), false), new Random());
		for (int y = 0; y < pathMap.getYCapacity(); y++) for (int x = 0; x < pathMap.getXCapacity(); x++) if (pathMap.getAt(x, y) && map.getBlocks()[y][x] != water) map.getBlocks()[y][x] = path;
		
		File outputFile = Paths.get("D:\\Users\\The_G_Meister\\Documents\\_MY SHIT\\Pokecrystal\\custom maps/maps/test.blk").toFile();
		ByteBuffer buffer = ByteBuffer.allocate(map.getYCapacity() * map.getXCapacity());
		for (int y = 0; y < map.getYCapacity(); y++) for (int x = 0; x < map.getXCapacity(); x++) buffer.put((byte) blockset.getBlocks().indexOf(map.getBlocks()[y][x]));
		
		buffer.flip();
		byte[] outputArray = new byte[buffer.remaining()];
		buffer.get(outputArray);
		
		try (FileOutputStream stream = new FileOutputStream(outputFile))
		{
			stream.write(outputArray);
		}
	}
	
}