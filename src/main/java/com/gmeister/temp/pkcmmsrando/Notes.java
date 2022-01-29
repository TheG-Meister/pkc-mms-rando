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
import java.util.stream.Collectors;

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
import com.gmeister.temp.pkcmmsrando.map.data.WarpNetwork;
import com.gmeister.temp.pkcmmsrando.rando.Randomiser;

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
	
	public static void randomiseWarpAreas(List<Map> maps, EmpiricalDataReader empReader, Randomiser rando) throws IOException, URISyntaxException
	{
		List<List<Warp>> warpGroups = new ArrayList<>();
		List<List<Map>> mapGroups = new ArrayList<>();
		List<String[]> mapGroupNamess = empReader.readVanillaMapGroups();
		for (String[] mapGroupNames : mapGroupNamess) mapGroups.add(Notes.getMapsByNames(maps, mapGroupNames));
		
		for (List<Map> mapGroup : mapGroups)
			for (Map map : mapGroup)
				for (Warp warp : map.getWarps()) if (warp.getDestination() != null)
					for (List<Map> mapGroup2 : mapGroups)
						if (!mapGroup.equals(mapGroup2) && mapGroup2.contains(warp.getDestination().getMap()))
		{
			List<Warp> group = null;
			
			groupTesting:
			for (List<Warp> testGroup : warpGroups) for (Warp testWarp : testGroup) if (warp.isPairedWith(testWarp))
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
	
	public static java.util.Map<Warp, Warp> buildWarpAreas(List<Map> maps, List<Flag> flags, EmpiricalDataReader empReader, Randomiser rando) throws FileNotFoundException, IOException, URISyntaxException
	{
		List<List<Warp>> warpGroups = new ArrayList<>();
		List<List<Map>> mapGroups = new ArrayList<>();
		List<String[]> mapGroupNamess = empReader.readVanillaMapGroups();
		for (String[] mapGroupNames : mapGroupNamess) mapGroups.add(Notes.getMapsByNames(maps, mapGroupNames));
		
		for (List<Map> mapGroup : mapGroups)
			for (Map map : mapGroup)
				for (Warp warp : map.getWarps())
					for (List<Map> mapGroup2 : mapGroups)
						if (!mapGroup.equals(mapGroup2) && warp.getDestination() != null &&
						mapGroup2.contains(warp.getDestination().getMap()))
		{
			List<Warp> group = null;
			
			groupTesting:
			for (List<Warp> testGroup : warpGroups) for (Warp testWarp : testGroup) if (warp.isPairedWith(testWarp))
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
		
		java.util.Map<List<Warp>, List<List<Warp>>> accessibleGroups = new HashMap<>();
		for (List<Warp> group : warpGroups) accessibleGroups.put(group, new ArrayList<>());
		
		for (List<Map> mapGroup : mapGroups)
		{
			List<List<Warp>> mapGroupWarpGroups = new ArrayList<>();
			
			for (List<Warp> warpGroup : warpGroups) if (mapGroup.contains(warpGroup.get(0).getMap())) mapGroupWarpGroups.add(warpGroup);
			
			for (List<Warp> warpGroup : mapGroupWarpGroups)
			{
				Warp warp = warpGroup.get(0);
				java.util.Map<Map, boolean[][]> accessibleCollision = new HashMap<>();
				boolean[][] startCollision = new boolean[warp.getMap().getBlocks().getCollisionYCapacity()][warp.getMap().getBlocks().getCollisionXCapacity()];
				startCollision[warp.getY()][warp.getX()] = true;
				accessibleCollision.put(warp.getMap(), startCollision);
				
				List<Map> mapsToTest = new ArrayList<>(accessibleCollision.keySet());
				
				while (mapsToTest.size() > 0)
				{
					Map map = mapsToTest.remove(0);
					
					java.util.Map<Map, boolean[][]> accessibleCollisionFromMap = Player.getAccessibleCollision(map, accessibleCollision.get(map), new ArrayList<>());
					
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
				
				for (List<Warp> otherGroup : mapGroupWarpGroups)
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
						}
					}
				}
					
			}
		}
		
		//Manually add warps from and to the north cycling road gatehouse
		Map route16 = maps.stream().filter(m -> m.getConstName().equals("ROUTE_16")).findFirst().orElseThrow();
		List<Warp> route16ToGate = new ArrayList<>(route16.getWarps().stream()
				.filter(w -> w.getPosition().getX() == 14 && (w.getPosition().getY() == 6 || w.getPosition().getY() == 7))
				.collect(Collectors.toList()));
		List<Warp> route7GateToSaffron = warpGroups.stream().filter(g -> g.stream().anyMatch(w -> w.getMap().getConstName().equals("ROUTE_7_SAFFRON_GATE"))).findFirst().orElseThrow();
		
		warpGroups.add(route16ToGate);
		accessibleGroups.get(route7GateToSaffron).add(route16ToGate);
		accessibleGroups.put(route16ToGate, new ArrayList<>(Arrays.asList(route7GateToSaffron)));
		
		Map route16Gate = maps.stream().filter(m -> m.getConstName().equals("ROUTE_16_GATE")).findFirst().orElseThrow();
		List<Warp> route16GateToRoute = new ArrayList<>(route16Gate.getWarps().stream()
				.filter(w -> w.getPosition().getX() == 9 && (w.getPosition().getY() == 4 || w.getPosition().getY() == 5))
				.collect(Collectors.toList()));
		List<Warp> route17GateToRoute = warpGroups.stream().filter(g -> g.stream().anyMatch(w -> w.getMap().getConstName().equals("ROUTE_17_ROUTE_18_GATE"))).findFirst().orElseThrow();
		
		warpGroups.add(route16GateToRoute);
		accessibleGroups.get(route17GateToRoute).add(route16GateToRoute);
		accessibleGroups.put(route16GateToRoute, new ArrayList<>(Arrays.asList(route17GateToRoute)));
		
		WarpNetwork network = new WarpNetwork(accessibleGroups);
		
		return rando.buildWarpGroups(network, false, true, true);
	}
	
	public static List<Map> getMapsByNames(List<Map> maps, String... constNames)
	{
		List<Map> selectedMaps = new ArrayList<>();
		List<String> constNamesList = new ArrayList<>(Arrays.asList(constNames));
		for (Map map : maps) if (constNamesList.contains(map.getConstName())) selectedMaps.add(map);
		return selectedMaps;
	}
	
	public static java.util.Map<Warp, Warp> randomiseWarps(ArrayList<Map> maps, Randomiser rando) throws IOException
	{
		//Collect a bunch of maps to manually edit warps
		Map victoryRoadGate = maps.stream().filter(m -> m.getConstName().equals("VICTORY_ROAD_GATE")).findFirst().orElseThrow();
		Map victoryRoad = maps.stream().filter(m -> m.getConstName().equals("VICTORY_ROAD")).findFirst().orElseThrow();
		Map route22 = maps.stream().filter(m -> m.getConstName().equals("ROUTE_22")).findFirst().orElseThrow();
		Map route23 = maps.stream().filter(m -> m.getConstName().equals("ROUTE_23")).findFirst().orElseThrow();
		Map route28 = maps.stream().filter(m -> m.getConstName().equals("ROUTE_28")).findFirst().orElseThrow();
		Map indigoPlateau = maps.stream().filter(m -> m.getConstName().equals("INDIGO_PLATEAU_POKECENTER_1F")).findFirst().orElseThrow();
		Map silverCaveRoom2 = maps.stream().filter(m -> m.getConstName().equals("SILVER_CAVE_ROOM_2")).findFirst().orElseThrow();
		Map redsRoom = maps.stream().filter(m -> m.getConstName().equals("SILVER_CAVE_ROOM_3")).findFirst().orElseThrow();
		
		//Link all the warps on the right side of victory road gate to themselves
		victoryRoadGate.getWarps().get(0).setDestination(victoryRoadGate.getWarps().get(0));
		victoryRoadGate.getWarps().get(1).setDestination(victoryRoadGate.getWarps().get(1));
		route22.getWarps().get(0).setDestination(route22.getWarps().get(0));
		
		//Link victory road and indigo plateau
		victoryRoadGate.getWarps().get(4).setDestination(indigoPlateau.getWarps().get(0));
		victoryRoadGate.getWarps().get(5).setDestination(indigoPlateau.getWarps().get(1));
		indigoPlateau.getWarps().get(0).setDestination(victoryRoadGate.getWarps().get(4));
		indigoPlateau.getWarps().get(1).setDestination(victoryRoadGate.getWarps().get(5));
		
		//Link route 23 and victory road (creates a cycle that should get un-done by the randomiser)
		victoryRoad.getWarps().get(0).setDestination(route23.getWarps().get(0));
		route23.getWarps().get(0).setDestination(victoryRoad.getWarps().get(0));
		route23.getWarps().get(1).setDestination(victoryRoad.getWarps().get(0));
		
		//Link victory road gate and Red's room
		victoryRoadGate.getWarps().get(6).setDestination(redsRoom.getWarps().get(0));
		victoryRoadGate.getWarps().get(7).setDestination(redsRoom.getWarps().get(0));
		redsRoom.getWarps().get(0).setDestination(victoryRoadGate.getWarps().get(6));
		
		//Link silver cave room 2 and route 28 (creates a cycle that should get un-done by the randomiser)
		route28.getWarps().get(1).setDestination(silverCaveRoom2.getWarps().get(1));
		silverCaveRoom2.getWarps().get(1).setDestination(route28.getWarps().get(1));
		
		//Create groups of warps which all lead to the same warp
		HashMap<Warp, List<Warp>> warpSourcess = new HashMap<>();
		for (Map map : maps) for (Warp warp : map.getWarps()) warpSourcess.put(warp, new ArrayList<>());
		for (Map map : maps) for (Warp warp : map.getWarps()) if (warp.getDestination() != null) warpSourcess.get(warp.getDestination()).add(warp);
		
		//Make a list of maps to unrandomise warps within
		List<String> unrandomisedMapNames = new ArrayList<>(
				Arrays.asList("NEW_BARK_TOWN", "ELMS_LAB", "PLAYERS_HOUSE_1F", "PLAYERS_HOUSE_2F",
						"PLAYERS_NEIGHBORS_HOUSE", "ELMS_HOUSE", "BATTLE_TOWER_BATTLE_ROOM", "BATTLE_TOWER_ELEVATOR",
						"BATTLE_TOWER_HALLWAY", "INDIGO_PLATEAU_POKECENTER_1F", "WILLS_ROOM", "KOGAS_ROOM",
						"BRUNOS_ROOM", "KARENS_ROOM", "LANCES_ROOM", "HALL_OF_FAME", "POKECENTER_2F", "TRADE_CENTER",
						"COLOSSEUM", "TIME_CAPSULE", "MOBILE_TRADE_ROOM", "MOBILE_BATTLE_ROOM", "SILVER_CAVE_ROOM_3"));
		
		List<List<Warp>> warpGroups = new ArrayList<>();
		for (Map map : maps)
			if (!unrandomisedMapNames.contains(map.getConstName()) && !map.getConstName().contains("BETA"))
				for (Warp warp : map.getWarps())
		{
			Warp dest = warp.getDestination();
			if (dest == null) continue;
			else if (unrandomisedMapNames.contains(dest.getMap().getConstName())) continue;
			else if (dest.getMap().getConstName().contains("BETA")) continue;
			else if (dest.equals(warp)) continue;
			else if (!warp.hasAccessibleDestination()) continue;
			else if (dest.getDestination() == null) continue;
			else if (!dest.hasAccessibleDestination()) continue;
			
			List<Warp> group = null;
			
			groupTesting:
			for (List<Warp> testGroup : warpGroups) for (Warp testWarp : testGroup) if (warp.isPairedWith(testWarp))
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
		
		return rando.shuffleWarpGroups(warpGroups, false, true);
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
				
				default:
				{
					System.err.println(arg + " is an unrecognised argument");
					return;
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
			java.util.Map<Warp, Warp> newTargets = Notes.randomiseWarps(disassembly.getMaps(), rando);
			
			for (Map map : disassembly.getMaps()) for (Warp warp : map.getWarps()) if (newTargets.containsKey(warp)) warp.setDestination(newTargets.get(warp));
			
			if (disWriter != null) for (Map map : disassembly.getMaps())
			{
				map.writeWarpsToScript();
				disWriter.writeMapScript(map);
			}
		}
		else if (warpAreas)
		{
			if (disReader == null) System.out.println("Error: Randomisers require -d");
			java.util.Map<Warp, Warp> newTargets = Notes.buildWarpAreas(disassembly.getMaps(), allFlags, empReader, rando);
			
			for (Map map : disassembly.getMaps()) for (Warp warp : map.getWarps()) if (newTargets.containsKey(warp)) warp.setDestination(newTargets.get(warp));
			
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