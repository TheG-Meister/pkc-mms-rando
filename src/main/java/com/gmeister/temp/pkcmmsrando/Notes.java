package com.gmeister.temp.pkcmmsrando;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gmeister.temp.pkcmmsrando.map.data.Constant;
import com.gmeister.temp.pkcmmsrando.map.data.Map;
import com.gmeister.temp.pkcmmsrando.map.data.TileSet;

public class Notes
{
	
	public static void fillAllRoutesWithGrass() throws IOException
	{
		Pattern p = Pattern.compile("^\\D+(\\d+).*");
		File in = Paths.get("D:\\Users\\The_G_Meister\\Documents\\_MY SHIT\\Pokecrystal/Map rando\\Routes\\").toFile();
		File out = Paths.get(
				"D:/Users/The_G_Meister/Documents/_MY SHIT/Pokecrystal/Rom patch test/pokecrystal-speedchoice").toFile();
		
		byte[] kantoBlocks = Files.readAllBytes(Paths.get(
				"D:/Users/The_G_Meister/Documents/_MY SHIT/Pokecrystal/pokecrystal-speedchoice-master/data/tilesets/kanto_metatiles.bin"));
		byte[][] kantoBlockTiles = new byte[Math.floorDiv(kantoBlocks.length, 16)][];
		for (int block = 0; block < kantoBlockTiles.length; block++)
			kantoBlockTiles[block] = Arrays.copyOfRange(kantoBlocks, block * 16, (block + 1) * 16);
		
		List<Byte> kantoBuildingTiles = new ArrayList<Byte>(Arrays.asList(new Byte[] {5, 6, 7, 8, 9, 10, 11, 12, 15, 21, 22, 23, 24, 25, 26, 27, 28, 29, 31, 37, 38, 40, 41, 47, 50, 56, 60, 63, 66, 67, 68, 69, 75, 76, 77, 78, 79, 83, 91, 92, 93, 95}));
		List<Byte> kantoBuildingBlocks = new ArrayList<>();
		for (int i = 0; i < kantoBlockTiles.length; i++)
			for (int j = 0; j < 16; j++) if (kantoBuildingTiles.contains(kantoBlockTiles[i][j]))
		{
			kantoBuildingBlocks.add((byte) i);
			break;
		}
		
		byte[] johtoBlocks = Files.readAllBytes(Paths.get(
				"D:/Users/The_G_Meister/Documents/_MY SHIT/Pokecrystal/pokecrystal-speedchoice-master/data/tilesets/johto_metatiles.bin"));
		byte[][] johtoBlockTiles = new byte[Math.floorDiv(johtoBlocks.length, 16)][];
		for (int block = 0; block < johtoBlockTiles.length; block++)
			johtoBlockTiles[block] = Arrays.copyOfRange(johtoBlocks, block * 16, (block + 1) * 16);
		
		List<Byte> johtoBuildingTiles = Arrays.asList(Byte.valueOf((byte) 1), (byte) 2, (byte) 7, (byte) 8, (byte) 9, (byte) 10, (byte) 11,
				(byte) 12, (byte) 13, (byte) 14, (byte) 15, (byte) 16, (byte) 17, (byte) 18, (byte) 22, (byte) 24,
				(byte) 25, (byte) 26, (byte) 27, (byte) 28, (byte) 34, (byte) 35, (byte) 36, (byte) 37, (byte) 38,
				(byte) 39, (byte) 40, (byte) 41, (byte) 42, (byte) 48, (byte) 49, (byte) 52, (byte) 53, (byte) 55,
				(byte) 56, (byte) 57, (byte) 58, (byte) 64, (byte) 65, (byte) 68, (byte) 80, (byte) 81, (byte) 82,
				(byte) 83, (byte) 84, (byte) 85, (byte) 128, (byte) 129, (byte) 130, (byte) 131, (byte) 132, (byte) 133,
				(byte) 134, (byte) 135, (byte) 136, (byte) 137, (byte) 138, (byte) 139, (byte) 140, (byte) 141,
				(byte) 142, (byte) 143, (byte) 144, (byte) 145, (byte) 146, (byte) 147, (byte) 148, (byte) 150,
				(byte) 151, (byte) 152, (byte) 153, (byte) 154, (byte) 155, (byte) 156, (byte) 157, (byte) 160,
				(byte) 161, (byte) 162, (byte) 163, (byte) 164, (byte) 165, (byte) 166, (byte) 167, (byte) 168,
				(byte) 169, (byte) 170, (byte) 171, (byte) 172, (byte) 173, (byte) 174, (byte) 175, (byte) 176,
				(byte) 177, (byte) 178, (byte) 179, (byte) 180, (byte) 181, (byte) 182, (byte) 183, (byte) 184,
				(byte) 185, (byte) 186, (byte) 187, (byte) 188, (byte) 189, (byte) 190, (byte) 191);
		List<Byte> johtoBuildingBlocks = new ArrayList<>();
		for (int i = 0; i < johtoBlockTiles.length; i++)
			for (int j = 0; j < 16; j++) if (johtoBuildingTiles.contains(johtoBlockTiles[i][j]))
		{
			johtoBuildingBlocks.add((byte) i);
			break;
		}
		
		List<Integer> waterRouteNums = Arrays.asList(19, 21, 27, 40, 41);
		List<Integer> vanillaRouteNums = Arrays.asList(20, 23, 26, 28);
		byte johtoGrass = 3;
		byte kantoGrass = 11;
		byte johtoWater = 53;
		byte kantoWater = 67;
		
		for (File f : in.listFiles())
		{
			byte[] b = Files.readAllBytes(f.toPath());
			Matcher m = p.matcher(f.getName());
			m.find();
			int routeNum = Integer.parseInt(m.group(1));
			
			if (waterRouteNums.contains(routeNum))
			{
				if (routeNum <= 25 || routeNum == 28)
				{
					for (int i = 0; i < b.length; i++) if (!kantoBuildingBlocks.contains(b[i])) b[i] = kantoWater;
				}
				else for (int i = 0; i < b.length; i++) if (!johtoBuildingBlocks.contains(b[i])) b[i] = johtoWater;
			}
			else if (!vanillaRouteNums.contains(routeNum)) if (routeNum <= 25 || routeNum == 28)
			{
				for (int i = 0; i < b.length; i++) if (!kantoBuildingBlocks.contains(b[i])) b[i] = kantoGrass;
			}
			else for (int i = 0; i < b.length; i++) if (!johtoBuildingBlocks.contains(b[i])) b[i] = johtoGrass;
			
			File outputFile = out.toPath().resolve("maps/").resolve(f.getName()).toFile();
			try (FileOutputStream stream = new FileOutputStream(outputFile))
			{
				stream.write(b);
			}
		}
	}
	
