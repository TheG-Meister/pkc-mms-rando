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
import com.gmeister.temp.pkcmmsrando.map.data.Flags;
import com.gmeister.temp.pkcmmsrando.map.data.Map;
import com.gmeister.temp.pkcmmsrando.map.data.Warp;
import com.gmeister.temp.pkcmmsrando.network.Edge;
import com.gmeister.temp.pkcmmsrando.network.FlaggedEdge;
import com.gmeister.temp.pkcmmsrando.network.FlaggedNetworkExplorer;
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
			FlaggedWarpNetwork<WarpNode, FlaggedEdge<WarpNode>> inputNetwork, java.util.Map<Flag, Set<WarpNode>> flagRequirements, WarpNode start, boolean oneWayBranches, boolean selfBranches, boolean unusedTargets, boolean reduceTargets)
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
			
			if (!this.canCollectAllFlags(network, sources, targets, flagRequirements, start))
				throw new IllegalArgumentException("All flags must be collectible in this network.");
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
				
				if (!this.validateNetwork(nextNetwork, nextSources, nextTargets, flagRequirements, start, oneWayBranches))
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
	
	private boolean validateNetwork(FlaggedWarpNetwork<WarpNode, FlaggedEdge<WarpNode>> network, List<WarpNode> sources, List<WarpNode> targets, java.util.Map<Flag, Set<WarpNode>> flagRequirements, WarpNode start, boolean oneWayBranches)
	{
		//Timer t = new Timer().start().print("Timer start");
		Set<NodeGroup<WarpNode>> sourceTiers = network.getUnreturnableNetwork().getSourceNodes();
		Set<NodeGroup<WarpNode>> targetTiers = network.getUnreturnableNetwork().getTargetNodes();
		
		boolean unreturnableBranches = this.canSolveAllUnreturnableBranches(network.getUnreturnableNetwork(), sourceTiers, targetTiers, sources, targets);
		//t.split().printSplit("Unreturnable branches");
		
		if (!unreturnableBranches) return false;
		else
		{
			boolean components = this.canConnectAllComponents(network.getComponentNetwork().getNodes(), sources, targets);
			//t.split().printSplit("Components");
			
			if (!components) return false;
			else
			{
				long controllableBranches = this.countControllableBranches(sources, oneWayBranches);
				long neededBranches = this.countNeededBranches(network, sourceTiers, targetTiers, oneWayBranches);
				//t.split().printSplit("Controllable branches");
				
				if (controllableBranches < neededBranches) return false;
				else
				{
					boolean flags = this.canCollectAllFlags(network, sources, targets, flagRequirements, start);
					//t.split().printSplit("Flags");
					
					if (!flags) return false;
					else return true;
				}
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
	
	private boolean canCollectAllFlags(FlaggedWarpNetwork<WarpNode, FlaggedEdge<WarpNode>> network, List<WarpNode> sources, List<WarpNode> targets, java.util.Map<Flag, Set<WarpNode>> flagRequirements, WarpNode start)
	{
		//System.out.println();
		//Timer t = new Timer().start().print("Timer start");
		//Store the flags required to get to each node
		java.util.Map<WarpNode, List<Set<Flag>>> nodeFlags = new HashMap<>();
		
		//This will be the flagged unreturnable network eventually
		FlaggedNetworkExplorer<WarpNode, FlaggedEdge<WarpNode>> explorer = new FlaggedNetworkExplorer<>(network, e -> e.getFlags());
		//t.split().printSplit("Init");
		//WarpNetwork<WarpNode, FlaggedEdge<WarpNode>> collapsedNetwork = network.collapse(new HashSet<>());
		//t.split().printSplit("Collapse network");
		
		//For every node required for any flag
		for (Flag flag : flagRequirements.keySet()) for (WarpNode node : flagRequirements.get(flag))
		{
			//find the component of the node
			NodeGroup<WarpNode> component = network.getComponentNetwork().getNode(node);
			
			Set<Set<Flag>> flagSets = new HashSet<>();
			
			//if this component contains the starting node, find the flags to get from the start warp to the node
			if (component.getNodes().contains(start)) flagSets.addAll(explorer.getFlagsForPath(start, node));
			
			//find the all possible flag combinations that allow movement from any available target node to the flag node
			List<WarpNode> componentTargets = targets.stream().filter(n -> component.getNodes().contains(n)).collect(Collectors.toList());
			
			//If the flag node is a target, no flags are required
			//This is technically a workaround, FlaggedNetworkExplorer could return this too
			if (componentTargets.contains(node)) flagSets.add(new HashSet<>());
			else targets.stream().filter(n -> component.getNodes().contains(n)).flatMap(n -> explorer.getFlagsForPath(n, node).stream()).forEach(s -> flagSets.add(s));
			
			nodeFlags.put(node, new ArrayList<>(flagSets));
		}
		//t.split().printSplit("Node collection");
		
		//If no combination of flags can get to a node, return false
		for (WarpNode node : nodeFlags.keySet()) if (nodeFlags.get(node).isEmpty())
			return false;
		
		//convert nodeFlags into a map of flags to the flags required to get them
		java.util.Map<Flag, Set<Set<Flag>>> flagFlags = new HashMap<>();
		for (Flag flag : flagRequirements.keySet()) for (WarpNode node : flagRequirements.get(flag))
		{
			if (!flagFlags.containsKey(flag)) flagFlags.put(flag, Flags.simplify(nodeFlags.get(node)));
			else flagFlags.put(flag, Flags.multiply(flagFlags.get(flag), nodeFlags.get(node)));
		}
		
		//t.split().printSplit("Flag conversion");
		
		//Attempt to collect all the flags starting from nothing
		Set<Flag> flagsCollected = new HashSet<>();
		int lastLength;
		do
		{
			lastLength = flagsCollected.size();
			for (Flag flag : flagFlags.keySet()) for (Set<Flag> requirements : flagFlags.get(flag)) if (flagsCollected.containsAll(requirements)) flagsCollected.add(flag);
		}
		while (lastLength < flagsCollected.size());
		
		//t.split().printSplit("Flag collection");
		//t.print("Total");
		
		if (flagRequirements.keySet().equals(flagsCollected)) return true;
		else return false;
	}
	
	private boolean canMissRequirements(FlaggedWarpNetwork<WarpNode, FlaggedEdge<WarpNode>> network, List<WarpNode> sources, List<WarpNode> targets, java.util.Map<Flag, Set<WarpNode>> flagRequirements, WarpNode start)
	{
		//Okay so we start from the start warp
		//Collapse to no flags because we'll need to do that anyway
		//Find bottom tiers
		//For every bottom tier
		//If there is a source, continue
		//If there is no way to exit the tier with flags, exit bad
		//If there is not all the requirements for the flagged exit, exit bad
		//Re-collapse with only required flags
		//Repeat
		
		//The only difference between the start warp and any other component is that we know the player begins at the start warp with no flags
		//Doing the analysis for all components means we can find the permissions required not to softlock
		//That permissions simply needs to be empty for the start warp
		//We need some form of conditional branch detector so we know what we can limit things behind too
		
		//I think we try using a network exploration
		//For now, find any source that can access another source? no
		//Find any target that can access a source only using flags
		//And also the source side of the flagged branch can access another source, or all of the required flags
		
		FlaggedNetworkExplorer<WarpNode, FlaggedEdge<WarpNode>> explorer = new FlaggedNetworkExplorer<>(network, e -> e.getFlags());
		
		List<Set<Flag>> flagSets = new ArrayList<>();
		flagSets.add(new HashSet<>());
		
		WarpNetwork<WarpNode, FlaggedEdge<WarpNode>> collapsedNetwork = network.collapse(new ArrayList<>());
		
		//Find a target that can only access a source conditionally - flaggedNetworkExplorer
		//Get a grouped network with only conditional branches and use it to find the limiting branches
		//Get a collapsed network where the only branches left over are conditional, unreturnable, or conditionally returnable
		//And use it to make sure there is a source above the limiting branches, or all the permissions required to progress
		
		
		//let's take an unconditional tier. It has a target, so any warp could lead to it, no sources, so it can't be left, and it can be accessed returnably with surf
		//It can also be left with surf
		//This would be a completely separate component within a collapsed network at no flags.
		//We do know that we can only get to a component via a target, or the start warp
		//technically any bottom tier that can't ever be accessed isn't worth assessing
		//From the start warp, we have no permissions and we're trying not to softlock
		//From a target, we want to find the permissions required to get to a source
		
		Set<NodeGroup<WarpNode>> sourceTiers = collapsedNetwork.getUnreturnableNetwork().getSourceNodes();
		Set<NodeGroup<WarpNode>> targetTiers = collapsedNetwork.getUnreturnableNetwork().getTargetNodes();
		
		Set<NodeGroup<WarpNode>> bottomTiers = new HashSet<>(collapsedNetwork.getUnreturnableNetwork().getNodes().stream().filter(n -> !sourceTiers.contains(n) && targetTiers.contains(n)).collect(Collectors.toList()));
		Set<NodeGroup<WarpNode>> nonSourceTiers = new HashSet<>(collapsedNetwork.getUnreturnableNetwork().getNodes().stream().filter(n -> !sourceTiers.contains(n)).collect(Collectors.toList()));
		
		//This might actually be useful
		
		//Okay, we should be able to write a quick method that does an incomplete search for sources or targets
		//Expand out only afterwards if we need to
		
		//A limitable flag set is a set of flags which we know we can force the player to obtain by making them pass through an edge that uses the flags
		//This happens when there is no other way for the player to progress except through this edge
		Set<Set<Flag>> limitableFlagSets = new HashSet<>();
		
		source:
		for (WarpNode source : network.getNodes())
		{
			boolean sourceTested = false;
			NodeGroup<WarpNode> collapsedSourceTier;
			
			target:
			for (FlaggedEdge<WarpNode> edge : network.getEdges(source)) if (!edge.getFlags().isEmpty() && !limitableFlagSets.contains(edge.getFlags()))
			{
				if (!sourceTested)
				{
					collapsedSourceTier = collapsedNetwork.getUnreturnableNetwork().getNode(source);
					if (collapsedSourceTier.getNodes().stream().noneMatch(n -> sources.contains(n))) continue source;
					
					sourceTested = true;
				}
				
				NodeGroup<WarpNode> collapsedTargetTier = collapsedNetwork.getUnreturnableNetwork().getNode(edge.getTarget());
				if (collapsedTargetTier.equals(collapsedSourceTier)) continue target;
				if (collapsedTargetTier.getNodes().stream().noneMatch(n -> sources.contains(n))) continue source;
			}
		}
		
		for (NodeGroup<WarpNode> nonSourceTier : nonSourceTiers)
		{
			//If this tier has a source, it can be left, so skip it
			if (nonSourceTier.getNodes().stream().anyMatch(n -> sources.contains(n))) continue;
			
			//What happens if there's no other way to leave?
			//Skip again, because otherwise we're solving the other solftlock condition
			Set<FlaggedEdge<WarpNode>> additionalEdges = new HashSet<>(network.getEdges().stream().filter(e -> nonSourceTier.getNodes().contains(e.getSource()) && !nonSourceTier.getNodes().contains(e.getTarget())).collect(Collectors.toList()));
			
			//If there are no additional edges, this method is not designed to exit in this case, so continue
			if (additionalEdges.isEmpty()) continue;
			
			//If there are unflagged branches available for progression, network collapsing isn't working properly, so throw an exception
			if (additionalEdges.stream().anyMatch(e -> e.getFlags().isEmpty())) throw new IllegalStateException("FlaggedWarpNetwork.collapse() is not working properly");
			
			//There's a way to be efficient with the next step(s)
			//What ways do we have?
			//flaggednetworkexplorer all targets of additional edges to sources
			//get all targets of those edges and find sources in the result
			//Search for collapsed tiers of new edges and whether they have a source in them, flagged network explorer from one to source otherwise
			
			//For every edge
			//Find the collapsed tier it leads to
			//If it has a source or can access a source, and this tier contains all the requirements for the flag for the branch to get there, continue?
			
			//This doesn't work if surf permissions are locked behind cut and we know the player has cut
			
			//We might be able to skip a lot of this if we know we have branches we can restrict progression behind too
			//I think what that looks like is source and target, conditional branch, source
			//Requirements for the requirements in this case? simply that they aren't behind that branch I guess
			//Only need to look at the start warp then
			
			//Is there any scenario in which we won't be able to access permissions?
			//Technically? might not be relevant yet, but if they can't be accessed then the completion method will exit I guess
			//I definitely think it's relevant though
			
		}
		
		//Find all combinations of flags used in edges
		Set<Set<Flag>> flagSets = new HashSet<>(network.getEdges().stream().map(e -> e.getFlags()).collect(Collectors.toList()));
		flagSets.remove(new HashSet<>());
		flagSets = Flags.simplify(flagSets);
		Set<Flag> allFlags = new HashSet<>(flagSets.stream().flatMap(s -> s.stream()).collect(Collectors.toList()));
		
		for (Set<Flag> flags : flagSets)
		{
			Set<FlaggedEdge<WarpNode>> edges = new HashSet<>(network.getEdges()
					.stream()
					.filter(e -> e.getFlags()
							.stream()
							.anyMatch(f -> flags.contains(f)))
					.collect(Collectors.toList()));
			
			Set<NodeGroup<WarpNode>> components = new HashSet<>(edges.stream()
					.map(e -> network.getComponentNetwork()
							.getNode(e.getSource()))
					.collect(Collectors.toList()));
			
			Set<Flag> allFlagsExcept = new HashSet<>(allFlags);
			allFlagsExcept.removeAll(flags);
			WarpNetwork<WarpNode, FlaggedEdge<WarpNode>> collapsedNetwork = network.collapse(allFlagsExcept);
			Set<NodeGroup<WarpNode>> collapsedSourceTiers = collapsedNetwork.getUnreturnableNetwork().getSourceNodes();
			
			component:
			for (NodeGroup<WarpNode> component : components)
			{
				Set<WarpNode> componentTargets = new HashSet<>(targets.stream().filter(n -> component.getNodes().contains(n) && !targetTiers.contains(network.getUnreturnableNetwork().getNode(n))).collect(Collectors.toList()));
				Set<WarpNode> componentSources = new HashSet<>(sources.stream().filter(n -> component.getNodes().contains(n) && !sourceTiers.contains(network.getUnreturnableNetwork().getNode(n))).collect(Collectors.toList()));
				
				//Problem is when any of these tiers does not contain none or all of the requirements
				//Or if a component contains any requirements and has two or more softlockable tiers that can be accessed by the same target
				
				//I feel like this is a good idea?
				if (componentTargets.isEmpty() || componentSources.isEmpty()) continue;
				
				//Could move a lot of this over to the network classes
				Set<NodeGroup<WarpNode>> collapsedComponents = new HashSet<>();
				for (WarpNode node : component.getNodes()) collapsedComponents.add(collapsedNetwork.getComponentNetwork().getNode(node));
				
				for (NodeGroup<WarpNode> collapsedComponent : collapsedComponents)
				{
					Set<NodeGroup<WarpNode>> collapsedTiers = new HashSet<>();
					for (WarpNode node : collapsedComponent.getNodes()) collapsedTiers.add(collapsedNetwork.getUnreturnableNetwork().getNode(node));
					
					//We can find branches that split components here
					//This is important right?
					//It doesn't have to split a component, only prevent travel until you have a permission
					//eg cherrygrove and the cut tree on route 30. one way down, but needs cut to get back up (or the egg key item and elms lab)
					
					for (NodeGroup<WarpNode> collapsedTier : collapsedTiers)
						if (!collapsedSourceTiers.contains(collapsedTier) && collapsedTier.getNodes().stream().noneMatch(n -> sources.contains(n)))
					{
						//Houston we have a problem
						//Okay only if we can't hide this tier behind a conditional branch and none of the flags are present
						//Or all of the flags are present
						
						//If all the flags are here then this tier is okay
						
						
						int a = 0;
						
						//We still need to be able to find branches that split a component in two
					}
					
					//We still don't know if this component is only progressible with permissions yet
					//If it is and there's only one tier, we can use this branch to hide other problematic tiers behind
					//How do we know that we don't already have perms when we get here
					//Only possible from start warp?
					//Not true, if we have target, flags, branch we know the player must have the flags already
					
					//We don't need to do this analysis for every component if we have a single conditional branch and the flags all in once place?
					//We might have to for the start warp
					//I think we can definitely skip over doing this for every component if we have a couple conditions met
				}
				
				//Could get the collapsed components, if there's more than one we have a problem
				//Take each conditional branch and check if the source and target are in the same tier?
				
				Set<NodeGroup<WarpNode>> softlockableTiers = new HashSet<>();
				
				
				
				//If we reach this point, 
			}
			
			//Fail if start warp can access a collapsed bottom tier with no sources that does not have all requirements in
			//Fail if start warp can access two or more collapsed bottom tiers with no sources
			//Doesn't this also mean we need to be able to have other perms available to make connections?
			
			//Find all components that contain a flag requirement
			Set<NodeGroup<WarpNode>> requirementComponents = new HashSet<>();
			
			for (Flag flag : flags)
				if (flagRequirements.containsKey(flag) && flagRequirements.get(flag).size() > 0)
					for (WarpNode node : flagRequirements.get(flag))
						requirementComponents.add(network.getComponentNetwork().getNode(node));
			
			//Find the tiers within these components that are blocked
			Set<NodeGroup<WarpNode>> blockedTiers = new HashSet<>();
			for (NodeGroup<WarpNode> component : requirementComponents)
			{
				Set<WarpNode> requirements = new HashSet<>();
				for (Flag flag : flags)
					if (flagRequirements.containsKey(flag) && flagRequirements.get(flag).size() > 0)
						for (WarpNode node : flagRequirements.get(flag))
							if (component.getNodes().contains(node))
								requirements.add(node);
				
				Set<WarpNode> componentSources = new HashSet<>(sources.stream().filter(n -> component.getNodes().contains(n)).collect(Collectors.toList()));
				
				requirementLoop:
				for (WarpNode requirement : requirements)
				{
					//We kinda need to know the exact flags
					//If there's no way to get from requirement to source then we just skip this source
					//If there's a way to get there without using any of the flags in flags then we skip this requirement
					//Otherwise we find the bottom tier in the collapsed network, which should be blocked by the conditional branch
					//Every bottom tier needs to obey the criterion?
					
					for (WarpNode source : componentSources)
					{
						List<Set<Flag>> pathFlags = explorer.getFlagsForPath(requirement, source);
						
						if (pathFlags.isEmpty()) continue;
						else if (pathFlags.stream().anyMatch(s -> s.stream().noneMatch(f -> flags.contains(f)))) continue requirementLoop;
						else
						{
							//Otherwise the only way to progress after the requirement is by using the requirement
							//This isn't true is it?
							//Does it actually have to be downstream from the requirement?
							//As long as it exists anywhere it's a potential problem
							//If the player can get there without the requirement then it's a problem
							
							//We can specifically find components which use the flagged edges in quwestion
							//Filter the edges for the flags and find the corresponding components
						}
					}
					
					
				}
			}
			
			
			
			
		}
		
		return false;
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