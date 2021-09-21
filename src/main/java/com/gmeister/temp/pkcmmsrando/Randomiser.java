package com.gmeister.temp.pkcmmsrando;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gmeister.temp.pkcmmsrando.map.data.Block;
import com.gmeister.temp.pkcmmsrando.map.data.BlockSet;
import com.gmeister.temp.pkcmmsrando.map.data.Map;
import com.gmeister.temp.pkcmmsrando.map.data.Warp;

public class Randomiser
{
	
	private Random random;
	
	/**
	 * Initialises a Randomiser with a new Random.
	 */
	public Randomiser()
	{
		this.random = new Random();
	}
	
	/**
	 * Initialises a Randomiser with the provided Random.
	 * @param random the Random to initialise with.
	 */
	public Randomiser(Random random)
	{
		this.random = random;
	}
	
	/**
	 * Shuffles blocks to other blocks with the same collision.
	 * <br><br>
	 * Replaces every block with a block that has the same collision values.
	 * This creates maps that look like an absolute mess but play exactly like the original maps.
	 * <br><br>
	 * This randomiser is race safe.
	 * @param blockSet the BlockSet from which new Blocks will be drawn
	 * @param blocks the Blocks to replace
	 * @return a new array of random blocks
	 */
	public Block[] randomiseBlocksByCollision(BlockSet blockSet, Block[] blocks)
	{
		Random random = new Random(this.random.nextLong());
		Block[] randomisedBlocks = new Block[blocks.length];
		
		for (int i = 0; i < blocks.length; i++) if (blockSet.getBlocks().indexOf(blocks[i]) != 0)
		{
			ArrayList<Block> blockGroup = null;
			for (ArrayList<Block> group : blockSet.getCollGroups()) if (group.contains(blocks[i]))
			{
				blockGroup = group;
				break;
			}
			
			int index = random.nextInt(blockGroup.size());
			//inelegant solution, but works
			while (blockSet.getBlocks().indexOf(blockGroup.get(index)) == 0) index = random.nextInt(blockGroup.size());
			randomisedBlocks[i] = blockGroup.get(index);
			if (randomisedBlocks[i] == null || blockSet.getBlocks().indexOf(randomisedBlocks[i]) == 0) throw new IllegalStateException();
		}
		
		return randomisedBlocks;
	}
	
	/**
	 * Shuffles selected lines in a script.
	 * @param script the script
	 * @param toShuffle an array of the same length of the script, denoting the lines to shuffle
	 * @return a shuffled script
	 */
	public ArrayList<String> shuffleScriptLines(ArrayList<String> script, boolean[] toShuffle)
	{
		Random random = new Random(this.random.nextLong());
		ArrayList<String> shuffledScript = new ArrayList<>();
		
		for (int i = 0; i < toShuffle.length && i < script.size(); i++) if (toShuffle[i]) shuffledScript.add(script.get(i));
		Collections.shuffle(shuffledScript, random);
		for (int i = 0; i < script.size(); i++)
		{
			if (i >= toShuffle.length) shuffledScript.add(script.get(i));
			else if (!toShuffle[i]) shuffledScript.add(i, script.get(i));
		}
		
		return shuffledScript;
	}
	
	/**
	 * Shuffles the pointers of all music tracks, such that every incidence of one track is replaced with a different track.
	 * <br><br>
	 * This randomiser is safe for races.
	 * @param script the music script to shuffle
	 * @return a shuffled script
	 */
	public ArrayList<String> shuffleMusicPointers(ArrayList<String> script)
	{
		boolean[] toShuffle = new boolean[script.size()];
		for (int i = 0; i < script.size(); i++) toShuffle[i] = script.get(i).startsWith("\tdba") && !script.get(i).contains("Music_Nothing");
		return this.shuffleScriptLines(script, toShuffle);
	}
	
	/**
	 * Shuffles the pointers of all sound effects, such that every incidence of one sound effect is replaced with a different sound effect.
	 * <br><br>
	 * This randomiser is safe for races, but having quick sound-effects replaced with long ones can greatly slow down pace.
	 * @param script the SFX script to shuffle
	 * @return a shuffled version of the provided script
	 */
	public ArrayList<String> shuffleSFXPointers(ArrayList<String> script)
	{
		boolean[] toShuffle = new boolean[script.size()];
		for (int i = 0; i < script.size(); i++) toShuffle[i] = script.get(i).startsWith("\tdba");
		return this.shuffleScriptLines(script, toShuffle);
	}
	
