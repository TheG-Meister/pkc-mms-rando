package com.gmeister.temp.pkcmmsrando;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gmeister.temp.pkcmmsrando.io.DisassemblyReader;
import com.gmeister.temp.pkcmmsrando.io.DisassemblyWriter;
import com.gmeister.temp.pkcmmsrando.io.EmpiricalDataReader;
import com.gmeister.temp.pkcmmsrando.map.data.CollisionConstant;
import com.gmeister.temp.pkcmmsrando.map.data.CollisionPermission;
import com.gmeister.temp.pkcmmsrando.map.data.Flag;
import com.gmeister.temp.pkcmmsrando.map.data.Map;
import com.gmeister.temp.pkcmmsrando.map.data.TileSet;
import com.gmeister.temp.pkcmmsrando.map.data.Warp;

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
	
	public static void randomiseWarpAreas(ArrayList<Map> maps) throws IOException
	{
		ArrayList<Warp> warps = new ArrayList<>();
		ArrayList<ArrayList<Map>> mapGroups = new ArrayList<>();
		ArrayList<String[]> mapGroupNamess = Notes.readMapGroups();
		for (String[] mapGroupNames : mapGroupNamess) mapGroups.add(Notes.getMapsByNames(maps, mapGroupNames));
		
		for (ArrayList<Map> mapGroup : mapGroups)
			for (Map map : mapGroup)
				for (Warp warp : map.getWarps())
					for (ArrayList<Map> mapGroup2 : mapGroups)
						if (!mapGroup.equals(mapGroup2) && mapGroup2.contains(warp.getDestination().getMap()))
			warps.add(warp);
		
		new Randomiser().shuffleWarpDestinations(warps, false, true, false);
	}
	
	public static ArrayList<Map> getMapsByNames(ArrayList<Map> maps, String... constNames)
	{
		ArrayList<Map> selectedMaps = new ArrayList<>();
		ArrayList<String> constNamesList = new ArrayList<>(Arrays.asList(constNames));
		for (Map map : maps) if (constNamesList.contains(map.getConstName())) selectedMaps.add(map);
		return selectedMaps;
	}
	
	public static ArrayList<String[]> readMapGroups() throws IOException
	{
		ArrayList<String[]> groups = new ArrayList<>();
		ArrayList<String> lines = new ArrayList<>(Files.readAllLines(Paths.get("E:/grant/documents/.my-stuff/Pokecrystal/pkc-mms-rando/map/vanilla-map-groups.tsv")));
		for (String line : lines) groups.add(line.split("\t"));
		return groups;
	}
	
	public static void warpRando() throws IOException
	{
		File inFolder = Paths.get(
				"E:/grant/documents/.my-stuff/Pokecrystal/pokecrystal-speedchoice-7.2/").toFile();
		File outFolder = Paths.get(
				"E:/grant/documents/.my-stuff/Pokecrystal/pkc-mms-rando/patches/21-08-21-1/pokecrystal-speedchoice/").toFile();
		
		DisassemblyReader disReader = new DisassemblyReader(inFolder);
		DisassemblyWriter disWriter = new DisassemblyWriter(outFolder);
		Randomiser rando = new Randomiser();
		
		ArrayList<String> musicScript = disReader.readMusicPointers();
		ArrayList<String> shuffledScript = rando.shuffleMusicPointers(musicScript);
		disWriter.writeMusicPointers(shuffledScript);
		
		ArrayList<CollisionConstant> collisionConstants = disReader.readCollisionConstants();
		ArrayList<TileSet> tileSets = disReader.readTileSets(collisionConstants);
		for (TileSet tileSet : tileSets) tileSet.getBlockSet().updateCollGroups();
		ArrayList<Map> maps = disReader.readMaps(tileSets);
		
		/*Notes.randomiseWarpAreas(maps);
		
		for (Map map : maps)
		{
			//System.out.println(map.getConstName());
			map.writeWarpsToScript();
			io.writeMapScript(map);
		}
		if (true) return;*/
		
		ArrayList<Warp> warps = new ArrayList<>();
		for (Map map : maps)
		{
			/*
			//Force the badge checks before E4 and Red by linking certain warps
			if (map.getConstName().equals("VICTORY_ROAD_GATE"))
			{
				Map route22 = map.getWarps().get(0).getDestination().getMap();
				
				//Link the warps to Route 22 to each other
				map.getWarps().get(0).setDestination(map.getWarps().get(1));
				map.getWarps().get(1).setDestination(map.getWarps().get(0));
				
				//Link route 22 to itself
				route22.getWarps().get(0).setDestination(route22.getWarps().get(0));
				
				for (Map indigoPlateau : maps) if (indigoPlateau.getConstName().equals("INDIGO_PLATEAU_POKECENTER_1F"))
				{
					Map victoryRoad = map.getWarps().get(4).getDestination().getMap();
					Map route23 = indigoPlateau.getWarps().get(0).getDestination().getMap();
					
					//Link victory road and indigo plateau
					map.getWarps().get(4).setDestination(indigoPlateau.getWarps().get(0));
					map.getWarps().get(5).setDestination(indigoPlateau.getWarps().get(1));
					
					indigoPlateau.getWarps().get(0).setDestination(map.getWarps().get(4));
					indigoPlateau.getWarps().get(1).setDestination(map.getWarps().get(5));
					
					//Link route 23 and victory road (creates a cycle that should get un-done by the randomiser)
					victoryRoad.getWarps().get(0).setDestination(route23.getWarps().get(0));
					
					route23.getWarps().get(0).setDestination(victoryRoad.getWarps().get(0));
					route23.getWarps().get(1).setDestination(victoryRoad.getWarps().get(0));
				}
				
				for (Map redsRoom : maps) if (redsRoom.getConstName().equals("SILVER_CAVE_ROOM_3"))
				{
					Map route28 = map.getWarps().get(6).getDestination().getMap();
					Map silverCaveRoom2 = redsRoom.getWarps().get(0).getDestination().getMap();
					
					//Link victory road gate and Red's room
					map.getWarps().get(6).setDestination(redsRoom.getWarps().get(0));
					map.getWarps().get(7).setDestination(redsRoom.getWarps().get(0));
					
					redsRoom.getWarps().get(0).setDestination(map.getWarps().get(6));
					
					//Link silver cave room 2 and route 28 (creates a cycle that should get un-done by the randomiser)
					route28.getWarps().get(1).setDestination(silverCaveRoom2.getWarps().get(1));
					
					silverCaveRoom2.getWarps().get(1).setDestination(route28.getWarps().get(1));
				}
			}*/
			
			//ignore all beta maps
			if (map.getConstName().contains("BETA")) continue;
			
			switch (map.getConstName())
			{
				//Unrandomise new bark town to force the player to get a pokemon
				case "NEW_BARK_TOWN":
				case "ELMS_LAB":
				case "PLAYERS_HOUSE_1F":
				case "PLAYERS_HOUSE_2F":
				case "PLAYERS_NEIGHBORS_HOUSE":
				case "ELMS_HOUSE":
				
				//unrandomise most battle tower areas as they are all cutscenes
				case "BATTLE_TOWER_BATTLE_ROOM":
				case "BATTLE_TOWER_ELEVATOR":
				case "BATTLE_TOWER_HALLWAY":
				
				//unrandomise indigo plateau
				case "INDIGO_PLATEAU_POKECENTER_1F":
				case "WILLS_ROOM":
				case "KOGAS_ROOM":
				case "BRUNOS_ROOM":
				case "KARENS_ROOM":
				case "LANCES_ROOM":
				case "HALL_OF_FAME":
				
				//Unrandomise red's room from the changes above
				//case "SILVER_CAVE_ROOM_3":
				
				//Unrandomise the cable club and its rooms
				case "POKECENTER_2F":
				case "TRADE_CENTER":
				case "COLOSSEUM":
				case "TIME_CAPSULE":
				case "MOBILE_TRADE_ROOM":
				case "MOBILE_BATTLE_ROOM":
					continue;
			}
			
			for (Warp warp : map.getWarps())
			{
				if (warp.getDestination() != null)
				{
					//String mapName = warp.getMap().getConstName();
					String destMapName = warp.getDestination().getMap().getConstName();
					//Remove warps that lead to some of the above maps
					if (destMapName.contains("BETA")) continue;
					
					switch (destMapName)
					{
						case "BATTLE_TOWER_ELEVATOR":
						case "POKECENTER_2F":
						case "INDIGO_PLATEAU_POKECENTER_1F":
						//case "SILVER_CAVE_ROOM_3":
							continue;
					}
					
					//Unrandomise the changed route22 and Victory Road gate warps
					//if (mapName.equals("VICTORY_ROAD_GATE") && destMapName.equals("VICTORY_ROAD_GATE")) continue;
					//if (warp.getMap().getConstName().equals("ROUTE_22") && warp.getDestination().getMap().getConstName().equals("ROUTE_22")) continue;
				}
				
				warps.add(warp);
			}
		}
		
		rando.shuffleWarpDestinations(warps, false, true, true);
		
		for (Map map : maps)
		{
			map.writeWarpsToScript();
			disWriter.writeMapScript(map);
		}
	}
	
	public static void main(String... args) throws IOException, URISyntaxException
	{
		File inFolder = Paths.get(
				"E:/grant/documents/.my-stuff/Pokecrystal/pokecrystal-speedchoice-7.2/").toFile();
		File outFolder = Paths.get(
				"E:/grant/documents/.my-stuff/Pokecrystal/pkc-mms-rando/patches/21-08-21-1/pokecrystal-speedchoice/").toFile();
		
		DisassemblyReader disReader = new DisassemblyReader(inFolder);
		DisassemblyWriter disWriter = new DisassemblyWriter(outFolder);
		EmpiricalDataReader empReader = new EmpiricalDataReader(null);
		Randomiser rando = new Randomiser();
		
		ArrayList<Flag> flags = new ArrayList<>();
		flags.addAll(disReader.readEngineFlags());
		flags.addAll(disReader.readEventFlags());
		
		ArrayList<CollisionPermission> perms = empReader.readCollisionPermissions(flags);
		ArrayList<CollisionConstant> collision = empReader.readCollisionConstants(perms);
		
		ArrayList<TileSet> tileSets = disReader.readTileSets(collision);
		for (TileSet tileSet : tileSets) tileSet.getBlockSet().updateCollGroups();
		ArrayList<Map> maps = disReader.readMaps(tileSets);
		
		for (Map map : maps) if (map.getConstName().equals("TOHJO_FALLS"))
		{
			System.out.println(map.getConstName());
			System.out.println(map.testMovement(13,15,10,14,null));
			System.out.println(map.testMovement(13,15,2,6,null));
			System.out.println(map.testMovement(13,15,2,6,flags));
			System.out.println(map.testMovement(13,15,25,15,null));
			System.out.println(map.testMovement(13,15,25,15,flags));
			break;
		}
	}
	
}