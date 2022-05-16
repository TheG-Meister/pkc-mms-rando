package com.gmeister.temp.pkcmmsrando.rando;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.gmeister.temp.pkcmmsrando.map.data.Block;
import com.gmeister.temp.pkcmmsrando.map.data.BlockSet;
import com.gmeister.temp.pkcmmsrando.map.data.Flag;
import com.gmeister.temp.pkcmmsrando.map.data.Map;
import com.gmeister.temp.pkcmmsrando.map.data.Warp;
import com.gmeister.temp.pkcmmsrando.network.Edge;
import com.gmeister.temp.pkcmmsrando.network.FlaggedEdge;
import com.gmeister.temp.pkcmmsrando.network.FlaggedWarpNetwork;
import com.gmeister.temp.pkcmmsrando.network.NodeGroup;
import com.gmeister.temp.pkcmmsrando.network.UnreturnableNetwork;
import com.gmeister.temp.pkcmmsrando.network.WarpNetwork;
import com.gmeister.temp.pkcmmsrando.network.WarpNode;

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
	
	public List<Edge<WarpNode>> buildWarpGroups(Collection<FlaggedEdge<WarpNode>> originalEdges,
			FlaggedWarpNetwork<WarpNode, FlaggedEdge<WarpNode>> inputNetwork, boolean oneWayBranches, boolean selfBranches, boolean unusedTargets, boolean reduceTargets)
	{
		if (originalEdges == null) throw new IllegalArgumentException("originalEdges must not be null");
		if (inputNetwork == null) throw new IllegalArgumentException("inputNetwork must not be null");
		
		Set<WarpNode> originalSources = new HashSet<>();
		Set<WarpNode> originalTargets = new HashSet<>();
		for (FlaggedEdge<WarpNode> edge : originalEdges)
		{
			if (!inputNetwork.getNodes().contains(edge.getSource())) throw new IllegalArgumentException("originalEdges must not contain edges with sources that are not part of the inputNetwork");
			if (!inputNetwork.getNodes().contains(edge.getTarget())) throw new IllegalArgumentException("originalEdges must not contain edges with targets that are not part of the inputNetwork");
			
			originalSources.add(edge.getSource());
			originalTargets.add(edge.getTarget());
		}
		
		if (!oneWayBranches && !originalTargets.containsAll(originalSources)) throw new IllegalArgumentException("Some source warps were omitted as target warps");
		
		//Suppressing this error until I understand how it works with merging warps or existing one-way warps
		//if (twoWay && !selfWarps && originalSources.size() + originalTargets.stream().filter(t -> !originalSources.contains(t)).count() % 2 != 0)
		//	throw new IllegalArgumentException("Could not avoid self warps as there are an odd number of branches being created.");
		
		Random random = new Random(this.random.nextLong());
		FlaggedWarpNetwork<WarpNode, FlaggedEdge<WarpNode>> network = new FlaggedWarpNetwork<>(inputNetwork);
		
		boolean testedAllSources = false;
		
		List<WarpNode> sources = new ArrayList<>(originalSources);
		List<WarpNode> allWarps = new ArrayList<>(originalSources);
		for (WarpNode warp : originalTargets) if (Collections.frequency(allWarps, warp) < Collections.frequency(sources, warp)) allWarps.add(warp);
		
		//Create lists to loop through
		List<WarpNode> sourceList = new ArrayList<>(allWarps);
		List<WarpNode> targets = new ArrayList<>(allWarps);
		
		//Pull random destinations
		List<WarpNode> newSources = new ArrayList<>();
		List<WarpNode> newTargets = new ArrayList<>();
		List<Edge<WarpNode>> output = new ArrayList<>();
		
		{
			Set<NodeGroup<WarpNode>> sourceTiers = network.getUnreturnableNetwork().getSourceNodes();
			Set<NodeGroup<WarpNode>> targetTiers = network.getUnreturnableNetwork().getTargetNodes();
			
			if (!this.canSolveAllUnreturnableBranches(network.getUnreturnableNetwork(), sourceTiers, targetTiers, sources, targets))
				throw new IllegalArgumentException("An unreturnable branch in the network cannot be made returnable.");
			
			if (!this.canConnectAllComponents(network.getComponentNetwork().getNodes(), sources, targets))
				throw new IllegalArgumentException("A network component could not be connected to another component.");
			
			long controllableBranches = this.countControllableBranches(sources, oneWayBranches);
			long neededBranches = this.countNeededBranches(network, sourceTiers, targetTiers, oneWayBranches);
			
			if (controllableBranches < neededBranches)
				throw new IllegalArgumentException("More branches are necessary for each warp to access every other warp.");
		}
		
		for (int sourceIndex = 0; sourceIndex < sourceList.size();)
		{
			if (unusedTargets) targets = new ArrayList<>(allWarps);
			Collections.shuffle(targets, random);
			
			WarpNode source = sourceList.get(sourceIndex);
			if (oneWayBranches && !sources.contains(source)) continue;
			boolean targetAcquired = false;
			
			targetLoop:
			for (WarpNode target : targets) 
			{
				if (!selfBranches && source == target) continue targetLoop;
				
				List<FlaggedEdge<WarpNode>> trackedBranches = new ArrayList<>();
				trackedBranches.add(new FlaggedEdge<>(source, target, new ArrayList<>()));
				if (!oneWayBranches && source != target) trackedBranches.add(new FlaggedEdge<>(target, source, new ArrayList<>()));
				
				List<FlaggedEdge<WarpNode>> newBranches = new ArrayList<>();
				for (FlaggedEdge<WarpNode> branch : trackedBranches) if (sources.contains(branch.getSource())) newBranches.add(branch);
				
				if (newBranches.isEmpty())
					continue targetLoop;
				
				FlaggedWarpNetwork<WarpNode, FlaggedEdge<WarpNode>> nextNetwork = new FlaggedWarpNetwork<>(network);
				List<WarpNode> nextSources = new ArrayList<>(sources);
				List<WarpNode> nextTargets = new ArrayList<>(targets);
				for (FlaggedEdge<WarpNode> edge : newBranches)
				{
					nextNetwork.addEdge(edge);
					nextSources.remove(edge.getSource());
					nextTargets.remove(edge.getTarget());
				}
				
				if (!this.validateNetwork(nextNetwork, nextSources, nextTargets, oneWayBranches))
					continue targetLoop;
				
				//Create the branch and update tracking data
				for (FlaggedEdge<WarpNode> edge : trackedBranches)
				{
					sourceList.remove(edge.getSource());
					targets.remove(edge.getTarget());
					newSources.add(edge.getSource());
					newTargets.add(edge.getTarget());
					output.add(edge);
					
					if (newBranches.contains(edge))
					{
						sources.remove(edge.getSource());
					}
				}
				network = nextNetwork;
				
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
		if (network.getComponentNetwork().getNodes().size() > 1) throw new IllegalStateException("Not all components were joined together");
		//Not sure how this one works with a flagged network
		if (!network.getUnreturnableNetwork().getEdges().isEmpty()) throw new IllegalStateException("Not all one-way branches were given an alternative path");
		
		return output;
	}
	
	private boolean validateNetwork(FlaggedWarpNetwork<WarpNode, FlaggedEdge<WarpNode>> network, List<WarpNode> sources, List<WarpNode> targets, boolean oneWayBranches)
	{
		Set<NodeGroup<WarpNode>> sourceTiers = network.getUnreturnableNetwork().getSourceNodes();
		Set<NodeGroup<WarpNode>> targetTiers = network.getUnreturnableNetwork().getTargetNodes();
		
		boolean unreturnableBranches = this.canSolveAllUnreturnableBranches(network.getUnreturnableNetwork(), sourceTiers, targetTiers, sources, targets);
		
		if (!unreturnableBranches) return false;
		else
		{
			boolean components = this.canConnectAllComponents(network.getComponentNetwork().getNodes(), sources, targets);
			
			if (!components) return false;
			else
			{
				long controllableBranches = this.countControllableBranches(sources, oneWayBranches);
				long neededBranches = this.countNeededBranches(network, sourceTiers, targetTiers, oneWayBranches);
				
				if (controllableBranches < neededBranches) return false;
				else return true;
			}
		}
	}
	
	private boolean canSolveAllUnreturnableBranches(UnreturnableNetwork<WarpNode, FlaggedEdge<WarpNode>> network, Set<NodeGroup<WarpNode>> sourceNodes, Set<NodeGroup<WarpNode>> targetNodes, List<WarpNode> sources, List<WarpNode> targets)
	{
		if (network.getEdges().size() < 1) return true;
		
		//For every tier
		for (NodeGroup<WarpNode> node : network.getNodes())
		{
			//If any bottom tier has no sources left, return false
			if (!targetNodes.contains(node) && sourceNodes.contains(node) && node.getNodes().stream().filter(n -> sources.contains(n)).count() < 1) return false;
			//If any top tier has no targets left, return false
			if (targetNodes.contains(node) && !sourceNodes.contains(node) && node.getNodes().stream().filter(n -> targets.contains(n)).count() < 1) return false;
		}
		
		return true;
	}
	
	private boolean canCollectAllPermissions(FlaggedWarpNetwork<WarpNode, FlaggedEdge<WarpNode>> network, List<WarpNode> sources, List<WarpNode> targets, java.util.Map<Flag, List<WarpNode>> flagRequirements, WarpNode start)
	{
		//Find each component with a flag in it
		//Find the minimum flags to get that flag
		//start from the start warp if it's present
		//otherwise test from every target warp in the component
		//Make a map from flag to sets of sets of flags
		//"Collect 'em all"
		//If any are uncollected return false
		//otherwise return true
		
		Set<Set<Flag>> flagCombinations = new HashSet<>();
		for (FlaggedEdge<WarpNode> edge : network.getEdges()) flagCombinations.add(edge.getFlags());
		
		for (Set<Flag> flags : flagCombinations)
		{
			WarpNetwork<WarpNode, FlaggedEdge<WarpNode>> collapsedNetwork = network.collapse(flags);
			Set<WarpNode> flagNodes = new HashSet<>();
			for (Flag flag : flags) flagNodes.addAll(flagRequirements.get(flag));
			
			for (NodeGroup<WarpNode> component : collapsedNetwork.getComponentNetwork().getNodes())
				if (flagNodes.stream().anyMatch(n -> component.getNodes().contains(n)))
			{
				if (component.getNodes().stream().filter(w -> sources.contains(w)).count() < 1)
					return false;
				if (component.getNodes().stream().filter(w -> targets.contains(w)).count() < 1)
					return false;
			}
		}
		
		return true;
	}
	
	private boolean canConnectAllComponents(Set<NodeGroup<WarpNode>> components, List<WarpNode> sources, List<WarpNode> targets)
	{
		//If there are one or less components, return true
		if (components.size() < 2) return true;
		
		//Otherwise for all components
		for (NodeGroup<WarpNode> component : components)
		{
			//If the component contains no sources, return false
			if (component.getNodes().stream().filter(w -> sources.contains(w)).count() < 1) return false;
			
			//If the component contains no targets, return false
			if (component.getNodes().stream().filter(w -> targets.contains(w)).count() < 1) return false;
		}
		
		//Otherwise return true;
		return true;
	}
	
	private int countSpareBranches(FlaggedWarpNetwork<WarpNode, FlaggedEdge<WarpNode>> network, List<WarpNode> sources, List<WarpNode> targets, boolean oneWayBranches, boolean unusedTargets, boolean reduceTargets)
	{
		//if unused targets are allowed
		//still gotta solve all one way branches
		//still gotta connect all components
		//don't bother connecting any nodes that are already connected
		return -1;
	}
	
	private long countControllableBranches(List<WarpNode> sources, boolean oneWayBranches)
	{
		//allWarps.stream().distinct().mapToInt(w -> Math.max(0, Collections.frequency(tempTargetList, w) - Collections.frequency(sources, w))).sum();
		
		//if (!oneWayBranches) return Math.floorDiv(sources.size() + targets.stream().filter(t -> !sources.contains(t)).count(), 2);
		//else return sources.size();
		
		//If one way edges are disallowed, return half the size of the source list, rounded down
		if (!oneWayBranches) return Math.floorDiv(sources.size(), 2);
		
		//Otherwise return the size of the source list
		else return sources.size();
	}
	
	private long countNeededBranches(FlaggedWarpNetwork<WarpNode, FlaggedEdge<WarpNode>> network, Set<NodeGroup<WarpNode>> sourceTiers, Set<NodeGroup<WarpNode>> targetTiers, boolean oneWayBranches)
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
		
		//The fork sum is the minimum number of forking one-way systems
		long forkSum = 0;
		//The merge sum is the minimum number of merging one-way systems
		long mergeSum = 0;
		
		//For each component
		for (NodeGroup<WarpNode> component : network.getComponentNetwork().getNodes())
		{
			//Find every tier that is part of the component
			Set<NodeGroup<WarpNode>> tiers = new HashSet<>();
			for (WarpNode node : component.getNodes()) tiers.add(network.getUnreturnableNetwork().getNode(node));
			
			//If there is more than one tier
			if (tiers.size() > 1)
			{
				//Count the number of bottom tiers, subtract one, and add to the fork sum
				forkSum += tiers.stream().filter(t -> !sourceTiers.contains(t) && targetTiers.contains(t)).count() - 1;
				//Count the number of top tiers, subtract one, and add to the merge sum
				mergeSum += tiers.stream().filter(t -> sourceTiers.contains(t) && !targetTiers.contains(t)).count() - 1;
			}
		}
		
		//Calculate the number of branches needed to connect all components and solve all one-way systems (except one)
		long neededBranches = Math.max(forkSum, mergeSum) + network.getComponentNetwork().getNodes().size() - 1;
		//Add an additional branch if there is any one-way system remaining, or one that may be created in future
		if (network.getUnreturnableNetwork().getEdges().size() > 0 || (oneWayBranches && network.getComponentNetwork().getNodes().size() > 1)) neededBranches++;
		
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