	/**
	 * Move all trainers to a random position on the map.
	 * <br><br>
	 * This edits the map script in place and requires it to be written out.
	 * <br><br>
	 * This randomiser is not safe for races as trainers may block required paths.
	 * @param map the map to shuffle trainer locations on
	 */
	public void randomiseTrainerLocation(Map map)
	{
		Random random = new Random(this.random.nextLong());
		Pattern commentsPattern = Pattern.compile("\\s*;.*");
		Pattern mapConstPattern = Pattern.compile("\\tmap_const\\s+");
		Pattern commaWhitespacePattern = Pattern.compile("\\s*,\\s*");
		Pattern objectEventPattern = Pattern.compile("\\tobject_event\\s+");
		Pattern numberPattern = Pattern.compile("\\d+");
		
		for (int i = 0; i < map.getScript().size(); i++)
		{
			String line = map.getScript().get(i);
			if (objectEventPattern.matcher(line).find())
			{
				String argsLine = commentsPattern.matcher(line).replaceFirst("");
				argsLine = mapConstPattern.matcher(line).replaceFirst("");
				String[] args = commaWhitespacePattern.split(argsLine);
				
				if (args[9].equals("OBJECTTYPE_TRAINER"))
				{
					int newX = random.nextInt(map.getXCapacity());
					int newY = random.nextInt(map.getYCapacity());
					
					StringBuffer buffer = new StringBuffer();
					Matcher numbers = numberPattern.matcher(line);
					numbers.find();
					numbers.appendReplacement(buffer, String.valueOf(newX));
					numbers.find();
					numbers.appendReplacement(buffer, String.valueOf(newY));
					numbers.appendTail(buffer);
					
					map.getScript().set(i, buffer.toString());
				}
			}
		}
	}
	
	public void shuffleWarpGroups(ArrayList<ArrayList<Warp>> warpGroups, boolean allowSelfWarps, boolean twoWay)
	{
		//Get a Random object
		Random random = new Random(this.random.nextLong());
		
		if (warpGroups.size() % 2 != 0 && twoWay && !allowSelfWarps) throw new IllegalArgumentException("Could not avoid self warps as there are an odd number of destinations");
		
		//Create a random list of destinations
		ArrayList<ArrayList<Warp>> groups = new ArrayList<>(warpGroups);
		ArrayList<ArrayList<Warp>> shuffledGroups = new ArrayList<>(warpGroups);
		Collections.shuffle(shuffledGroups, random);
		
		//Pull random destinations
		ArrayList<ArrayList<Warp>> oldGroups = new ArrayList<>();
		ArrayList<ArrayList<Warp>> newGroups = new ArrayList<>();
		
		while (shuffledGroups.size() > 0)
		{
			ArrayList<Warp> newGroup = shuffledGroups.get(0);
			boolean testedAllOldDestsForThisGroup = false;
			
			oldGroupLoops:
			while (true)
			{
				for (ArrayList<Warp> oldGroup : groups) if (allowSelfWarps || (!newGroups.contains(oldGroup) || testedAllOldDestsForThisGroup) && !oldGroup.equals(newGroup)) 
				{
					groups.remove(oldGroup);
					oldGroups.add(oldGroup);
					shuffledGroups.remove(newGroup);
					newGroups.add(newGroup);
					
					if (twoWay)
					{
						groups.remove(newGroup);
						oldGroups.add(newGroup);
						shuffledGroups.remove(oldGroup);
						newGroups.add(oldGroup);
					}
					
					break oldGroupLoops;
				}
				
				if (!testedAllOldDestsForThisGroup)
				{
					testedAllOldDestsForThisGroup = true;
				}
				else
				{
					throw new IllegalStateException("Could not find an old destination for the warp that links " + newGroup.get(0).getMap().getConstName() + " to " + newGroup.get(0).getDestination().getMap().getConstName());
				}
			}
		}
		
		for (int i = 0; i < oldGroups.size(); i++)
		{
			ArrayList<Warp> oldGroup = oldGroups.get(i);
			ArrayList<Warp> newGroup = newGroups.get(i);
			for (int j = 0; j < oldGroup.size(); j++) oldGroup.get(j).setDestination(newGroup.get(j % newGroup.size()));
		}
	}
	
	/*
	 * Currently, this method performs various initial filtering steps which could ideally be moved out to a more relevant class.
	 * For example, one-way warps are not selected as destinations. While this acts as a good balancing feature, it is not necessary for all warp randomisers.
	 * 
	 * Groups of destinations will be considered in the future. This will allow 2-tile warp carpets leading to other 2-tile warp carpets to be considered as a single destination each.
	 */
	