	public static void main(String... args) throws IOException
	{
		File inFolder = Paths.get(
				"D:/Users/The_G_Meister/Documents/_MY SHIT/Pokecrystal/pokecrystal-speedchoice-master/").toFile();
		File outFolder = Paths.get(
				"D:/Users/The_G_Meister/Documents/_MY SHIT/Pokecrystal/pkc-mms-rando/patches/share-block/pokecrystal-speedchoice/").toFile();
		
		DisassemblyIO io = new DisassemblyIO(inFolder, outFolder);
		Randomiser rando = new Randomiser();
		
		//ArrayList<String> musicScript = io.readMusicPointers();
		//ArrayList<String> shuffledScript = rando.shuffleMusicPointers(musicScript);
		//io.writeMusicPointers(shuffledScript);
		
		ArrayList<Constant> collisionConstants = io.readCollisionConstants();
		ArrayList<TileSet> tileSets = io.readTileSets(collisionConstants);
		for (TileSet tileSet : tileSets) tileSet.getBlockSet().updateCollGroups();
		ArrayList<Map> maps = io.readMaps(tileSets);
		
		for (Map map : maps)
		{
			map.getBlocks().setBlocks(rando.randomiseBlocksByCollision(map.getTileSet().getBlockSet(), map.getBlocks().getBlocks()));
			io.writeMapBlocks(map.getBlocks(), map.getTileSet().getBlockSet());
		}
	}
	
}