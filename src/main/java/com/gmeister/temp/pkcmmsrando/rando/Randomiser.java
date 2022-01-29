package com.gmeister.temp.pkcmmsrando.rando;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.gmeister.temp.pkcmmsrando.map.data.Block;
import com.gmeister.temp.pkcmmsrando.map.data.BlockSet;
import com.gmeister.temp.pkcmmsrando.map.data.Map;
import com.gmeister.temp.pkcmmsrando.map.data.Warp;
import com.gmeister.temp.pkcmmsrando.map.data.WarpNetwork;
import com.gmeister.temp.pkcmmsrando.map.data.WarpNetwork.Branch;

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
	 * Shuffles selected lines in a script.
	 * @param script the script
	 * @param toShuffle a list of the same length of the script, denoting the lines to shuffle
	 * @return a shuffled script
	 */
	public ArrayList<String> shuffleScriptLines(ArrayList<String> script, List<Boolean> toShuffle)
	{
		Random random = new Random(this.random.nextLong());
		ArrayList<String> shuffledScript = new ArrayList<>();
		
		for (int i = 0; i < toShuffle.size() && i < script.size(); i++) if (toShuffle.get(i)) shuffledScript.add(script.get(i));
		Collections.shuffle(shuffledScript, random);
		for (int i = 0; i < script.size(); i++)
		{
			if (i >= toShuffle.size()) shuffledScript.add(script.get(i));
			else if (!toShuffle.get(i)) shuffledScript.add(i, script.get(i));
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
	 * Shuffles the pointers to all overworld sprites, such that all occurrences of each sprite are replaced with a different sprite.
	 * <br><br>
	 * This randomiser is currently untested.
	 * @param script the overworld sprite script to shuffle
	 * @return a shuffled version of the provided script
	 */
	public ArrayList<String> shuffleOverworldSpritePointers(ArrayList<String> script)
	{
		Pattern constPattern = Pattern.compile("^\\toverworld_sprite ");
		List<Boolean> toShuffle = script.stream().map(s -> constPattern.matcher(s).find()).collect(Collectors.toList());
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
	
	public java.util.Map<Warp, Warp> buildWarpGroups(WarpNetwork inputNetwork, boolean selfWarps, boolean twoWay, boolean oneIn)
	{
		Random random = new Random(this.random.nextLong());
		WarpNetwork network = new WarpNetwork(inputNetwork);
		
		if (twoWay && !selfWarps && network.getNetwork().keySet().size() % 2 != 0) throw new IllegalArgumentException("Could not avoid self warps as there are an odd number of groups");
		
		boolean testedAllSources = false;
		
		//Create a random list of destinations
		List<List<Warp>> sources = new ArrayList<>(network.getNetwork().keySet());
		List<List<Warp>> targets = new ArrayList<>(sources);
		if (oneIn) Collections.shuffle(targets, random);
		
		//Pull random destinations
		List<List<Warp>> newSources = new ArrayList<>();
		List<List<Warp>> newTargets = new ArrayList<>();
		
		for (int sourceIndex = 0; sourceIndex < sources.size();)
		{
			//Store the number of branches that can be controlled
			int controllableBranches;
			if (twoWay) controllableBranches = Math.floorDiv(sources.size(), 2);
			else controllableBranches = sources.size();
			
			//Store the sum of all one-way branches that are parallel to another in forks and merges
			long forkSum = network.getComponents().stream().mapToLong(c -> network.countBottomTiers(c) - 1).sum();
			long mergeSum = network.getComponents().stream().mapToLong(c -> network.countTopTiers(c) - 1).sum();
			
			//Store the number of branches necessary to make a completable overworld
			long neededBranches = Math.max(forkSum, mergeSum) + network.getComponents().size() - 1;
			if (network.getOneWayBranches().size() > 0 || (!twoWay && network.getComponents().size() > 1)) neededBranches++;
			
			//If there are less controllable branches than needed branches, throw an error
			if (controllableBranches < neededBranches)
			{
				//If this is the first loop, the provided network cannot generate a completable overworld
				if (sources.containsAll(network.getNetwork().keySet())) throw new IllegalArgumentException("accessibleGroups does not contain enough connections to fulfil all provided settings");
				//If this is any other loop, the coded logic is wrong
				else throw new IllegalStateException("Too many optional branches were created");
			}
			
			List<Warp> source = sources.get(sourceIndex);
			
			final List<List<Warp>> localTargets;
			if (oneIn) localTargets = targets;
			else
			{
				localTargets = new ArrayList<>(network.getNetwork().keySet());
				Collections.shuffle(targets, random);
			}
			
			targetLoop:
			for (List<Warp> target : localTargets) 
			{
				if (!selfWarps && source == target) continue targetLoop;
				
				List<Branch> newBranches = new ArrayList<>();
				newBranches.add(new Branch(source, target, null, null));
				if (twoWay && source != target) newBranches.add(new Branch(target, source, null, null));
				
				List<List<Warp>> nextSources = new ArrayList<>(sources);
				nextSources.remove(source);
				if (twoWay) nextSources.remove(target);
				
				List<List<Warp>> nextTargets = new ArrayList<>(localTargets);
				nextTargets.remove(target);
				if (twoWay) nextTargets.remove(source);
				
				WarpNetwork nextNetwork = new WarpNetwork(network);
				for (Branch branch : newBranches) nextNetwork.addBranch(branch.source, branch.target);
				
				for (Branch branch : nextNetwork.getOneWayBranches())
				{
					List<List<Warp>> warpsAbove = nextNetwork.getAllAccessorTiers(nextNetwork.getTierOf(branch.source))
							.stream()
							.flatMap(List::stream)
							.collect(Collectors.toList());
					List<List<Warp>> warpsBelow = nextNetwork.getAllAccesseeTiers(nextNetwork.getTierOf(branch.target))
							.stream()
							.flatMap(List::stream)
							.collect(Collectors.toList());
					
					//If any new one-way branch would have no targets above or sources below, continue
					if (warpsAbove.stream().filter(w -> nextTargets.contains(w)).count() < 1)
						continue targetLoop;
					if (warpsBelow.stream().filter(w -> nextSources.contains(w)).count() < 1)
						continue targetLoop;
				}
				
				//If there is more than one component, and any component has no remaining sources or targets, continue;
				if (nextNetwork.getComponents().size() > 1)
					for (List<List<Warp>> component : nextNetwork.getComponents())
				{
					if (component.stream().filter(w -> nextSources.contains(w)).count() < 1)
						continue targetLoop;
					if (component.stream().filter(w -> nextTargets.contains(w)).count() < 1)
						continue targetLoop;
				}
				
				//if the new controllable branches is less than the new needed branches, continue
				int nextControllableBranches;
				if (twoWay) nextControllableBranches = Math.floorDiv(nextSources.size(), 2);
				else nextControllableBranches = nextSources.size();
				
				long nextForkSum = nextNetwork.getComponents().stream().mapToLong(c -> nextNetwork.countBottomTiers(c) - 1).sum();
				long nextMergeSum = nextNetwork.getComponents().stream().mapToLong(c -> nextNetwork.countTopTiers(c) - 1).sum();
				
				long nextNeededBranches = Math.max(nextForkSum, nextMergeSum) + nextNetwork.getComponents().size() - 1;
				if (nextNetwork.getOneWayBranches().size() > 0 || (!twoWay && nextNetwork.getComponents().size() > 1)) nextNeededBranches++;
				
				if (nextControllableBranches < nextNeededBranches)
					continue targetLoop;
				
				//Create the branch and update tracking data
				for (Branch branch : newBranches)
				{
					sources.remove(branch.source);
					localTargets.remove(branch.target);
					newSources.add(branch.source);
					newTargets.add(branch.target);
					network.addBranch(branch.source, branch.target);
				}
				
				break targetLoop;
			}
			
			if (sources.contains(source)) throw new IllegalStateException("Could not find a destination warp for source " + source);
			
			if (!selfWarps && !testedAllSources)
			{
				while (sourceIndex < sources.size() && newTargets.contains(sources.get(sourceIndex))) sourceIndex++;
				if (sourceIndex >= sources.size())
				{
					sourceIndex = 0;
					testedAllSources = true;
				}
			}
		}
		
		if (!sources.isEmpty()) throw new IllegalStateException("Not all sources were assigned a destination");
		if (network.getComponents().size() > 1) throw new IllegalStateException("Not all components were joined together");
		if (!network.getOneWayBranches().isEmpty()) throw new IllegalStateException("Not all one-way branches were given an alternative path");
		
		for (List<Warp> warpGroup : network.getNetwork().keySet())
		{
			List<List<Warp>> groupsBelow = network.getAllAccessees(warpGroup);
			
			System.out.print(warpGroup.get(0).getPosition());
			for (List<Warp> otherGroup : network.getNetwork().keySet()) System.out.print("\t" + (groupsBelow.contains(otherGroup) ? 1 : 0));
			System.out.println();
		}
		
		java.util.Map<Warp, Warp> output = new HashMap<>();
		for (int i = 0; i < newSources.size(); i++)
		{
			List<Warp> newSource = newSources.get(i);
			List<Warp> newTarget = newTargets.get(i);
			for (int j = 0; j < newSource.size(); j++) output.put(newSource.get(j), newTarget.get(j % newTarget.size()));
		}
		
		return output;
	}
	
	public java.util.Map<Warp, Warp> shuffleWarpGroups(List<List<Warp>> warpGroups, boolean allowSelfWarps, boolean twoWay)
	{
		//Get a Random object
		Random random = new Random(this.random.nextLong());
		
		if (warpGroups.size() % 2 != 0 && twoWay && !allowSelfWarps) throw new IllegalArgumentException("Could not avoid self warps as there are an odd number of destinations");
		
		//Create a random list of destinations
		List<List<Warp>> groups = new ArrayList<>(warpGroups);
		List<List<Warp>> shuffledGroups = new ArrayList<>(warpGroups);
		Collections.shuffle(shuffledGroups, random);
		
		//Pull random destinations
		List<List<Warp>> oldGroups = new ArrayList<>();
		List<List<Warp>> newGroups = new ArrayList<>();
		
		while (shuffledGroups.size() > 0)
		{
			List<Warp> newGroup = shuffledGroups.get(0);
			boolean testedAllOldDestsForThisGroup = false;
			
			oldGroupLoops:
			while (true)
			{
				for (List<Warp> oldGroup : groups) if (allowSelfWarps || ((!newGroups.contains(oldGroup) || testedAllOldDestsForThisGroup) && !oldGroup.equals(newGroup))) 
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
		
		java.util.Map<Warp, Warp> output = new HashMap<>();
		for (int i = 0; i < oldGroups.size(); i++)
		{
			List<Warp> newSource = oldGroups.get(i);
			List<Warp> newTarget = newGroups.get(i);
			for (int j = 0; j < newSource.size(); j++) output.put(newSource.get(j), newTarget.get(j % newTarget.size()));
		}
		
		return output;
	}
	
}