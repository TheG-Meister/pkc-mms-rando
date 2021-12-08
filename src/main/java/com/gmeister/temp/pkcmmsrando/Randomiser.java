package com.gmeister.temp.pkcmmsrando;

import java.util.ArrayList;
import java.util.Arrays;
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
				for (ArrayList<Warp> oldGroup : groups) if (allowSelfWarps || ((!newGroups.contains(oldGroup) || testedAllOldDestsForThisGroup) && !oldGroup.equals(newGroup))) 
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
	
	public void buildWarpGroups(ArrayList<ArrayList<Warp>> warpGroups, HashMap<ArrayList<Warp>, ArrayList<ArrayList<Warp>>> accessibleGroups, ArrayList<Warp> startingGroup)
	{
		
		Random random = new Random(this.random.nextLong());
		
		for (ArrayList<Warp> warpGroup : warpGroups) if (!accessibleGroups.containsKey(warpGroup)) throw new IllegalArgumentException("accessibleGroups does not contain a key for every group");
		for (ArrayList<Warp> warpGroup : accessibleGroups.keySet()) if (!warpGroups.contains(warpGroup)) throw new IllegalArgumentException("accessibleGroups contains groups that are not present in warpGroups");
		if (!warpGroups.contains(startingGroup)) throw new IllegalArgumentException("warpGroups does not contain startingGroup");
		
		if (warpGroups.size() % 2 != 0) throw new IllegalArgumentException("Could not avoid self warps as there are an odd number of groups");
		
		//Make warp group groups
		ArrayList<ArrayList<ArrayList<Warp>>> warpGroupGroups = new ArrayList<>();
		for (ArrayList<Warp> warpGroup : warpGroups)
		{
			//Find an existing warp group group that contains this warp group, or any warp group that this warp group can access. Otherwise, make a new group 
			ArrayList<ArrayList<Warp>> warpGroupGroup = warpGroupGroups.stream()
					.filter(g -> g.contains(warpGroup))
					.findFirst()
					.orElse(warpGroupGroups.stream()
							.filter(g -> accessibleGroups.get(warpGroup).stream()
									.anyMatch(h -> g.contains(h)))
							.findFirst()
							.orElse(new ArrayList<>()));
			
			//If the warp group group isn't part of the list, add it
			if (!warpGroupGroups.contains(warpGroupGroup)) warpGroupGroups.add(warpGroupGroup);
			
			//If this warp group isn't part of the warp group group, add it
			if (!warpGroupGroup.contains(warpGroup)) warpGroupGroup.add(warpGroup);
			
			//Add all of the warps groups that this warp group can access to the warp group group if they are not already present
			warpGroupGroup.addAll(accessibleGroups.get(warpGroup).stream()
					.filter(g -> !warpGroupGroup.contains(g))
					.collect(Collectors.toList()));
		}
		
		/*
		 * How to we know that in blackthorn, 4 must connect to 1 or 2?
		 * Well, we know that 4 is below the ledge as it can't access 1, 2 and 3, which can all access it
		 * We know that 3 must be below a ledge from 1 and 2, as it can't access them but they can access it
		 * We know that 1 and 2 can access each other and therefore aren't considered to be separate (imagine tin tower ledge hopping)
		 * 3 must be de-throned by 1 and 2 as they can both access it
		 * We then employ the rule where both of the ones at the top cannot open a one way path and one at the bottom cannot close a 1-way path
		 * 
		 * In alph outside, how do we know that 1 and 2 must connect to 3 and 4?
		 * 1 and 2 must be below a ledge from 3 and 4 from the same reasons as above
		 * 3 must be on a different ledge from 4 as 3 cannot access 4
		 * 4 must be on a different ledge from 3 as 4 cannot access 3
		 * 
		 * According to my network diagram, the lines that are there cannot be changed
		 * We want every node to be accessible from every other node
		 * The only change we can make is we can create one extra branch from every accessible node
		 * This means that each node must be accessible either from a pre-existing branch, or a branch we create
		 * We need enough nodes to create enough branches for this
		 * We also need to make sure that each time there is 1-way travel between nodes, there is an alternate path backwards
		 * 
		 * In the case of blackthorn, we can see that 3 can access 4, 1 can access 2 & 3, and 2 can access 1 & 3
		 * Therefore if 4 can follow branches to 1 or 2, the 1-ways to 3 will be accessible
		 * 
		 * For each node, every path that you leave from must be able to return to the node
		 * Consider branches that leave the node and branches that join the node when adding branches
		 * 
		 * Delegate the solving of each one way branch to the lowest free nodes on the branch
		 * A fork solves a split because it turns 2 1-way systems into 1 1-way system
		 */
		
		//Get the sum of each group size minus 2. If it's greater than or equal to zero we have a rom
		//More complicated:
		/*
		 * If there are literally zero one-way movements, add 2 to the sum (make a tree instead of a circle)
		 * For each warp group that is inaccessible from all other warp groups within the warp group group, remove another 2 from the sum
		 */
		//int accessibleSum = warpGroupGroups.stream().mapToInt(g -> g.size() - 2).sum();
		//if (accessibleSum < 0) throw new IllegalArgumentException("Sum of accessible warps in an optimal map is less than zero: " + accessibleSum);
		
		/*
		 * Wanna do two things
		 * Not link within overworld fragments until all fragments are connected
		 * Orientate fragments so they all go around in a circle
		 * 
		 * Each time you link warps within overworld fragments instead of between overworld fragments, you're removing warps from the pool
		 * Each time you split a one way system or join two different one way systems, you're removing warps from the pool
		 * Wait, we're literally trying to count the number of loops, right?
		 * 
		 * If we calc this sum, then we know exactly the number of loops we are forced to make, 1-way or 2-way.
		 * More importantly, sometimes loops are optional and other times they are forced
		 * 
		 * One loop is forced if any single map or warp is 1-way
		 * An extra loop is forced for every additional fork of 1-way system (as defined by accessibleGroups)
		 * 
		 * An optional loop is made each time a connection is made within an overworld fragment
		 * An optional loop is made each time a fork is artificially generated in a 1-way system, by placing 1-way maps
		 * 
		 * Wait okay we didn't technically create an extra loop by placing 1-way maps but the theory still works.
		 * Basically, doing this means we need an extra loop for a completable rom.
		 * 
		 * Let's do this differently
		 * Count the number of loops we're forced to make
		 * Does a house count as a loop in 2-way rando? Yes
		 * The ledge stuff also contributes. EG. the typical splitter has 3 warps but opens 2 loops.
		 * 
		 * What hapens if we think about branches instead?
		 * In 2-way rando:
		 * Houses kill a branch
		 * Corridors continue a branch
		 * Anything with more than 2 warps creates branches
		 * The presence of any 1-way progression requires 2 branches to join up
		 * 
		 * What if we count the number of branches created and the number of branches required?
		 * Probably better to consider paths?
		 * 
		 * What about creating and consuming 1-way branches?
		 * A 2-way branch is just a possibility of a 1-way rando that we can account for
		 * Warps that are pits or are blocked by ledges either cannot create or consume a 1-way branch
		 */
		
		/*
		 * I want a way to store the delegation of 1-way solving
		 * For each 1-way we have to solve, record what warps below can solve it, and what warps above can solve it
		 */
		
		/*
		 * What does the existence of any branch do to the loop count in a created rom?
		 * Hmmm what if we think of the minimum number of branches required to solve the rom?
		 * 
		 * Hard coded restrictions:
		 * Number of creatable branches is equal to the number of nodes (all modes)
		 * Each node can only have one new branch originate from it (all modes)
		 * Each node can only have one new branch lead to it (one-in one-out and two-way mode)
		 * Each time a node's branch is created, the equal and opposite branch must also be created (two-way mode)
		 * Every node needs a branch from and to it (no softlock mode)
		 * 
		 * Technically we get to remove a set of branches and re-assign each one, but this will do
		 * I think I'm trying to use stats from individual branches to calculate stats for the network as a whole
		 * 
		 * Stats of a network:
		 * Number of nodes
		 * Number of existing branches
		 * Number of non-redundant existing branches
		 * 
		 * The most effective branch to add solves the most of these branches at once
		 * Any branch you add also needs to obey these criteria
		 * Branches don't add anything of use to an optimal network if they are possible via another path
		 * Branches cause bad overworlds if you can't reverse them via another path
		 * 
		 * In addition, every node needs a branch going from it and a branch going to it
		 * 
		 * One thing we could do is keep track of all nodes that can be accessed by any other node, including via multiple branches
		 * Essentially we "compress" multiple branches into a single branch just for analysis purposes
		 * The ROM is complete when the list of accessible nodes from each node contains every node
		 * 
		 * However, with over 800 warps this will be a funckin huge table that eats up very unnecessary amounts of memory
		 * 
		 * Actually to save memory it might be a good idea to cut down on the numbers of branches.
		 * 
		 * Every node must have a branch arrive at it and leave from it
		 * Each branch must be able to be reversed by any other path
		 * Every node must be part of the same component
		 */
		
		/*
		 * Probably make a copy of accessible warps
		 * For every node
		 * Follow through every branch from the node
		 * For each branch
		 * If its destination node is accessible from any other branch, delete this branch
		 */
		
		for (List<Warp> warpGroup : warpGroups)
		{
			java.util.Map<List<Warp>, List<List<Warp>>> downstreamGroupsMap = new HashMap<>();
			for (List<Warp> accessibleGroup : accessibleGroups.get(warpGroup))
			{
				List<List<Warp>> downstreamGroups = new ArrayList<>();
				downstreamGroups.addAll(accessibleGroups.get(accessibleGroup));
				
				//BEWARE, the size of the list changes during this loop
				for (int i = 0; i < downstreamGroups.size(); i++)
					downstreamGroups.addAll(accessibleGroups.get(downstreamGroups.get(i)).stream()
							.filter(g -> !downstreamGroups.contains(g))
							.collect(Collectors.toList()));
				
				downstreamGroupsMap.put(accessibleGroup, downstreamGroups);
			}
			
			List<List<Warp>> currentlyAccessibleGroups = new ArrayList<>(accessibleGroups.get(warpGroup));
			for (List<Warp> accessibleGroup : currentlyAccessibleGroups)
			{
				if (downstreamGroupsMap.entrySet().stream()
						.filter(e -> !e.getKey().equals(accessibleGroup))
						.anyMatch(e -> e.getValue().contains(accessibleGroup)))
				{
					//This group is accessible via any other combination of branches
					//System.out.println(warpGroup.get(0).getPosition() + "\t" + accessibleGroup.get(0).getPosition());
					accessibleGroups.get(warpGroup).remove(accessibleGroup);
					downstreamGroupsMap.remove(accessibleGroup);
				}
			}
		}
		
		for (List<Warp> groupAbove : warpGroups)
		{
			branch:
			for (List<Warp> groupBelow : accessibleGroups.get(groupAbove)) if (!accessibleGroups.get(groupBelow).contains(groupAbove))
			{
				//Exhaustive search algorithm to find free warps... above and below?
				List<List<Warp>> groupsBelow = new ArrayList<>(Arrays.asList(groupBelow));
				for (int i = 0; i < groupsBelow.size(); i++)
				{
					groupsBelow.addAll(accessibleGroups.get(groupsBelow.get(i)).stream()
							.filter(g -> !groupsBelow.contains(g))
							.collect(Collectors.toList()));
					if (groupsBelow.contains(groupAbove)) break branch; 
				}

				List<List<Warp>> groupsAbove = new ArrayList<>(Arrays.asList(groupAbove));
				for (int i = 0; i < groupsAbove.size(); i++)
				{
					final int j = i;
					groupsAbove.addAll(accessibleGroups.entrySet().stream()
							.filter(e -> e.getValue().contains(groupsAbove.get(j)) && !groupsAbove.contains(e.getKey()))
							.map(e -> e.getKey())
							.collect(Collectors.toList()));
					if (groupsAbove.contains(groupBelow)) break branch; 
				}
				
				System.out.println(groupAbove.get(0).getPosition() + "\t" +
				groupsAbove.stream().map(g -> g.get(0).getPosition()).collect(Collectors.toList()) + "\t" +
						groupsBelow.stream().map(g -> g.get(0).getPosition()).collect(Collectors.toList()));
			}
		}
		
		/*
		 * this is great
		 * How do we know the number of loops this will make?
		 * idk tbh
		 * We can group these
		 * Any connection with the same warps below and above is all part of the same ledge
		 * When this happens, merge the groups
		 * 
		 * When a 1-way branch has the same warps above and below, it's already accessible
		 * Actually more generally it's solved when any single warp is present both above and below
		 * This occurrs when one of the warps below is connected to one of the warps above
		 * This is also when we know the 1-way path is solved after randomisation
		 * Once this happens, remove the branch
		 * This could also be provided through accessibleGroups, but won't be using the way I make it
		 * 
		 * There is a merger when none of the warps above are the same, but all of the warps below are the same
		 * There is a fork when all of the warps above are the same, but none of the warps below are the same
		 * 
		 * Multiple branches can be solved (like, solved solved) at once if they share a warp below and above (by connecting them)
		 * That can be done with a simple set of if statements, but finding solutions like that might require more work
		 */
		
		//Create a random list of groups
		ArrayList<ArrayList<Warp>> shuffledGroups = new ArrayList<>(warpGroups);
		Collections.shuffle(shuffledGroups, random);
		ArrayList<ArrayList<Warp>> freeGroups = new ArrayList<>();
		freeGroups.add(startingGroup);
		freeGroups.addAll(accessibleGroups.get(startingGroup));
		
		//Pull random destinations
		ArrayList<ArrayList<Warp>> oldGroups = new ArrayList<>();
		ArrayList<ArrayList<Warp>> newGroups = new ArrayList<>();
		
		while (shuffledGroups.size() > 0)
		{
			ArrayList<Warp> oldGroup = freeGroups.get(0);
			
			oldGroupLoops:
			while (true)
			{
				for (ArrayList<Warp> newGroup : shuffledGroups) if (oldGroup != newGroup || shuffledGroups.size() < 2)
				{
					ArrayList<ArrayList<Warp>> nextFreeGroups = new ArrayList<>(freeGroups);
					for (ArrayList<Warp> freeGroup : accessibleGroups.get(newGroup)) if (shuffledGroups.contains(freeGroup)) nextFreeGroups.add(freeGroup);
					nextFreeGroups.remove(oldGroup);
					nextFreeGroups.remove(newGroup);
					
					if (nextFreeGroups.size() > 0 || shuffledGroups.size() < 2)
					{
						oldGroups.add(oldGroup);
						newGroups.add(newGroup);
						
						oldGroups.add(newGroup);
						newGroups.add(oldGroup);
						
						shuffledGroups.remove(newGroup);
						shuffledGroups.remove(oldGroup);
						
						freeGroups = nextFreeGroups;
						
						break oldGroupLoops;
					}
				}
				
				throw new IllegalStateException("Could not find a destination for " + oldGroup.get(0));
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
				for (Warp oldDest : destinations) if (allowSelfWarps || ((!newDests.contains(oldDest) || testedAllOldDestsForThisWarp) && !oldDest.equals(newDest))) 
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