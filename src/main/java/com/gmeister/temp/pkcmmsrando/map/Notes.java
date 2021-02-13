package com.gmeister.temp.pkcmmsrando.map;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

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
		
		Path mapPath = Paths.get("D:\\Users\\The_G_Meister\\Documents\\_MY SHIT\\Pokecrystal\\pokecrystal-speedchoice-master\\maps/Route29.blk");
		byte[] mapBytes = Files.readAllBytes(mapPath);
		Map map = new Map(30, 9);
		for (int y = 0; y < map.getYCapacity(); y++) for (int x = 0, i = 0; x < map.getXCapacity(); x++, i++)
		{
			System.out.println(mapBytes[i] & 0xFF);
			map.getBlocks()[y][x] = blockset.getBlocks().get(mapBytes[i] & 0xFF);
		}
		
		System.out.println();
	}
	
}