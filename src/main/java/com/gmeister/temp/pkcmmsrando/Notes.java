package com.gmeister.temp.pkcmmsrando;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gmeister.temp.pkcmmsrando.io.DisassemblyReader;
import com.gmeister.temp.pkcmmsrando.io.DisassemblyWriter;
import com.gmeister.temp.pkcmmsrando.io.EmpiricalDataReader;
import com.gmeister.temp.pkcmmsrando.map.data.CollisionPermission;
import com.gmeister.temp.pkcmmsrando.map.data.Disassembly;
import com.gmeister.temp.pkcmmsrando.map.data.Flag;
import com.gmeister.temp.pkcmmsrando.map.data.Map;
import com.gmeister.temp.pkcmmsrando.map.data.Player;
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
	
	public static void randomiseWarpAreas(ArrayList<Map> maps, EmpiricalDataReader empReader, Randomiser rando) throws IOException, URISyntaxException
	{
		ArrayList<ArrayList<Warp>> warpGroups = new ArrayList<>();
		ArrayList<ArrayList<Map>> mapGroups = new ArrayList<>();
		ArrayList<String[]> mapGroupNamess = empReader.readVanillaMapGroups();
		for (String[] mapGroupNames : mapGroupNamess) mapGroups.add(Notes.getMapsByNames(maps, mapGroupNames));
		
		for (ArrayList<Map> mapGroup : mapGroups)
			for (Map map : mapGroup)
				for (Warp warp : map.getWarps()) if (warp.getDestination() != null)
					for (ArrayList<Map> mapGroup2 : mapGroups)
						if (!mapGroup.equals(mapGroup2) && mapGroup2.contains(warp.getDestination().getMap()))
		{
			ArrayList<Warp> group = null;
			
			groupTesting:
			for (ArrayList<Warp> testGroup : warpGroups) for (Warp testWarp : testGroup) if (warp.isAdjacentTo(testWarp))
			{
				group = testGroup;
				break groupTesting;
			}
			
			if (group == null)
			{
				group = new ArrayList<>();
				warpGroups.add(group);
			}
			
			group.add(warp);
		}
		
		rando.shuffleWarpGroups(warpGroups, false, true);
	}
	
	public static void buildWarpAreas(ArrayList<Map> maps, ArrayList<Flag> flags, EmpiricalDataReader empReader, Randomiser rando) throws FileNotFoundException, IOException, URISyntaxException
	{
		ArrayList<ArrayList<Warp>> warpGroups = new ArrayList<>();
		ArrayList<ArrayList<Map>> mapGroups = new ArrayList<>();
		ArrayList<String[]> mapGroupNamess = empReader.readVanillaMapGroups();
		for (String[] mapGroupNames : mapGroupNamess) mapGroups.add(Notes.getMapsByNames(maps, mapGroupNames));
		
		for (ArrayList<Map> mapGroup : mapGroups)
			for (Map map : mapGroup)
				for (Warp warp : map.getWarps())
					for (ArrayList<Map> mapGroup2 : mapGroups)
						if (!mapGroup.equals(mapGroup2) && warp.getDestination() != null &&
						mapGroup2.contains(warp.getDestination().getMap()))
		{
			ArrayList<Warp> group = null;
			
			groupTesting:
			for (ArrayList<Warp> testGroup : warpGroups) for (Warp testWarp : testGroup) if (warp.isAdjacentTo(testWarp))
			{
				group = testGroup;
				break groupTesting;
			}
			
			if (group == null)
			{
				group = new ArrayList<>();
				warpGroups.add(group);
			}
			
			group.add(warp);
		}
		
		HashMap<ArrayList<Warp>, ArrayList<ArrayList<Warp>>> accessibleGroups = new HashMap<>();
		for (ArrayList<Warp> group : warpGroups) accessibleGroups.put(group, new ArrayList<>());
		
		for (ArrayList<Map> mapGroup : mapGroups)
		{
			ArrayList<ArrayList<Warp>> mapGroupWarpGroups = new ArrayList<>();
			
			for (ArrayList<Warp> warpGroup : warpGroups) if (mapGroup.contains(warpGroup.get(0).getMap())) mapGroupWarpGroups.add(warpGroup);
			
			for (ArrayList<Warp> warpGroup : mapGroupWarpGroups)
			{
				Warp warp = warpGroup.get(0);
				HashMap<Map, boolean[][]> accessibleCollision = new HashMap<>();
				boolean[][] startCollision = new boolean[warp.getMap().getBlocks().getCollisionYCapacity()][warp.getMap().getBlocks().getCollisionXCapacity()];
				startCollision[warp.getY()][warp.getX()] = true;
				accessibleCollision.put(warp.getMap(), startCollision);
				
				ArrayList<Map> mapsToTest = new ArrayList<>(accessibleCollision.keySet());
				
				while (mapsToTest.size() > 0)
				{
					Map map = mapsToTest.remove(0);
					
					HashMap<Map, boolean[][]> accessibleCollisionFromMap = Player.getAccessibleCollision(map, accessibleCollision.get(map), flags);
					
					for (Map updatedMap : accessibleCollisionFromMap.keySet())
					{
						if (accessibleCollision.containsKey(updatedMap))
						{
							boolean[][] oldCollision = accessibleCollision.get(updatedMap);
							boolean[][] newCollision = accessibleCollisionFromMap.get(updatedMap);
							boolean changed = false;
							for (int y = 0; y < oldCollision.length; y++) for (int x = 0; x < oldCollision[y].length; x++) if (!oldCollision[y][x] && newCollision[y][x])
							{
								changed = true;
								oldCollision[y][x] = newCollision[y][x];
							}
							accessibleCollision.put(updatedMap, oldCollision);
							if (mapGroup.contains(updatedMap) && changed && map != updatedMap && !mapsToTest.contains(updatedMap)) mapsToTest.add(updatedMap);
						}
						else
						{
							accessibleCollision.put(updatedMap, accessibleCollisionFromMap.get(updatedMap));
							if (mapGroup.contains(updatedMap) && !mapsToTest.contains(updatedMap)) mapsToTest.add(updatedMap);
						}
					}
				}
				
				for (ArrayList<Warp> otherGroup : mapGroupWarpGroups)
				{
					Warp otherWarp = otherGroup.get(0);
					if (!warp.equals(otherWarp))
					{
						if ((accessibleCollision.keySet().contains(otherWarp.getMap()) &&
								accessibleCollision.get(otherWarp.getMap())[otherWarp.getY()][otherWarp.getX()])
								|| (accessibleCollision.keySet().contains(otherWarp.getDestination().getMap()) &&
										accessibleCollision.get(otherWarp.getDestination().getMap())[otherWarp.getDestination().getY()][otherWarp.getDestination().getX()]))
						{
							accessibleGroups.get(warpGroup).add(otherGroup);
							StringBuilder builder = new StringBuilder();
							builder.append(warp.getMap().getConstName()).append(" ");
							builder.append(warp.getX()).append(" ");
							builder.append(warp.getY());
							builder.append(" -> ");
							builder.append(otherWarp.getMap().getConstName()).append(" ");
							builder.append(otherWarp.getX()).append(" ");
							builder.append(otherWarp.getY());
							System.out.println(builder.toString());
						}
					}
				}
					
			}
		}
		
		for (ArrayList<Warp> group : warpGroups) for (Warp warp : group) if (warp.getMap().getConstName().equals("ROUTE_29"))
		{
			rando.buildWarpGroups(warpGroups, accessibleGroups, group);
			break;
		}
	}
	
	/*
	 * We're trying to get all maps to be visitable right?
	 * The way the new group rando works is by setting destinations directly instead of setting one destination to the destination of another
	 * Any group that cannot be accessed via this code cannot be made the destination of another group that cannot be accessed by the code
	 * Furthermore, this continues if all of the warps leading to a map cannot be accessed
	 * Oh, rather than all maps being accessed, make it so all warps can be accessed (groups, I mean)
	 * 
	 * Start at new bark
	 * Pick a random warp group
	 * Only place it down if the number of remaining accessible warps is greater than 1
	 * Have to build off of new bark though
	 */
	
	public static ArrayList<Map> getMapsByNames(ArrayList<Map> maps, String... constNames)
	{
		ArrayList<Map> selectedMaps = new ArrayList<>();
		ArrayList<String> constNamesList = new ArrayList<>(Arrays.asList(constNames));
		for (Map map : maps) if (constNamesList.contains(map.getConstName())) selectedMaps.add(map);
		return selectedMaps;
	}
	
	public static void randomiseWarps(ArrayList<Map> maps, Randomiser rando) throws IOException
	{
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
	}
	
	public static ArrayList<String> randomiseMusicPointers(DisassemblyReader reader, Randomiser rando) throws FileNotFoundException, IOException
	{
		return rando.shuffleMusicPointers(reader.readMusicPointers());
	}
	
	public static ArrayList<String> randomiseSFXPointers(DisassemblyReader reader, Randomiser rando) throws FileNotFoundException, IOException
	{
		return rando.shuffleSFXPointers(reader.readSFXPointers());
	}
	
	public static ArrayList<String> randomiseOverworldSpritePointers(DisassemblyReader reader, Randomiser rando) throws FileNotFoundException, IOException
	{
		return rando.shuffleOverworldSpritePointers(reader.readOverworldSpritePointers());
	}
	
	public static void main(String... args) throws IOException, URISyntaxException
	{
		List<String> argsList = new ArrayList<>(Arrays.asList(args));
		
		boolean help = false;
		boolean version = false;
		
		String disIn = null;
		String disOut = null;
		boolean warps = false;
		boolean warpAreas = false;
		boolean overworldSpritePointers = false;
		boolean musicPointers = false;
		boolean sfxPointers = false;
		boolean mapBlocks = false;
		
		while (argsList.size() > 0)
		{
			String arg = argsList.remove(0);
			
			switch (arg)
			{
				case "-h":
				case "--help":
				{
					help = true;
					break;
				}
				
				case "-v":
				case "--version":
				{
					version = true;
					break;
				}
				
				case "-d":
				case "--disassembly-in":
				{
					if (argsList.isEmpty())
					{
						System.err.println(arg + " requires a path argument.");
						return;
					}
					
					disIn = argsList.remove(0);
					break;
				}
				
				case "-D":
				case "--disassembly-out":
				{
					if (argsList.isEmpty())
					{
						System.err.println(arg + " requires a path argument.");
						return;
					}
					
					disOut = argsList.remove(0);
					break;
				}
				
				case "--warps":
				{
					warps = true;
					break;
				}
				
				case "--warp-areas":
				{
					warpAreas = true;
					break;
				}
				
				case "--overworld-sprite-pointers":
				{
					overworldSpritePointers = true;
					break;
				}
				
				case "--music-pointers":
				{
					musicPointers = true;
					break;
				}
				
				case "--sfx-pointers":
				{
					sfxPointers = true;
					break;
				}
				
				case "--map-blocks":
				{
					mapBlocks = true;
					break;
				}
			}
		}
		
		if (help)
		{
			System.out.println("pkc-mms-rando v0.0.2");
			System.out.println();
			System.out.println("requires the following arguments in order:");
			System.out.println("Path to a pret/pokecrystal style disassembly input folder, eg. \"C:/user/documents/pokecrystal-speedchoice-7.2/\"");
			System.out.println("Path to an output folder, eg. \"C:/user/documents/output/\"");
			System.out.println("true/false, whether to randomise music pointers (race safe)");
			System.out.println("true/false, whether to randomise SFX pointers (race safe)");
			System.out.println("true/false, whether to randomise warps (not race safe)");
			
			return;
		}
		
		if (version)
		{
			System.out.println("pkc-mms-rando v0.0.2");
			return;
		}
		
		DisassemblyReader disReader = null;
		DisassemblyWriter disWriter = null;
		
		if (disIn != null) disReader = new DisassemblyReader(Paths.get(disIn).normalize().toAbsolutePath().toFile());
		if (disOut != null) disWriter = new DisassemblyWriter(Paths.get(disOut).normalize().toAbsolutePath().toFile());
		
		EmpiricalDataReader empReader = new EmpiricalDataReader(null);
		Randomiser rando = new Randomiser();
		Disassembly disassembly = new Disassembly();
		ArrayList<Flag> allFlags = new ArrayList<>();
		
		if (warps || warpAreas || mapBlocks)
		{
			disassembly.setEngineFlags(disReader.readEngineFlags());
			disassembly.setEventFlags(disReader.readEventFlags());
			allFlags.addAll(disassembly.getEngineFlags());
			allFlags.addAll(disassembly.getEventFlags());
			
			ArrayList<CollisionPermission> perms = empReader.readCollisionPermissions(allFlags);
			
			disassembly.setCollisionConstants(empReader.readCollisionConstants(perms));
			disassembly.setTileSets(disReader.readTileSets(disassembly.getCollisionConstants()));
			disassembly.setMaps(disReader.readMaps(disassembly.getTileSets()));
		}
		
		if (warps && warpAreas) System.err.println("Error: choose one of --warps and --warp-areas");
		else if (warps)
		{
			if (disReader == null) System.out.println("Error: Randomisers require -d");
			Notes.randomiseWarps(disassembly.getMaps(), rando);
			
			if (disWriter != null) for (Map map : disassembly.getMaps())
			{
				map.writeWarpsToScript();
				disWriter.writeMapScript(map);
			}
		}
		else if (warpAreas)
		{
			if (disReader == null) System.out.println("Error: Randomisers require -d");
			Notes.randomiseWarpAreas(disassembly.getMaps(), empReader, rando);
			
			if (disWriter != null) for (Map map : disassembly.getMaps())
			{
				map.writeWarpsToScript();
				disWriter.writeMapScript(map);
			}
		}
		
		if (overworldSpritePointers)
		{
			if (disReader == null) System.out.println("Error: Randomisers require -d");
			ArrayList<String> pointers = Notes.randomiseOverworldSpritePointers(disReader, rando);
			if (disWriter != null) disWriter.writeOverworldSpritePointers(pointers);
		}
		
		if (musicPointers)
		{
			if (disReader == null) System.out.println("Error: Randomisers require -d");
			ArrayList<String> pointers = Notes.randomiseMusicPointers(disReader, rando);
			if (disWriter != null) disWriter.writeMusicPointers(pointers);
		}
		
		if (sfxPointers)
		{
			if (disReader == null) System.out.println("Error: Randomisers require -d");
			ArrayList<String> pointers = Notes.randomiseSFXPointers(disReader, rando);
			if (disWriter != null) disWriter.writeSFXPointers(pointers);
		}
		
		if (mapBlocks)
		{
			if (disReader == null) System.out.println("Error: Randomisers require -d");
			
			for (TileSet tileSet : disassembly.getTileSets()) tileSet.getBlockSet().updateCollGroups();
			
			for (Map map : disassembly.getMaps()) map.getBlocks().setBlocks(rando.randomiseBlocksByCollision(map.getTileSet().getBlockSet(), map.getBlocks().getBlocks()));
			
			if (disWriter != null) disWriter.writeAllMapBlocks(disassembly.getMaps());
		}
	}
	
}