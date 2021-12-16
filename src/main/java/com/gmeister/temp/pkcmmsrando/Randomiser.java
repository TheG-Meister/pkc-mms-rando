package com.gmeister.temp.pkcmmsrando;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
	
	public void shuffleWarpGroups(List<List<Warp>> warpGroups, boolean allowSelfWarps, boolean twoWay)
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
		
		for (int i = 0; i < oldGroups.size(); i++)
		{
			List<Warp> oldGroup = oldGroups.get(i);
			List<Warp> newGroup = newGroups.get(i);
			for (int j = 0; j < oldGroup.size(); j++) oldGroup.get(j).setDestination(newGroup.get(j % newGroup.size()));
		}
	}
	
	public void buildWarpGroups(List<List<Warp>> warpGroups, java.util.Map<List<Warp>, List<List<Warp>>> accessibleGroups, List<Warp> startingGroup)
	{
		
		Random random = new Random(this.random.nextLong());
		
		for (List<Warp> warpGroup : warpGroups) if (!accessibleGroups.containsKey(warpGroup)) throw new IllegalArgumentException("accessibleGroups does not contain a key for every group");
		for (List<Warp> warpGroup : accessibleGroups.keySet()) if (!warpGroups.contains(warpGroup)) throw new IllegalArgumentException("accessibleGroups contains groups that are not present in warpGroups");
		if (!warpGroups.contains(startingGroup)) throw new IllegalArgumentException("warpGroups does not contain startingGroup");
		
		if (warpGroups.size() % 2 != 0) throw new IllegalArgumentException("Could not avoid self warps as there are an odd number of groups");
		
		//Make warp group groups
		List<List<List<Warp>>> warpGroupGroups = new ArrayList<>();
		for (List<Warp> warpGroup : warpGroups)
		{
			//Find an existing warp group group that contains this warp group, or any warp group that this warp group can access. Otherwise, make a new group 
			List<List<Warp>> warpGroupGroup = warpGroupGroups.stream()
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
		 * Hard coded restrictions:
		 * Number of creatable branches is equal to the number of nodes (all modes)
		 * Each node can only have one new branch originate from it (all modes)
		 * Each node can only have one new branch lead to it (one-in one-out and two-way mode)
		 * Each time a node's branch is created, the equal and opposite branch must also be created (two-way mode)
		 * Every node needs a branch from and to it (no softlock mode)
		 * 
		 * The most effective branch to add solves the most of these branches at once
		 * Any branch you add also needs to obey these criteria
		 * Branches don't add anything of use to an optimal network if they are possible via another path
		 * Branches cause bad overworlds if you can't reverse them via another path
		 * 
		 * In addition, every node needs a branch going from it and a branch going to it
		 * 
		 * Every node must have a branch arrive at it and leave from it
		 * Each branch must be able to be reversed by any other path
		 * Every node must be part of the same component
		 */
		
		/*for (List<Warp> warpGroup : warpGroups)
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
		}*/
		
		class Branch
		{
			List<Warp> sourceGroup;
			List<Warp> destGroup;
			List<List<Warp>> groupsAbove = new ArrayList<>();
			List<List<Warp>> groupsBelow = new ArrayList<>();
			
			public Branch(List<Warp> sourceGroup, List<Warp> destGroup, List<List<Warp>> groupsAbove,
					List<List<Warp>> groupsBelow)
			{
				this.sourceGroup = sourceGroup;
				this.destGroup = destGroup;
				this.groupsAbove = groupsAbove;
				this.groupsBelow = groupsBelow;
			}
		}
		
		List<Branch> oneWayBranches = new ArrayList<>();
		List<List<Branch>> forks = new ArrayList<>();
		List<List<Branch>> merges = new ArrayList<>();
		
		//Collect any unreturnable branches and the criteria for solving them
		for (List<Warp> groupAbove : warpGroups)
		{
			branch:
			for (List<Warp> groupBelow : accessibleGroups.get(groupAbove))
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
				
				Branch branch = new Branch(groupAbove, groupBelow, groupsAbove, groupsBelow);
				
				for (Branch otherBranch : oneWayBranches) if (
						otherBranch.groupsBelow.containsAll(branch.groupsBelow) &&
						branch.groupsBelow.containsAll(otherBranch.groupsBelow) &&
						otherBranch.groupsAbove.equals(branch.groupsAbove) &&
						branch.groupsAbove.equals(otherBranch.groupsAbove))
					break branch;
				
				for (Branch otherBranch : oneWayBranches)
				{
					if (otherBranch.groupsBelow.containsAll(branch.groupsBelow) &&
							branch.groupsBelow.containsAll(otherBranch.groupsBelow) &&
							!branch.groupsAbove.stream().anyMatch(g -> otherBranch.groupsAbove.contains(g)))
					{
						List<Branch> merge = merges.stream().filter(m -> m.contains(otherBranch)).findFirst().orElse(new ArrayList<>());
						merge.add(branch);
						if (!merge.contains(otherBranch)) merge.add(otherBranch);
						if (!merges.contains(merge)) merges.add(merge);
					}
					
					if (branch.groupsAbove.equals(otherBranch.groupsAbove) &&
							otherBranch.groupsAbove.equals(branch.groupsAbove) &&
							!branch.groupsBelow.stream().anyMatch(g -> otherBranch.groupsBelow.contains(g)))
					{
						List<Branch> fork = forks.stream().filter(f -> f.contains(otherBranch)).findFirst().orElse(new ArrayList<>());
						fork.add(branch);
						if (!fork.contains(otherBranch)) fork.add(otherBranch);
						if (!forks.contains(fork)) forks.add(fork);
					}
				}
				
				oneWayBranches.add(branch);
			}
		}
		
		int requiredLoops = Math.max(forks.stream().mapToInt(f -> f.size() - 1).sum(), merges.stream().mapToInt(m -> m.size() - 1).sum()) + ((oneWayBranches.size() > 0) ? 1 : 0);
		int availableLoops = Math.floorDiv(warpGroupGroups.stream().mapToInt(g -> g.size() - 2).sum() + 2, 2);
		if (availableLoops < requiredLoops) throw new IllegalArgumentException("accessibleGroups does not contain enough connections for a completable 2-way randomiser");
		
		/*
		 * When a 1-way branch has the same warps above and below, it's already accessible
		 * Actually more generally it's solved when any single warp is present both above and below
		 * This occurrs when one of the warps below is connected to one of the warps above
		 * This is also when we know the 1-way path is solved after randomisation
		 * Once this happens, remove the branch
		 * This could also be provided through accessibleGroups, but won't be using the way I make it
		 */
		
		/*
		 * Skip the next possible branch
		 * if any tracked 1-way branch loses its last node above or below without being solved
		 * if any node or set of nodes creates a self-containd component (no free branches in and out) unless everything is part of the same component
		 * if it is a two-way randomiser and there are 
		 * not enough loops are left
		 * 
		 * Each time a branch is created
		 * if the branch originates from a dead end group, remove the dead end group
		 * if the branch leads to an inacessible group, remove the inaccessible group
		 * if the branch originates from a node below and leads to a node above a tracked 1-way branch, remove the 1-way branch
		 * else, if the branch leads to a node above a 1-way branch, remove the destination node from the nodes above and add the source node if it is open as a destination
		 * if the branch originates from a node below a 1-way branch, remove the source node from nodes below and add the destination node if it has a free branch
		 * if it does not do the above and it cannot already be undone, add it to the 1-way branches
		 * 
		 * A loop is made when:
		 * A connection is made within a warp cluster instead of between them
		 * A one-way branch is placed in parallel to an existing one instead of in series with it
		 */
		
		boolean allowSelfWarps = false;
		boolean twoWay = true;
		boolean oneIn = true;
		
		List<List<List<Warp>>> warpClusters = new ArrayList<>();
		warpGroupGroups.stream().forEach(g -> warpClusters.add(new ArrayList<>(g)));
		int loopsCreated = 0;
		boolean pendingSelfWarp = false;
		boolean testedAllSources = false;
		
		//Create a random list of destinations
		List<List<Warp>> sources = new ArrayList<>(warpGroups);
		List<List<Warp>> dests = new ArrayList<>(warpGroups);
		if (oneIn) Collections.shuffle(dests, random);
		
		//Pull random destinations
		List<List<Warp>> newSources = new ArrayList<>();
		List<List<Warp>> newDests = new ArrayList<>();
		
		for (int i = 0; i < sources.size();)
		{
			List<Warp> source = sources.get(i);
			List<List<Warp>> sourceCluster = warpClusters.stream().filter(c -> c.contains(source)).findFirst().orElseThrow();
			final List<List<Warp>> localDests;
			
			if (oneIn) localDests = dests;
			else
			{
				localDests = new ArrayList<>(warpGroups);
				Collections.shuffle(dests, random);
			}
			
			sourceLoops:
			while (true)
			{
				for (List<Warp> dest : localDests) 
				{
					if (!allowSelfWarps && source == dest) continue;
					
					List<List<Warp>> destCluster = warpClusters.stream().filter(c -> c.contains(dest)).findFirst().orElseThrow();
					
					if (warpClusters.size() > 1)
					{
						int limit;
						if (source == dest) limit = 2;
						else if (twoWay) limit = 3;
						else limit = 2;
						
						if (sourceCluster == destCluster)
						{
							if (loopsCreated >= availableLoops) continue;
							
							if (sourceCluster.stream().filter(g -> sources.contains(g)).count() < limit) continue;
							if (sourceCluster.stream().filter(g -> localDests.contains(g)).count() < limit) continue;
						}
						else if (warpClusters.size() > 2)
						{
							if (sourceCluster.stream().filter(g -> sources.contains(g)).count() + destCluster.stream().filter(g -> sources.contains(g)).count() < limit) continue;
							if (sourceCluster.stream().filter(g -> localDests.contains(g)).count() + destCluster.stream().filter(g -> localDests.contains(g)).count() < limit) continue;
						}
					}
					
					if (source == dest)
					{
						if (!pendingSelfWarp) loopsCreated++;
						pendingSelfWarp = !pendingSelfWarp;
					} 
					else if (sourceCluster == destCluster) loopsCreated++;
					
					sources.remove(source);
					localDests.remove(dest);
					newSources.add(source);
					newDests.add(dest);
					
					if (twoWay)
					{
						sources.remove(dest);
						newSources.add(dest);
						localDests.remove(source);
						newDests.add(source);
					}
					
					if (sourceCluster != destCluster)
					{
						sourceCluster.addAll(destCluster);
						warpClusters.remove(destCluster);
					}
					
					break sourceLoops;
				}
				
				throw new IllegalStateException("Could not find a destionation warp for source " + source);
			}
			
			if (!allowSelfWarps && !testedAllSources)
			{
				while (i < sources.size() && newDests.contains(sources.get(i))) i++;
				if (i >= sources.size() && !testedAllSources)
				{
					i = 0;
					testedAllSources = true;
				}
			}
			//if you get here and sources is not empty, throw an error
			else if (!sources.isEmpty()) throw new IllegalStateException("Could not find a destionation warps for sources " + sources);
			
		}
		
		for (List<Warp> warpGroup : warpGroups)
		{
			List<List<Warp>> groupsBelow = new ArrayList<>();
			groupsBelow.add(newDests.get(newSources.indexOf(warpGroup)));
			
			for (int i = 0; i < groupsBelow.size(); i++)
			{
				groupsBelow.addAll(accessibleGroups.get(groupsBelow.get(i)).stream()
						.filter(g -> !groupsBelow.contains(g))
						.collect(Collectors.toList()));
				if (!groupsBelow.contains(newDests.get(newSources.indexOf(groupsBelow.get(i))))) groupsBelow.add(newDests.get(newSources.indexOf(groupsBelow.get(i))));
			}
			
			System.out.print(warpGroup.get(0).getPosition());
			for (List<Warp> otherGroup : warpGroups) System.out.print("\t" + (groupsBelow.contains(otherGroup) ? 1 : 0));
			System.out.println();
		}
		
		for (int i = 0; i < newSources.size(); i++)
		{
			List<Warp> source = newSources.get(i);
			List<Warp> dest = newDests.get(i);
			for (int j = 0; j < source.size(); j++) source.get(j).setDestination(dest.get(j % dest.size()));
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