	/**
	 * Shuffles the destinations of provided warps.
	 * <br><br>
	 * Any warp which is accessible from another warp, does not move, and leads back to one of the warps that leads to it is selected as a destination for shuffling.
	 * All warps leading to a destination which was selected for shuffling are then redirected to a shuffled destination.
	 * <br><br>
	 * This randomiser has the potential to be safe for races with enough moderation of the warps provided, but currently this is not present in the code.
	 * @param warps the list of warps to shuffle the destination of
	 * @param allowSelfWarps whether a warp can select itself as its destination
	 * @param twoWay whether to force randomised destinations to lead back to where they came from
	 * @param preserveMapConnections whether to keep connections between Maps
	 */
	public void shuffleWarpDestinations(ArrayList<Warp> warps, boolean allowSelfWarps, boolean twoWay, boolean preserveMapConnections)
	{
		//Get a Random object
		Random random = new Random(this.random.nextLong());
		
		//Create groups of warps which all lead to the same warp
		ArrayList<ArrayList<Warp>> warpSourcess = new ArrayList<>();
		for (int i = 0; i < warps.size(); i++) warpSourcess.add(new ArrayList<>());
		for (Warp warp : warps) if (warp.getDestination() != null) warpSourcess.get(warps.indexOf(warp.getDestination())).add(warp); 
		
		//Create a list of useable destinations
		ArrayList<Warp> destinations = new ArrayList<>();
		for (Warp warp : warps) if (warpSourcess.get(warps.indexOf(warp)).size() > 0 //Destinations must have another warp lead to them
				&& warp.getDestination() != null //Destinations must not move (SS Aqua, Elevators, Cable Club) 
				&& warp.hasAccessibleDestination() //Destinations must be able to reach their own destination
				&& warpSourcess.get(warps.indexOf(warp)).contains(warp.getDestination())) //Destinations lead back to one of their sources
		{
			//Destinations have at least one warp that can access it
			for (Warp source : warpSourcess.get(warps.indexOf(warp))) if (source.hasAccessibleDestination())
			{
				destinations.add(warp);
				break;
			}
		}
		
		for (int i = 0; i < destinations.size(); i++) if (!destinations.contains(destinations.get(i).getDestination()))
		{
			System.out.println("A dest was not included!");
			destinations.remove(i);
			i--;
		}
		
		ArrayList<Map> maps = new ArrayList<>();
		ArrayList<ArrayList<Map>> mapConnections = new ArrayList<>();
		ArrayList<ArrayList<Map>> newConnections = new ArrayList<>();
		for (Warp warp : destinations)
		{
			if (!maps.contains(warp.getMap()))
			{
				maps.add(warp.getMap());
				mapConnections.add(new ArrayList<>());
				newConnections.add(new ArrayList<>());
			}
			if (warp.getDestination() != null) mapConnections.get(maps.indexOf(warp.getMap())).add(warp.getDestination().getMap());
		}
		
		if (destinations.size() % 2 != 0 && twoWay && !allowSelfWarps) throw new IllegalArgumentException("Could not avoid self warps as there are an odd number of destinations");
		
		//Create a random list of destinations
		ArrayList<Warp> shuffledDestinations = new ArrayList<>(destinations);
		Collections.shuffle(shuffledDestinations, random);
		
		//Pull random destinations
		ArrayList<Warp> oldDests = new ArrayList<>();
		ArrayList<Warp> newDests = new ArrayList<>();
		
		while (shuffledDestinations.size() > 0)
		{
			Warp newDest = shuffledDestinations.get(0);
			boolean testedAllOldDestsForThisWarp = false;
			
			oldDestLoops:
			while (true)
			{
				for (Warp oldDest : destinations) if (allowSelfWarps || (!newDests.contains(oldDest) || testedAllOldDestsForThisWarp) && !oldDest.equals(newDest)) 
				{
					if (preserveMapConnections &&
							Collections.frequency(newConnections.get(maps.indexOf(oldDest.getDestination().getMap())), newDest.getDestination().getMap())
							>= Collections.frequency(mapConnections.get(maps.indexOf(oldDest.getDestination().getMap())), newDest.getDestination().getMap()))
						continue;
					
					destinations.remove(oldDest);
					oldDests.add(oldDest);
					shuffledDestinations.remove(newDest);
					newDests.add(newDest);
					newConnections.get(maps.indexOf(oldDest.getDestination().getMap())).add(newDest.getDestination().getMap());
					
					if (twoWay)
					{
						destinations.remove(newDest);
						oldDests.add(newDest);
						shuffledDestinations.remove(oldDest);
						newDests.add(oldDest);
						newConnections.get(maps.indexOf(newDest.getDestination().getMap())).add(oldDest.getDestination().getMap());
					}
					
					break oldDestLoops;
				}
				
				if (!testedAllOldDestsForThisWarp)
				{
					testedAllOldDestsForThisWarp = true;
				}
				else
				{
					throw new IllegalStateException("Could not find an old destination for the warp that links " + newDest.getMap().getConstName() + " to " + newDest.getDestination().getMap().getConstName());
				}
			}
		}
		
		if (destinations.size() > 0) throw new IllegalStateException("Logic error: not all destinations were arrigned a new one");
		if (oldDests.size() != newDests.size()) throw new IllegalStateException("Logic error: there are different numbers of old and new destinations");
		
		//For each warp, record it's new destination to prevent concurrent modification errors
		ArrayList<Warp> newDestinations = new ArrayList<>();
		for (Warp warp : warps)
		{
			if (oldDests.contains(warp.getDestination())) newDestinations.add(newDests.get(oldDests.indexOf(warp.getDestination())).getDestination());
			else newDestinations.add(warp.getDestination());
		}
		
		//Apply the new recorded destinations
		for (int i = 0; i < warps.size(); i++) warps.get(i).setDestination(newDestinations.get(i));
	}
	
}