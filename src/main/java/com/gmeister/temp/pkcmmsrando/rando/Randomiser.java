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
	
	public java.util.Map<Warp, Warp> buildWarpGroups(List<List<Warp>> originalSources, List<List<Warp>> originalTargets,
			WarpNetwork inputNetwork, boolean oneWayBranches, boolean selfBranches, boolean unusedTargets, boolean reduceTargets)
	{
		if (originalSources.size() != originalTargets.size()) throw new IllegalArgumentException("Differing number of sources and targets");
		if (!inputNetwork.getNetwork().keySet().containsAll(originalSources)) throw new IllegalArgumentException("Not all sources are found in the network");
		if (!inputNetwork.getNetwork().keySet().containsAll(originalTargets)) throw new IllegalArgumentException("Not all targets are found in the network");
		if (!oneWayBranches && !originalTargets.containsAll(originalSources)) throw new IllegalArgumentException("Some source warps were omitted as target warps");
		
		//Suppressing this error until I understand how it works with merging warps or existing one-way warps
		//if (twoWay && !selfWarps && originalSources.size() + originalTargets.stream().filter(t -> !originalSources.contains(t)).count() % 2 != 0)
		//	throw new IllegalArgumentException("Could not avoid self warps as there are an odd number of branches being created.");
		
		Random random = new Random(this.random.nextLong());
		WarpNetwork network = new WarpNetwork(inputNetwork);
		
		boolean testedAllSources = false;
		
		List<List<Warp>> sources = new ArrayList<>(originalSources);
		List<List<Warp>> allWarps = new ArrayList<>(originalSources);
		for (List<Warp> warp : originalTargets) if (Collections.frequency(allWarps, warp) < Collections.frequency(sources, warp)) allWarps.add(warp);
		
		//Create lists to loop through
		List<List<Warp>> sourceList = new ArrayList<>(allWarps);
		List<List<Warp>> targets = new ArrayList<>(allWarps);
		
		//Pull random destinations
		List<List<Warp>> newSources = new ArrayList<>();
		List<List<Warp>> newTargets = new ArrayList<>();
		
		if (!this.canSolveAllUnreturnableBranches(network, sources, targets))
			throw new IllegalArgumentException("An unreturnable branch in the network cannot be made returnable.");
		
		if (!this.canConnectAllComponents(network, sources, targets))
			throw new IllegalArgumentException("A network component could not be connected to another component.");
		
		long controllableBranches = this.countControllableBranches(network, sources, targets, oneWayBranches);
		long neededBranches = this.countNeededBranches(network, null, oneWayBranches);
		
		if (controllableBranches < neededBranches)
			throw new IllegalArgumentException("More branches are necessary for each warp to access every other warp.");
		
		for (int sourceIndex = 0; sourceIndex < sourceList.size();)
		{
			if (unusedTargets) targets = new ArrayList<>(allWarps);
			Collections.shuffle(targets, random);
			
			List<Warp> source = sourceList.get(sourceIndex);
			if (oneWayBranches && !sources.contains(source)) continue;
			boolean targetAcquired = false;
			
			targetLoop:
			for (List<Warp> target : targets) 
			{
				if (!selfBranches && source == target) continue targetLoop;
				
				List<Branch> trackedBranches = new ArrayList<>();
				trackedBranches.add(new Branch(source, target, null, null));
				if (!oneWayBranches && source != target) trackedBranches.add(new Branch(target, source, null, null));
				
				List<Branch> newBranches = new ArrayList<>();
				for (Branch branch : trackedBranches) if (sources.contains(branch.source)) newBranches.add(branch);
				
				if (newBranches.isEmpty())
					continue targetLoop;
				
				WarpNetwork nextNetwork = new WarpNetwork(network);
				List<List<Warp>> nextSources = new ArrayList<>(sources);
				List<List<Warp>> nextTargets = new ArrayList<>(targets);
				for (Branch branch : newBranches)
				{
					nextNetwork.addBranch(branch.source, branch.target);
					nextSources.remove(branch.source);
					nextTargets.remove(branch.target);
				}
				
				if (!this.validateNetwork(nextNetwork, nextSources, nextTargets, oneWayBranches))
					continue targetLoop;
				
				//Create the branch and update tracking data
				for (Branch branch : trackedBranches)
				{
					sourceList.remove(branch.source);
					targets.remove(branch.target);
					newSources.add(branch.source);
					newTargets.add(branch.target);
					
					if (newBranches.contains(branch))
					{
						sources.remove(branch.source);
						network.addBranch(branch.source, branch.target);
					}
				}
				
				targetAcquired = true;
				break targetLoop;
			}
			
			if (!targetAcquired) throw new IllegalStateException("Could not find a destination warp for source " + source);
			
			if (!selfBranches && !testedAllSources)
			{
				while (sourceIndex < sourceList.size() && newTargets.contains(sourceList.get(sourceIndex))) sourceIndex++;
				if (sourceIndex >= sourceList.size())
				{
					sourceIndex = 0;
					testedAllSources = true;
				}
			}
		}
		
		if (!sourceList.isEmpty()) throw new IllegalStateException("Not all sources were assigned a destination");
		if (network.getComponents().size() > 1) throw new IllegalStateException("Not all components were joined together");
		if (!network.getOneWayBranches().isEmpty()) throw new IllegalStateException("Not all one-way branches were given an alternative path");
		
		java.util.Map<Warp, Warp> output = new HashMap<>();
		for (int i = 0; i < newSources.size(); i++)
		{
			List<Warp> newSource = newSources.get(i);
			List<Warp> newTarget = newTargets.get(i);
			for (int j = 0; j < newSource.size(); j++) output.put(newSource.get(j), newTarget.get(j % newTarget.size()));
		}
		
		return output;
	}
	
	private boolean validateNetwork(WarpNetwork network, List<List<Warp>> sources, List<List<Warp>> targets, boolean oneWayBranches)
	{
		if (!this.canSolveAllUnreturnableBranches(network, sources, targets)) return false;
		else if (!this.canConnectAllComponents(network, sources, targets)) return false;
		else
		{
			long controllableBranches = this.countControllableBranches(network, sources, targets, oneWayBranches);
			long neededBranches = this.countNeededBranches(network, null, oneWayBranches);
			
			if (controllableBranches < neededBranches) return false;
			else return true;
		}
	}
	
	private boolean canSolveAllUnreturnableBranches(WarpNetwork network, List<List<Warp>> sources, List<List<Warp>> targets)
	{
		for (Branch branch : network.getOneWayBranches())
		{
			//Might be able to do this for only the top and bottom tiers of each component
			List<List<Warp>> warpsAbove = network.getAllAccessorTiers(network.getTierOf(branch.source))
					.stream()
					.flatMap(List::stream)
					.collect(Collectors.toList());
			List<List<Warp>> warpsBelow = network.getAllAccesseeTiers(network.getTierOf(branch.target))
					.stream()
					.flatMap(List::stream)
					.collect(Collectors.toList());
			
			//If any new one-way branch would have no targets above or sources below, continue
			if (warpsAbove.stream().filter(w -> targets.contains(w)).count() < 1)
				return false;
			if (warpsBelow.stream().filter(w -> sources.contains(w)).count() < 1)
				return false;
		}
		
		return true;
	}
	
	private boolean canConnectAllComponents(WarpNetwork network, List<List<Warp>> sources, List<List<Warp>> targets)
	{
		if (network.getComponents().size() > 1)
			for (List<List<Warp>> component : network.getComponents())
		{
			if (component.stream().filter(w -> sources.contains(w)).count() < 1)
				return false;
			if (component.stream().filter(w -> targets.contains(w)).count() < 1)
				return false;
		}
		
		return true;
	}
	
	private int countSpareBranches(WarpNetwork network, List<List<Warp>> sources, List<List<Warp>> targets, boolean oneWayBranches, boolean unusedTargets, boolean reduceTargets)
	{
		//if unused targets are allowed
		//still gotta solve all one way branches
		//still gotta connect all components
		//don't bother connecting any nodes that are already connected
		return -1;
	}
	
	private long countControllableBranches(WarpNetwork network, List<List<Warp>> sources, List<List<Warp>> targets, boolean oneWayBranches)
	{
		//allWarps.stream().distinct().mapToInt(w -> Math.max(0, Collections.frequency(tempTargetList, w) - Collections.frequency(sources, w))).sum();
		
		//if (!oneWayBranches) return Math.floorDiv(sources.size() + targets.stream().filter(t -> !sources.contains(t)).count(), 2);
		//else return sources.size();
		
		if (!oneWayBranches) return Math.floorDiv(sources.size(), 2);
		else return sources.size();
	}
	
	private long countNeededBranches(WarpNetwork network, List<List<Warp>> targetOnlyWarps, boolean oneWayBranches)
	{
		/*long forkSum = 0;
		long mergeSum = 0;
		
		for (List<List<Warp>> component : network.getComponents())
		{
			List<List<List<Warp>>> tiers = network.getTiersOf(component);
			if (tiers.size() > 1) for (List<List<Warp>> tier : tiers)
			{
				if (network.isTopTier(tier)) mergeSum += Math.max(1, tier.stream().filter(w -> targetOnlyWarps.contains(w)).count());
				else
				{
					if (network.isBottomTier(tier)) forkSum++;
					mergeSum += tier.stream().filter(w -> targetOnlyWarps.contains(w)).count();
				}
			}
		}*/
		
		long forkSum = network.getComponents().stream().mapToLong(c -> network.countBottomTiers(c) - 1).sum();
		long mergeSum = network.getComponents().stream().mapToLong(c -> network.countTopTiers(c) - 1).sum();
		
		long neededBranches = Math.max(forkSum, mergeSum) + network.getComponents().size() - 1;
		if (network.getOneWayBranches().size() > 0 || (oneWayBranches && network.getComponents().size() > 1)) neededBranches++;
		
		return neededBranches;
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