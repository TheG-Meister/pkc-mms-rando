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
	
	public Randomiser()
	{
		this.random = new Random();
	}
	
	public Randomiser(Random random)
	{
		this.random = random;
	}
	
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
	
	public ArrayList<String> shuffleMusicPointers(ArrayList<String> script)
	{
		boolean[] toShuffle = new boolean[script.size()];
		for (int i = 0; i < script.size(); i++) toShuffle[i] = script.get(i).startsWith("\tdba") && !script.get(i).contains("Music_Nothing");
		return this.shuffleScriptLines(script, toShuffle);
	}
	
	public ArrayList<String> shuffleSFXPointers(ArrayList<String> script)
	{
		boolean[] toShuffle = new boolean[script.size()];
		for (int i = 0; i < script.size(); i++) toShuffle[i] = script.get(i).startsWith("\tdba");
		return this.shuffleScriptLines(script, toShuffle);
	}
	
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
	
	//Change this function such that, for every warp, it accepts a list of potential candidates it can randomise to?
	//Other methods can then run this one by filling up the groups
	//Won't this be a fuck tonne of array lists if there's like 600 warps?
	//Aren't we doing that anyway with the warps sources?
	
	/*
	 * Chunks of code we have here:
	 * Getting a random
	 * Getting the warp sources arrays
	 * Creating the list of old warp to new warp
	 * Various changes to said array depending on rules
	 * Actually applying the changes
	 */
	
	/*
	 * The current basic warp rando shuffles only warps that are destinations, and then makes them two-way
	 * The most recent warp rando preserves connections between maps, but shuffles the warps that lead there via a similar process to the basic one
	 * 
	 * New plan
	 */
	
	/*
	 * Can we split this up so 1-way warps only reandomise destinations with other 1-way warps?
	 * Can we split this up so the destination selection criteria/actions are the only included code, without passing an anonymous class into another method?
	 * (Or at least each line of code runs something simple, or another method that we can modify)
	 */
	
	public void shuffleWarpDestinations(ArrayList<Warp> warps, boolean allowSelfWarps, boolean twoWay)
	{
		//Get a Random object
		Random random = new Random(this.random.nextLong());
		
		//Create groups of warps which all lead to the same warp
		ArrayList<ArrayList<Warp>> warpSourcess = new ArrayList<>();
		for (int i = 0; i < warps.size(); i++) warpSourcess.add(new ArrayList<>());
		for (Warp warp : warps) if (warp.getDestination() != null) warpSourcess.get(warps.indexOf(warp.getDestination())).add(warp);
		
		//Create a list of useable destinations
		ArrayList<Warp> destinations = new ArrayList<>();
		for (Warp warp : warps) if (warpSourcess.get(warps.indexOf(warp)).size() > 0 && warp.getDestination() != null) destinations.add(warp);
		
		if (destinations.size() % 2 != 0 && twoWay && !allowSelfWarps) throw new IllegalArgumentException("Could not avoid self warps as there are an odd number of destinations");
		
		//Create a random list of destinations
		ArrayList<Warp> shuffledDestinations = new ArrayList<>(destinations);
		Collections.shuffle(shuffledDestinations, random);
		
		//Pull random destinations
		ArrayList<Warp> oldDests = new ArrayList<>();
		ArrayList<Warp> newDests = new ArrayList<>();
		
		boolean testedAllOldDests = false;
		
		while (shuffledDestinations.size() > 0)
		{
			Warp newDest = shuffledDestinations.remove(0);
			newDests.add(newDest);
			
			findAnOldDest:
			while (true)
			{
				for (Warp oldDest : destinations) if (allowSelfWarps || (!newDests.contains(oldDest) || testedAllOldDests) && !oldDest.equals(newDest)) 
				{
					//if (!newIndices.contains(shuffledIndex) && warpSourcess.get(oldIndex).get(0).getDestination().getMap().equals(warps.get(shuffledIndex).getDestination().getMap()))
					destinations.remove(oldDest);
					oldDests.add(oldDest);
					
					if (twoWay)
					{
						destinations.remove(newDest);
						oldDests.add(newDest);
						shuffledDestinations.remove(oldDest);
						newDests.add(oldDest);
					}
					
					break findAnOldDest;
				}
				testedAllOldDests = true;
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