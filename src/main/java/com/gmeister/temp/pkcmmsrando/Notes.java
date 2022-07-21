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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.gmeister.temp.pkcmmsrando.io.DisassemblyReader;
import com.gmeister.temp.pkcmmsrando.io.DisassemblyWriter;
import com.gmeister.temp.pkcmmsrando.io.EmpiricalDataReader;
import com.gmeister.temp.pkcmmsrando.map.data.CollisionConstant;
import com.gmeister.temp.pkcmmsrando.map.data.CollisionPermission;
import com.gmeister.temp.pkcmmsrando.map.data.CoordEvent;
import com.gmeister.temp.pkcmmsrando.map.data.Disassembly;
import com.gmeister.temp.pkcmmsrando.map.data.Flag;
import com.gmeister.temp.pkcmmsrando.map.data.Map;
import com.gmeister.temp.pkcmmsrando.map.data.MapConnection;
import com.gmeister.temp.pkcmmsrando.map.data.MapExploration;
import com.gmeister.temp.pkcmmsrando.map.data.MapExplorer;
import com.gmeister.temp.pkcmmsrando.map.data.OverworldPosition;
import com.gmeister.temp.pkcmmsrando.map.data.Player;
import com.gmeister.temp.pkcmmsrando.map.data.SpriteMovementDataConstant;
import com.gmeister.temp.pkcmmsrando.map.data.TileSet;
import com.gmeister.temp.pkcmmsrando.map.data.Warp;
import com.gmeister.temp.pkcmmsrando.map.data.WarpNetwork;
import com.gmeister.temp.pkcmmsrando.network.Edge;
import com.gmeister.temp.pkcmmsrando.network.FlaggedEdge;
import com.gmeister.temp.pkcmmsrando.network.FlaggedWarpNetwork;
import com.gmeister.temp.pkcmmsrando.network.WarpNode;
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
			List<Warp> group = warpGroups.stream().filter(g -> g.stream().anyMatch(w -> w.isPairedWith(warp))).findFirst().orElse(new ArrayList<>());
			if (!group.contains(warp)) group.add(warp);
			if (!warpGroups.contains(group)) warpGroups.add(group);
		}
		
		java.util.Map<List<Warp>, List<List<Warp>>> networkMap = new HashMap<>();
		for (List<Warp> group : warpGroups) networkMap.put(group, new ArrayList<>());
		
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
							networkMap.get(warpGroup).add(otherGroup);
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
		networkMap.get(route7GateToSaffron).add(route16ToGate);
		networkMap.put(route16ToGate, new ArrayList<>(Arrays.asList(route7GateToSaffron)));
		
		Map route16Gate = maps.stream().filter(m -> m.getConstName().equals("ROUTE_16_GATE")).findFirst().orElseThrow();
		List<Warp> route16GateToRoute = new ArrayList<>(route16Gate.getWarps().stream()
				.filter(w -> w.getPosition().getX() == 9 && (w.getPosition().getY() == 4 || w.getPosition().getY() == 5))
				.collect(Collectors.toList()));
		List<Warp> route17GateToRoute = warpGroups.stream().filter(g -> g.stream().anyMatch(w -> w.getMap().getConstName().equals("ROUTE_17_ROUTE_18_GATE"))).findFirst().orElseThrow();
		
		warpGroups.add(route16GateToRoute);
		networkMap.get(route17GateToRoute).add(route16GateToRoute);
		networkMap.put(route16GateToRoute, new ArrayList<>(Arrays.asList(route17GateToRoute)));
		
		WarpNetwork network = new WarpNetwork(networkMap);
		
		List<List<Warp>> sourceNodes = new ArrayList<>();
		List<List<Warp>> targetNodes = new ArrayList<>();
		for (List<Warp> sourceNode : network.getNetwork().keySet())
		{
			Warp target = sourceNode.stream().map(w -> w.getDestination()).filter(w -> w != null).findAny().orElseThrow();
			List<Warp> targetNode = network.getNetwork().keySet().stream().filter(n -> n.contains(target)).findAny().orElseThrow();
			sourceNodes.add(sourceNode);
			targetNodes.add(targetNode);
		}
		
		return rando.buildWarpGroups(sourceNodes, targetNodes, network, false, false, false, false);
	}
	
	public static List<Map> getMapsByNames(List<Map> maps, String... constNames)
	{
		List<Map> selectedMaps = new ArrayList<>();
		List<String> constNamesList = new ArrayList<>(Arrays.asList(constNames));
		for (Map map : maps) if (constNamesList.contains(map.getConstName())) selectedMaps.add(map);
		return selectedMaps;
	}
	
	//This return type does not work for multiple edges from a single warp
	//List<Edge<Warp>> could be better, but does not work for equal edges?
	public static java.util.Map<Warp, Warp> randomiseWarps(ArrayList<Map> maps, Randomiser rando, EmpiricalDataReader empReader, List<Flag> flags) throws IOException
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
		
		//Make a list of maps to unrandomise warps within
		List<String> unrandomisedMapNames = new ArrayList<>(
				Arrays.asList("NEW_BARK_TOWN", "ELMS_LAB", "PLAYERS_HOUSE_1F", "PLAYERS_HOUSE_2F",
						"PLAYERS_NEIGHBORS_HOUSE", "ELMS_HOUSE", "BATTLE_TOWER_BATTLE_ROOM", "BATTLE_TOWER_ELEVATOR",
						"BATTLE_TOWER_HALLWAY", "INDIGO_PLATEAU_POKECENTER_1F", "WILLS_ROOM", "KOGAS_ROOM",
						"BRUNOS_ROOM", "KARENS_ROOM", "LANCES_ROOM", "HALL_OF_FAME", "POKECENTER_2F", "TRADE_CENTER",
						"COLOSSEUM", "TIME_CAPSULE", "MOBILE_TRADE_ROOM", "MOBILE_BATTLE_ROOM", "SILVER_CAVE_ROOM_3"));
		
		//Select Warps to randomise
		List<Warp> warpsToRandomise = new ArrayList<>();
		List<List<Warp>> warpGroups = new ArrayList<>();
		for (Map map : maps)
			if (!unrandomisedMapNames.contains(map.getConstName()) && !map.getConstName().contains("BETA"))
				for (Warp source : map.getWarps())
		{
			Warp target = source.getDestination();
			if (target == null) continue;
			else if (unrandomisedMapNames.contains(target.getMap().getConstName())) continue;
			else if (target.getMap().getConstName().contains("BETA")) continue;
			else if (target.equals(source)) continue;
			else if (target.getDestination() == null) continue;
			//Swap || for && to include one-way warps
			else if (!source.hasAccessibleDestination() || !target.hasAccessibleDestination()) continue;
			
			warpsToRandomise.add(source);
			
			List<Warp> group = new ArrayList<>();
			group.add(source);
			warpGroups.add(group);
		}
		
		//Group warps together
		for (int i = 0; i < warpsToRandomise.size(); i++)
		{
			Warp warp = warpsToRandomise.get(i);
			List<Warp> group = warpGroups.stream().filter(g -> g.contains(warp)).findAny().orElseThrow();
			for (int j = i + 1; j < warpsToRandomise.size(); j++)
			{
				Warp otherWarp = warpsToRandomise.get(j);
				if (warp.isPairedWith(otherWarp))
				{
					List<Warp> otherGroup = warpGroups.stream().filter(g -> g.contains(otherWarp)).findAny().orElseThrow();
					if (group != otherGroup)
					{
						group.addAll(otherGroup);
						warpGroups.remove(otherGroup);
					}
				}
			}
		}
		
		//Remove groups whose targets don't lead back
		for (int i = 0; i < warpGroups.size();)
		{
			List<Warp> source = warpGroups.get(i);
			List<Warp> target = warpGroups.stream().filter(g -> g.contains(source.get(0).getDestination())).findAny().orElseThrow();
			if (target.stream().noneMatch(t -> source.contains(t.getDestination()))) warpGroups.remove(i);
			else i++;
		}
		
		//Create lists of Nodes and Branches to form a ConditionalWarpNetwork
		
		Set<WarpNode> nodes = new HashSet<>();
		Set<FlaggedEdge<WarpNode>> edges = new HashSet<>();
		java.util.Map<WarpNode, List<Warp>> nodeMap = new HashMap<>();
		
		//Add a node for every warp group
		for (List<Warp> warp : warpGroups)
		{
			WarpNode node = new WarpNode(warp);
			nodes.add(node);
			nodeMap.put(node, warp);
		}
		
		//Find all the nodes that each node can access, and create a branch for it 
		for (WarpNode node : nodes)
		{
			//Get the warp group from the node and select a representative warp
			Set<Warp> warpGroup = node.getWarps();
			Warp warp = nodeMap.get(node).get(0);
			
			//Create a map of maps to map explorers, and add an explorer for the starting map
			java.util.Map<Map, MapExplorer> explorerMap = new HashMap<>();
			MapExplorer startExplorer = new MapExplorer(warp.getMap());
			startExplorer.exploreFrom(warp.getPosition(), new HashSet<>());
			explorerMap.put(warp.getMap(), startExplorer);
			
			//Create a list of maps to test, and add the starting map
			List<Map> mapsToTest = new ArrayList<>();
			mapsToTest.add(warp.getMap());
			
			//Test all remaining maps
			while (mapsToTest.size() > 0)
			{
				Map map = mapsToTest.remove(0);
				
				//Get the explorer for this map
				MapExplorer explorer = explorerMap.get(map);
				if (explorer == null) throw new IllegalStateException();
				
				//Explore the map
				explorer.explore();
				
				//For every combination of flags neceessary for movements
				for (Set<Flag> otherFlags : explorer.getMapExplorationTable().keySet())
				{
					//Get the map exploration
					MapExploration exploration = explorer.getMapExplorationTable().get(otherFlags).getMapExploration();
					
					//Create a list of positions to continue movement through
					List<OverworldPosition> newPositions = new ArrayList<>();
					
					//For all accessed warps which aren't part of the current group
					for (Warp otherWarp : exploration.getWarpsAccessed()) if (!warpGroup.contains(otherWarp))
					{
						//Find the group this warp is part of
						List<Warp> otherGroup = warpGroups.stream().filter(g -> g.contains(otherWarp)).findAny().orElse(null);
						
						//If this warp isn't part of a group, continue movement through it's target
						if (otherGroup == null) newPositions.add(otherWarp.getDestination().getPosition());
						else
						{
							//This section might be unnecessary if we can scan everything altogether at the end
							
							//Find the node this warp is a part of
							WarpNode otherNode = nodes.stream().filter(n -> n.getWarps().contains(otherGroup.get(0))).findAny().orElseThrow();
							
							//Find all existing branches with the same source and target node
							List<FlaggedEdge<WarpNode>> currentBranches = edges.stream().filter(b -> b.getSource() == node && b.getTarget() == otherNode).collect(Collectors.toList());
							
							//Create the new branch and track whether it should be added
							FlaggedEdge<WarpNode> newBranch = new FlaggedEdge<>(node, otherNode, new HashSet<>(otherFlags));
							boolean addBranch = true;
							
							//For all existing branches
							for (FlaggedEdge<WarpNode> branch : currentBranches)
							{
								//If this branch contains the same or less flags, don't add the new branch 
								if (newBranch.getFlags().containsAll(branch.getFlags())) addBranch = false;
								//Otherwise, if this branch contains more flags, remove it
								//I don't think this ever runs
								else if (branch.getFlags().containsAll(newBranch.getFlags())) edges.remove(branch);
							}
							
							if (addBranch) edges.add(newBranch);
						}
					}
					
					//Continue movement through all map connections
					for (MapConnection connection : exploration.getConnectionsAccessed().keySet()) newPositions.addAll(exploration.getConnectionsAccessed().get(connection));
					
					//Create a list of new unique maps to update the tracking of, and loop through it
					List<Map> newMaps = newPositions.stream().map(p -> p.getMap()).distinct().collect(Collectors.toList());
					for (Map otherMap : newMaps)
					{
						if (explorerMap.get(otherMap) == null) explorerMap.put(otherMap, new MapExplorer(otherMap));
						
						boolean changed = false;
						for (OverworldPosition position : newPositions) if (position.getMap() == otherMap &&
								!explorerMap.get(otherMap).getEntry(otherFlags).getMapExploration().getTilesAccessed()[position.getY()][position.getX()])
						{
							changed = true;
							explorerMap.get(otherMap).exploreFrom(position, otherFlags);
						}
						
						if (changed && !mapsToTest.contains(otherMap)) mapsToTest.add(otherMap);
					}
				}
			}
			
			for (WarpNode otherNode : nodes) if (otherNode != node)
				for (Warp otherWarp : otherNode.getWarps()) if (!warp.hasAccessibleDestination() && explorerMap.containsKey(otherWarp.getMap()))
			{
				//Get all flags that can access this warp
				//If the branch does not exist, add it
				List<Set<Flag>> flagSets = explorerMap.get(otherWarp.getMap()).getFlagsToAccess(otherWarp.getPosition());
				List<FlaggedEdge<WarpNode>> existingBranches = edges.stream().filter(b -> b.getSource() == node && b.getTarget() == otherNode).collect(Collectors.toList());
				
				flagSets.stream().filter(s -> existingBranches.stream().noneMatch(b -> b.getFlags().equals(s))).forEach(s -> edges.add(new FlaggedEdge<>(node, otherNode, s)));
			}
		}
		
		//Create edges between warps that can access each other via elevators
		List<String> goldenrodDeptStoreElevMaps = new ArrayList<>(Arrays.asList("GOLDENROD_DEPT_STORE_B1F", "GOLDENROD_DEPT_STORE_1F", "GOLDENROD_DEPT_STORE_2F", "GOLDENROD_DEPT_STORE_3F", "GOLDENROD_DEPT_STORE_4F", "GOLDENROD_DEPT_STORE_5F", "GOLDENROD_DEPT_STORE_6F"));
		List<String> celadonDeptStoreElevMaps = new ArrayList<>(Arrays.asList("CELADON_DEPT_STORE_1F", "CELADON_DEPT_STORE_2F", "CELADON_DEPT_STORE_3F", "CELADON_DEPT_STORE_4F", "CELADON_DEPT_STORE_5F", "CELADON_DEPT_STORE_6F"));
		List<List<String>> elevMapLists = new ArrayList<>(Arrays.asList(goldenrodDeptStoreElevMaps, celadonDeptStoreElevMaps));
		
		for (List<String> elevMapList : elevMapLists)
		{
			List<WarpNode> elevMapNodes = nodes.stream().filter(n -> elevMapList.contains(nodeMap.get(n).get(0).getMap().getConstName())).collect(Collectors.toList());
			List<FlaggedEdge<WarpNode>> elevMapNodeEdges = edges.stream().filter(e -> elevMapNodes.contains(e.getSource()) && elevMapNodes.contains(e.getTarget())).collect(Collectors.toList());
			
			for (WarpNode source : elevMapNodes)
				for (WarpNode target : elevMapNodes)
					if (source != target && elevMapNodeEdges.stream().noneMatch(e -> e.getSource().equals(source) && e.getTarget().equals(target)))
						edges.add(new FlaggedEdge<WarpNode>(source, target, new HashSet<>()));
		}
		
		FlaggedWarpNetwork<WarpNode, FlaggedEdge<WarpNode>> network = new FlaggedWarpNetwork<>(nodes, edges);
		network.printEdgeTable();
		
		Set<FlaggedEdge<WarpNode>> originalEdges = new HashSet<>();
		for (WarpNode source : nodes)
		{
			//Errors for null destinations
			WarpNode target = nodes.stream().filter(n -> n.getWarps().contains(nodeMap.get(source).get(0))).findAny().orElseThrow();
			originalEdges.add(new FlaggedEdge<>(source, target, new HashSet<>()));
		}
		
		java.util.Map<Flag, List<Warp>> flagRequirements = empReader.readFlagRequirements(flags, maps);
		
		List<Edge<WarpNode>> randomisedWarps = rando.buildWarpGroups(originalEdges, network, false, false, false, false);
		
		java.util.Map<Warp, Warp> output = new HashMap<>();
		for (Edge<WarpNode> edge : randomisedWarps)
		{
			List<Warp> sourceList = nodeMap.get(edge.getSource());
			List<Warp> targetList = nodeMap.get(edge.getTarget());
			for (int i = 0; i < sourceList.size(); i++) output.put(sourceList.get(i), targetList.get(i % targetList.size()));
		}
		
		return output;
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
					//System.err.println("The --warp-areas randomiser is out of action until flag-based progression can be processed.");
					warpAreas = true;
					return;
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
			List<SpriteMovementDataConstant> moveDataConstants = empReader.readSpriteMovementDataConstants();
			
			disassembly.setCollisionConstants(empReader.readCollisionConstants(perms));
			disassembly.setTileSets(disReader.readTileSets(disassembly.getCollisionConstants()));
			disassembly.setMaps(disReader.readMaps(disassembly.getTileSets(), allFlags, moveDataConstants));
			
			List<CollisionConstant> coordEventConstants = empReader.readCoordEventCollision(perms);
			CollisionConstant floor = disassembly.getCollisionConstants().stream().filter(c -> c.getName().equals("COLL_FLOOR")).findFirst().orElseThrow();
			for (Map map : disassembly.getMaps())
			{
				CollisionConstant constant = coordEventConstants.stream().filter(c -> c.getName().equals(map.getConstName())).findFirst().orElse(floor);
				for (CoordEvent event : map.getCoordEvents()) event.setSimulatedCollision(constant);
			}
		}
		
		if (warps && warpAreas) System.err.println("Error: choose one of --warps and --warp-areas");
		else if (warps)
		{
			if (disReader == null) System.out.println("Error: Randomisers require -d");
			java.util.Map<Warp, Warp> newTargets = Notes.randomiseWarps(disassembly.getMaps(), rando, empReader, allFlags);
			
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