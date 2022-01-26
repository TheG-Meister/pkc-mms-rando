package com.gmeister.temp.pkcmmsrando.rando;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import com.gmeister.temp.pkcmmsrando.map.data.Warp;
import com.gmeister.temp.pkcmmsrando.map.data.WarpNetwork;
import com.gmeister.temp.pkcmmsrando.map.data.WarpNetwork.Branch;

public class WarpRandomiser
{
	
	private Random random;
	
	/**
	 * Initialises a Randomiser with a new Random.
	 */
	public WarpRandomiser()
	{
		this.random = new Random();
	}
	
	/**
	 * Initialises a Randomiser with the provided Random.
	 * @param random the Random to initialise with.
	 */
	public WarpRandomiser(Random random)
	{
		this.random = random;
	}
	
	public void buildWarpGroups(List<List<Warp>> warpGroups, Map<List<Warp>, List<List<Warp>>> accessibleGroups, List<Warp> startingGroup)
	{
		boolean allowSelfWarps = false;
		boolean twoWay = true;
		boolean oneIn = true;
		
		Random random = new Random(this.random.nextLong());
		
		for (List<Warp> warpGroup : warpGroups) if (!accessibleGroups.containsKey(warpGroup)) throw new IllegalArgumentException("accessibleGroups does not contain a key for every group");
		for (List<Warp> warpGroup : accessibleGroups.keySet()) if (!warpGroups.contains(warpGroup)) throw new IllegalArgumentException("accessibleGroups contains groups that are not present in warpGroups");
		if (!warpGroups.contains(startingGroup)) throw new IllegalArgumentException("warpGroups does not contain startingGroup");
		
		if (warpGroups.size() % 2 != 0) throw new IllegalArgumentException("Could not avoid self warps as there are an odd number of groups");
		
		//Create a copy of the provided network and remove redundant branches
		Map<List<Warp>, List<List<Warp>>> networkMap = new HashMap<>();
		for (List<Warp> key : accessibleGroups.keySet()) networkMap.put(key, new ArrayList<>(accessibleGroups.get(key)));
		WarpNetwork network = new WarpNetwork(networkMap);
		
		boolean testedAllSources = false;
		
		//Create a random list of destinations
		List<List<Warp>> sources = new ArrayList<>(warpGroups);
		List<List<Warp>> targets = new ArrayList<>(warpGroups);
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
				if (sources.containsAll(warpGroups)) throw new IllegalArgumentException("accessibleGroups does not contain enough connections to fulfil all provided settings");
				//If this is any other loop, the coded logic is wrong
				else throw new IllegalStateException("Too many optional branches were created");
			}
			
			List<Warp> source = sources.get(sourceIndex);
			
			final List<List<Warp>> localTargets;
			if (oneIn) localTargets = targets;
			else
			{
				localTargets = new ArrayList<>(warpGroups);
				Collections.shuffle(targets, random);
			}
			
			targetLoop:
			for (List<Warp> target : localTargets) 
			{
				if (!allowSelfWarps && source == target) continue targetLoop;
				
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
			
			if (!allowSelfWarps && !testedAllSources)
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
		
		for (List<Warp> warpGroup : warpGroups)
		{
			List<List<Warp>> groupsBelow = network.getAllAccessees(warpGroup);
			
			System.out.print(warpGroup.get(0).getPosition());
			for (List<Warp> otherGroup : warpGroups) System.out.print("\t" + (groupsBelow.contains(otherGroup) ? 1 : 0));
			System.out.println();
		}
		
		for (int i = 0; i < newSources.size(); i++)
		{
			List<Warp> source = newSources.get(i);
			List<Warp> dest = newTargets.get(i);
			for (int j = 0; j < source.size(); j++) source.get(j).setDestination(dest.get(j % dest.size()));
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
	
}