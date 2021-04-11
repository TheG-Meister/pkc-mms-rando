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
	
	public void shuffleWarps(ArrayList<Map> maps)
	{
		Random random = new Random(this.random.nextLong());
		
		/*
		ArrayList<Warp> warps = new ArrayList<>();
		for (Map map : maps) warps.addAll(map.getWarps());
		Collections.shuffle(warps, random);
		for (Map map : maps) for (int i = 0; i < map.getWarps().size(); i++) 
		{
			Warp oldWarp = map.getWarps().get(i); 
			Warp newWarp = warps.remove(0);
			oldWarp.setMapTo(newWarp.getMapTo());
			oldWarp.setDestination(newWarp);
		}*/
		
		/*
		 * A warp is one way if the tile of the warp it leads to is not a tile with warp permissions
		 * A warp group consists of a selection of warps where each warp in the group leads to any other warp in the group
		 * 
		 * Maps to ban:
		 * All beta maps
		 * All Pokecentre 1F rooms
		 * All E4 rooms and HOF
		 * Battle tower rooms
		 * 
		 * Warps to fix:
		 * 
		 * What is gonna happen to elevators?
		 * 
		 * I don't know how to swap around warps, lol.
		 * The current thing I'm trying is classifying every warp into a group based on how they link to each other
		 * I'm then splitting them by their destinations, and want to swap the destinations of one set of warps to another set of warps.
		 * That seems to get super finnicky at the end
		 * 
		 * I also tried finding all the warps that have the destination of a particular warp
		 * I think swap the destination of each of those groups to be a different warp
		 * I think this should work better in principle?
		 * The problem with this is that it doesn't actually create two way warps, lol
		 * It can if you do things right?
		 */
		
		
		ArrayList<Map> newMaps = new ArrayList<>(maps);
		/*for (Map map : maps)
		{
			if (map.getName().contains("Beta")) newMaps.remove(map);
		}*/
		
		ArrayList<Warp> warps = new ArrayList<>();
		ArrayList<ArrayList<Warp>> warpsSources = new ArrayList<>();
		
		for (Map map : newMaps) for (Warp warp : map.getWarps())
		{
			warps.add(warp);
			warpsSources.add(new ArrayList<>());
		}
		for (Map map : newMaps) for (Warp warp : map.getWarps()) if (warp.getDestination() != null) warpsSources.get(warps.indexOf(warp.getDestination())).add(warp);
		
		ArrayList<Integer> shuffledIndices = new ArrayList<>();
		for (int i = 0; i < warps.size(); i++) shuffledIndices.add(i);
		Collections.shuffle(shuffledIndices, random);
		
		ArrayList<Warp> newWarps = new ArrayList<>();
		for (int i = 0; i < warps.size(); i++) newWarps.add(new Warp());
		for (int i = 0; i < warps.size(); i++)
		{
			Warp warp = warps.get(i);
			Warp newWarp = newWarps.get(i);
			newWarp.setX(warp.getX());
			newWarp.setY(warp.getY());
			if (warp.getDestination() != null) newWarp.setDestination(newWarps.get(warps.indexOf(warp.getDestination())));
			newWarp.setMapTo(warp.getMapTo());
		}
		
		int count = 0;
		for (int i = 0; i < warps.size(); i++) if (warpsSources.get(i).size() > 0) count++;
		int count2 = 0;
		ArrayList<Integer> usedIndices = new ArrayList<>();
		for (int i = 0; i < warps.size(); i++) if (warpsSources.get(i).size() > 0 && !usedIndices.contains(i))
		{
			//Pop new indices from shuffledIndices until the new warp in question actually has things that lead to it
			int newIndex;
			do newIndex = shuffledIndices.remove(0);
			while (warpsSources.get(newIndex).size() < 1 || usedIndices.contains(newIndex));
			
			//For each warp that leads to the old warp, change their destination to the new warp
			ArrayList<Warp> oldWarpSources = warpsSources.get(i);
			Warp newWarpWithData = warps.get(newIndex);
			for (int warpIndex = 0; warpIndex < oldWarpSources.size(); warpIndex++)
			{
				Warp toEdit = newWarps.get(warps.indexOf(oldWarpSources.get(warpIndex)));
				if (newWarpWithData.getDestination() == null) toEdit.setDestination(null);
				else toEdit.setDestination(newWarps.get(warps.indexOf(newWarpWithData.getDestination())));
				toEdit.setMapTo(newWarpWithData.getMapTo());
			}
			
			//For each warp that has its destination as the new warp, change their destination to the destination of the old warp
			ArrayList<Warp> newWarpSources = warpsSources.get(newIndex);
			Warp oldWarpWithData = warps.get(i);
			for (int warpIndex = 0; warpIndex < newWarpSources.size(); warpIndex++)
			{
				Warp toEdit = newWarps.get(warps.indexOf(newWarpSources.get(warpIndex)));
				if (oldWarpWithData.getDestination() == null) toEdit.setDestination(null);
				else toEdit.setDestination(newWarps.get(warps.indexOf(oldWarpWithData.getDestination())));
				toEdit.setMapTo(oldWarpWithData.getMapTo());
			}
			
			//Tag indices as used
			usedIndices.add(i);
			usedIndices.add(newIndex);
			count2 += 2;
		}
		
		for (Map map : newMaps) for (int warpIndex = 0; warpIndex < map.getWarps().size(); warpIndex++) map.getWarps().set(warpIndex, newWarps.get(warps.indexOf(map.getWarps().get(warpIndex))));
		
		for (Map map : newMaps) map.writeWarpsToScript();
	}
	
}