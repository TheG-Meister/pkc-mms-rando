package com.gmeister.temp.pkcmmsrando.rando;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import com.gmeister.temp.pkcmmsrando.map.data.Warp;

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
	
	public List<List<Warp>> getAllAccessors(List<Warp> warpGroup, Map<List<Warp>, List<List<Warp>>> network)
	{
		List<List<Warp>> accessors = new ArrayList<>();
		accessors.add(warpGroup);
		
		//BEWARE, the size of the list changes during this loop
		for (int i = 0; i < accessors.size(); i++)
		{
			final int j = i;
			accessors.addAll(network.entrySet().stream()
					.filter(e -> e.getValue().contains(accessors.get(j)) && !accessors.contains(e.getKey()))
					.map(e -> e.getKey())
					.distinct()
					.collect(Collectors.toList()));
		}
		
		return accessors;
	}
	
	public List<List<Warp>> getAllAccessees(List<Warp> warpGroup, Map<List<Warp>, List<List<Warp>>> network)
	{
		List<List<Warp>> accessees = new ArrayList<>();
		accessees.add(warpGroup);
		
		//BEWARE, the size of the list changes during this loop
		for (int i = 0; i < accessees.size(); i++)
			accessees.addAll(network.get(accessees.get(i)).stream()
					.filter(g -> !accessees.contains(g))
					.distinct()
					.collect(Collectors.toList()));
		
		return accessees;
	}
	
	public boolean canAccess(List<Warp> warpGroup, List<Warp> otherGroup, Map<List<Warp>, List<List<Warp>>> network)
	{
		List<List<Warp>> downstreamGroups = new ArrayList<>();
		//This is intentionally different getAllAccessees to test a group accessing itself
		downstreamGroups.addAll(network.get(warpGroup));
		
		//BEWARE, the size of the list changes during this loop
		for (int j = 0; j < downstreamGroups.size(); j++)
		{
			downstreamGroups.addAll(network.get(downstreamGroups.get(j)).stream()
					.filter(g -> !downstreamGroups.contains(g))
					.distinct()
					.collect(Collectors.toList()));
			if (downstreamGroups.contains(otherGroup)) return true;
		}
		
		return false;
	}
	
	public List<List<List<Warp>>> groupWarpGroups(List<List<Warp>> warpGroups, Map<List<Warp>, List<List<Warp>>> network)
	{
		List<List<List<Warp>>> warpGroupGroups = new ArrayList<>();
		for (List<Warp> warpGroup : warpGroups)
		{
			//Find an existing warp group group that contains this warp group, or any warp group that this warp group can access. Otherwise, make a new group 
			List<List<Warp>> warpGroupGroup = warpGroupGroups.stream()
					.filter(g -> g.contains(warpGroup))
					.findFirst()
					.orElse(warpGroupGroups.stream()
							.filter(g -> network.get(warpGroup).stream()
									.anyMatch(h -> g.contains(h)))
							.findFirst()
							.orElse(new ArrayList<>()));
			
			//If the warp group group isn't part of the list, add it
			if (!warpGroupGroups.contains(warpGroupGroup)) warpGroupGroups.add(warpGroupGroup);
			
			//If this warp group isn't part of the warp group group, add it
			if (!warpGroupGroup.contains(warpGroup)) warpGroupGroup.add(warpGroup);
			
			//Add all of the warps groups that this warp group can access to the warp group group if they are not already present
			warpGroupGroup.addAll(network.get(warpGroup).stream()
					.filter(g -> !warpGroupGroup.contains(g))
					.collect(Collectors.toList()));
		}
		return warpGroupGroups;
	}
	
	public Map<List<Warp>, List<List<Warp>>> removeRedundantBranches(Map<List<Warp>, List<List<Warp>>> network)
	{
		for (List<Warp> warpGroup : network.keySet())
		{
			List<List<Warp>> accessees = network.get(warpGroup);
			List<List<Warp>> allAccessees = this.getAllAccessees(warpGroup, network);
			
			for (int i = 0; i < accessees.size();)
			{
				List<Warp> accessee = accessees.remove(i);
				List<List<Warp>> newTotalAccessees = this.getAllAccessees(warpGroup, network);
				if (!newTotalAccessees.containsAll(allAccessees))
				{
					accessees.add(i, accessee);
					i++;
				}
			}
		}
		
		return network;
	}
	
	public void checkRemovedBranches(Map<List<Warp>, List<List<Warp>>> network, Map<List<Warp>, List<List<Warp>>> newNetwork)
	{
		for (List<Warp> warpGroup : newNetwork.keySet())
		{
			List<List<Warp>> groupsBelow = this.getAllAccessees(warpGroup, network);
			List<List<Warp>> newGroupsBelow = this.getAllAccessees(warpGroup, newNetwork);
			
			if (!groupsBelow.containsAll(newGroupsBelow) || !newGroupsBelow.containsAll(groupsBelow))
				throw new IllegalStateException("Branch removal algorithm removes too many branches");
		}
	}
	
	public void printNetwork(Map<List<Warp>, List<List<Warp>>> network)
	{
		for (List<Warp> warpGroup : network.keySet()) System.out.print(";" + warpGroup);
		System.out.println();
		for (List<Warp> warpGroup : network.keySet())
		{
			List<List<Warp>> accessibleGroups = network.get(warpGroup);
			System.out.print(warpGroup);
			for (List<Warp> otherGroup : network.keySet()) System.out.print(";" + (accessibleGroups.contains(otherGroup) ? 1 : 0));
			System.out.println();
		}
	}
	
	public void buildWarpGroups(List<List<Warp>> warpGroups, Map<List<Warp>, List<List<Warp>>> accessibleGroups, List<Warp> startingGroup)
	{
		boolean allowSelfWarps = true;
		boolean twoWay = true;
		boolean oneIn = true;
		
		Random random = new Random(this.random.nextLong());
		
		for (List<Warp> warpGroup : warpGroups) if (!accessibleGroups.containsKey(warpGroup)) throw new IllegalArgumentException("accessibleGroups does not contain a key for every group");
		for (List<Warp> warpGroup : accessibleGroups.keySet()) if (!warpGroups.contains(warpGroup)) throw new IllegalArgumentException("accessibleGroups contains groups that are not present in warpGroups");
		if (!warpGroups.contains(startingGroup)) throw new IllegalArgumentException("warpGroups does not contain startingGroup");
		
		if (warpGroups.size() % 2 != 0) throw new IllegalArgumentException("Could not avoid self warps as there are an odd number of groups");
		
		//Create a copy of the provided network and remove redundant branches
		Map<List<Warp>, List<List<Warp>>> network = new HashMap<>();
		for (List<Warp> key : accessibleGroups.keySet()) network.put(key, new ArrayList<>(accessibleGroups.get(key)));
		network = this.removeRedundantBranches(network);
		this.checkRemovedBranches(accessibleGroups, network);
		this.printNetwork(network);
		
		//Make warp group groups
		List<List<List<Warp>>> warpGroupGroups = this.groupWarpGroups(warpGroups, network);
		
		//Collect all nodes that have nothing arrive at them
		List<List<Warp>> inaccessibleGroups = new ArrayList<>(warpGroups);
		network.entrySet().stream().map(e -> e.getValue()).forEach(g -> inaccessibleGroups.removeAll(g));
		
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
			for (List<Warp> groupBelow : network.get(groupAbove))
			{
				Branch branch = new Branch(groupAbove, groupBelow, this.getAllAccessors(groupAbove, network), this.getAllAccessees(groupBelow, network));
				
				for (Branch otherBranch : oneWayBranches) if (
						otherBranch.groupsBelow.containsAll(branch.groupsBelow) &&
						branch.groupsBelow.containsAll(otherBranch.groupsBelow) &&
						otherBranch.groupsAbove.containsAll(branch.groupsAbove) &&
						branch.groupsAbove.containsAll(otherBranch.groupsAbove))
					continue branch;
				
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
					
					if (branch.groupsAbove.containsAll(otherBranch.groupsAbove) &&
							otherBranch.groupsAbove.containsAll(branch.groupsAbove) &&
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
		
		int branchesAvailable = warpGroups.size();
		int branchesReserved = Math.max(forks.stream().mapToInt(f -> f.size() - 1).sum(), merges.stream().mapToInt(m -> m.size() - 1).sum());
		if (twoWay)
		{
			branchesReserved += Math.floorDiv(branchesAvailable, 2);
			branchesReserved += warpGroupGroups.size() - 1;
			if (oneWayBranches.size() > 0) branchesReserved++;
		}
		else
		{
			branchesReserved += warpGroupGroups.size() - 1;
			if (warpGroupGroups.size() > 1 || oneWayBranches.size() > 0) branchesReserved++; 
		}
		
		if (branchesAvailable < branchesReserved) throw new IllegalArgumentException("accessibleGroups does not contain enough connections to fulfil all provided settings");
		
		List<List<List<Warp>>> warpClusters = new ArrayList<>();
		warpGroupGroups.stream().forEach(g -> warpClusters.add(new ArrayList<>(g)));
		int optionalBranchesCreated = 0;
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
			
			if (sources.size() < inaccessibleGroups.size()) throw new IllegalStateException("Not enough branches were used in making inaccessible warps accessible");
			
			destLoop:
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
						//This check should be valid regardless of how many warp clusters there are
						if (branchesAvailable - branchesReserved < optionalBranchesCreated) continue;
						
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
					if (twoWay)
					{
						if (!pendingSelfWarp) optionalBranchesCreated++;
						pendingSelfWarp = !pendingSelfWarp;
					}
					else optionalBranchesCreated++;
				} 
				else if (sourceCluster == destCluster) optionalBranchesCreated++;
				
				List<Branch> addedBranches = new ArrayList<>();
				addedBranches.add(new Branch(source, dest, null, null));
				if (twoWay && source != dest) addedBranches.add(new Branch(dest, source, null, null));
				
				for (Branch branch : addedBranches)
				{
					sources.remove(branch.sourceGroup);
					localDests.remove(branch.destGroup);
					newSources.add(branch.sourceGroup);
					newDests.add(branch.destGroup);
					if (inaccessibleGroups.contains(branch.destGroup)) inaccessibleGroups.remove(branch.destGroup);
					if (!network.get(branch.sourceGroup).contains(branch.destGroup)) network.get(source).add(branch.destGroup);
				}
				
				for (int j = 0; j < oneWayBranches.size();)
				{
					Branch branch = oneWayBranches.get(j);
					
					if (this.canAccess(branch.destGroup, branch.sourceGroup, network)) oneWayBranches.remove(branch);
					else j++;
				}
				
				if (sourceCluster != destCluster)
				{
					sourceCluster.addAll(destCluster);
					warpClusters.remove(destCluster);
				}
				
				break destLoop;
			}
			
			if (sources.contains(source)) throw new IllegalStateException("Could not find a destination warp for source " + source);
			
			if (!allowSelfWarps && !testedAllSources)
			{
				while (i < sources.size() && newDests.contains(sources.get(i))) i++;
				if (i >= sources.size())
				{
					i = 0;
					testedAllSources = true;
				}
			}
		}
		
		if (!sources.isEmpty()) throw new IllegalStateException("Not all sources were assigned a destination");
		if (!oneWayBranches.isEmpty()) throw new IllegalStateException("Not all one-way branches were given an alternative path");
		
		for (List<Warp> warpGroup : warpGroups)
		{
			List<List<Warp>> groupsBelow = this.getAllAccessees(warpGroup, network);
			
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