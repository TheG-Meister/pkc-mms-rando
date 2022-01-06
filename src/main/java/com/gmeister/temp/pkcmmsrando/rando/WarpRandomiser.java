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
		boolean allowSelfWarps = false;
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
		//network = this.removeRedundantBranches(network);
		//this.checkRemovedBranches(accessibleGroups, network);
		//this.printNetwork(network);
		
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
		
		List<List<List<Warp>>> warpClusters = new ArrayList<>();
		warpGroupGroups.stream().forEach(g -> warpClusters.add(new ArrayList<>(g)));
		boolean testedAllSources = false;
		
		//Create a random list of destinations
		List<List<Warp>> sources = new ArrayList<>(warpGroups);
		List<List<Warp>> dests = new ArrayList<>(warpGroups);
		if (oneIn) Collections.shuffle(dests, random);
		
		//Pull random destinations
		List<List<Warp>> newSources = new ArrayList<>();
		List<List<Warp>> newDests = new ArrayList<>();
		
		for (int sourceIndex = 0; sourceIndex < sources.size();)
		{
			//Store the number of branches that can be controlled
			int controllableBranches;
			if (twoWay) controllableBranches = Math.floorDiv(sources.size(), 2);
			else controllableBranches = sources.size();
			
			//Store the sum of all one-way branches that are parallel to another in forks and merges
			int forkSum = forks.stream().mapToInt(f -> f.size() - 1).sum();
			int mergeSum = merges.stream().mapToInt(m -> m.size() - 1).sum();
			
			//Store the number of branches necessary to make a completable overworld
			int neededBranches = Math.max(forkSum, mergeSum) + warpClusters.size() - 1;
			if (oneWayBranches.size() > 0 || (!twoWay && warpClusters.size() > 1)) neededBranches++;
			
			//If there are less controllable branches than needed branches, throw an error
			if (controllableBranches < neededBranches)
			{
				//If this is the first loop, the provided network cannot generate a completable overworld
				if (sources.containsAll(warpGroups)) throw new IllegalArgumentException("accessibleGroups does not contain enough connections to fulfil all provided settings");
				//If this is any other loop, the coded logic is wrong
				else throw new IllegalStateException("Too many optional branches were created");
			}
			
			List<Warp> source = sources.get(sourceIndex);
			List<List<Warp>> sourceCluster = warpClusters.stream().filter(c -> c.contains(source)).findFirst().orElseThrow();
			final List<List<Warp>> localDests;
			
			if (oneIn) localDests = dests;
			else
			{
				localDests = new ArrayList<>(warpGroups);
				Collections.shuffle(dests, random);
			}
			
			if (sources.size() < inaccessibleGroups.size()) throw new IllegalStateException("Not enough branches were used in making inaccessible warps accessible");
			
			targetLoop:
			for (List<Warp> target : localDests) 
			{
				/*
				 * Things that should never be allowed:
				 * Depleting a one way warp of sources below or targets above without solving it
				 * Linking 2 of 3+ warp clusters together without either of them having a free source and target afterwards
				 * 
				 * Things that are only allowed if we have some disposable branches:
				 * Creating a fork, if the fork sum is greater than or equal to the merge sum
				 * Creating a merge, if the merge sum is greater than or equal to the fork sum
				 * Reducing the fork sum below the merge sum without solving all of them
				 * Reducing the merge sum below the fork sum without solving all of them
				 * Making a branch within a cluster without solving any one-way warps
				 */
				
				if (!allowSelfWarps && source == target) continue targetLoop;
				
				List<Branch> newBranches = new ArrayList<>();
				newBranches.add(new Branch(source, target, null, null));
				if (twoWay && source != target) newBranches.add(new Branch(target, source, null, null));
				
				//if any branch would consume the last warp below any one way branch without solving it and without adding any more branches, continue
				for (Branch branch : oneWayBranches)
				{
					if (branch.groupsBelow.contains(source) ^ branch.groupsAbove.contains(target))
					{
						List<List<Warp>> freeWarps = branch.groupsBelow.stream().filter(g -> sources.contains(g) && g != source && (!twoWay || g != target)).collect(Collectors.toList());
						this.getAllAccessees(target, network).stream().filter(g -> !freeWarps.contains(g) && g != source && (!twoWay || g != target)).forEach(g -> freeWarps.add(g));
						if (freeWarps.size() < 1) continue targetLoop;
					}
					if (twoWay && (!branch.groupsBelow.contains(target) ^ !branch.groupsAbove.contains(source)))
					{
						List<List<Warp>> freeWarps = branch.groupsBelow.stream().filter(g -> sources.contains(g) && g != source && g != target).collect(Collectors.toList());
						this.getAllAccessees(source, network).stream().filter(g -> !freeWarps.contains(g) && g != source && g != target).forEach(g -> freeWarps.add(g));
						if (freeWarps.size() < 1) continue targetLoop;
					}
				}
				
				List<List<Warp>> destCluster = warpClusters.stream().filter(c -> c.contains(target)).findFirst().orElseThrow();
				
				if (warpClusters.size() > 1)
				{
					int limit;
					if (source == target) limit = 2;
					else if (twoWay) limit = 3;
					else limit = 2;
					
					if (sourceCluster == destCluster)
					{
						if (sourceCluster.stream().filter(g -> sources.contains(g)).count() < limit) continue targetLoop;
						if (sourceCluster.stream().filter(g -> localDests.contains(g)).count() < limit) continue targetLoop;
					}
					else if (warpClusters.size() > 2)
					{
						if (sourceCluster.stream().filter(g -> sources.contains(g)).count() + destCluster.stream().filter(g -> sources.contains(g)).count() < limit) continue targetLoop;
						if (sourceCluster.stream().filter(g -> localDests.contains(g)).count() + destCluster.stream().filter(g -> localDests.contains(g)).count() < limit) continue targetLoop;
					}
				}
				
				if (controllableBranches <= neededBranches)
				{
					//If the source and destination cluster are the same, and none of the new branches solve any one-way branches, continue
					if (sourceCluster == destCluster
							&& !newBranches.stream().anyMatch(b -> !oneWayBranches.stream().anyMatch(
									o -> o.groupsAbove.contains(b.destGroup) && o.groupsBelow.contains(b.sourceGroup))))
						continue targetLoop;
					
					//If a merge is being created, and the merge sum is greater than or equal to the fork sum, continue
					if (newBranches.stream().anyMatch(b -> oneWayBranches.stream().anyMatch(o -> o.groupsBelow.contains(b.destGroup))))
						continue targetLoop;
					
					//If a fork is being created, and the fork sum is greater than or equal to the merge sum, continue
					if (oneWayBranches.stream().anyMatch(b -> b.groupsAbove.contains(source)) &&
							(!twoWay || oneWayBranches.stream().anyMatch(b -> b.groupsAbove.contains(target))))
						continue targetLoop;
				}
				
				//Perform additional branch-wise testing
				//if any two fork branches share a warp below, they're equivalent
				//if and two merge branches share a warp above, they're equivalent
				//It's actually any warp below and no warps above?
				
				//Create the branch and update tracking data
				for (Branch branch : newBranches)
				{
					branch.groupsAbove = this.getAllAccessors(branch.sourceGroup, network);
					branch.groupsBelow = this.getAllAccessees(branch.destGroup, network);
					
					sources.remove(branch.sourceGroup);
					localDests.remove(branch.destGroup);
					newSources.add(branch.sourceGroup);
					newDests.add(branch.destGroup);
					if (inaccessibleGroups.contains(branch.destGroup)) inaccessibleGroups.remove(branch.destGroup);
					if (!network.get(branch.sourceGroup).contains(branch.destGroup)) network.get(branch.sourceGroup).add(branch.destGroup);
					
					for (int branchIndex = 0; branchIndex < oneWayBranches.size();)
					{
						Branch oneWayBranch = oneWayBranches.get(branchIndex);
						
						if (oneWayBranch.groupsBelow.contains(branch.sourceGroup)) for (List<Warp> t : branch.groupsBelow)
						{
							if (t == oneWayBranch.sourceGroup)
							{
								oneWayBranches.remove(oneWayBranch);
								for (List<Branch> fork : forks) fork.remove(oneWayBranch);
								for (List<Branch> merge : merges) merge.remove(oneWayBranch);
								continue;
							}
							else if (!oneWayBranch.groupsBelow.contains(t)) oneWayBranch.groupsBelow.add(t);
						}
						
						if (oneWayBranch.groupsAbove.contains(branch.destGroup)) for (List<Warp> s : branch.groupsAbove)
						{
							if (s == oneWayBranch.destGroup)
							{
								oneWayBranches.remove(oneWayBranch);
								for (List<Branch> fork : forks) fork.remove(oneWayBranch);
								for (List<Branch> merge : merges) merge.remove(oneWayBranch);
								continue;
							}
							if (!oneWayBranch.groupsAbove.contains(s)) oneWayBranch.groupsAbove.add(s);
						}
						
						branchIndex++;
					}
					
					forks.removeAll(forks.stream().filter(f -> f.size() <= 1).collect(Collectors.toList()));
					merges.removeAll(merges.stream().filter(m -> m.size() <= 1).collect(Collectors.toList()));
					
					if (!this.canAccess(branch.destGroup, branch.sourceGroup, network) &&
						!oneWayBranches.stream().anyMatch(b -> b.groupsBelow.containsAll(branch.groupsBelow) &&
							branch.groupsBelow.containsAll(b.groupsBelow) &&
							b.groupsAbove.containsAll(branch.groupsAbove) &&
							branch.groupsAbove.containsAll(b.groupsAbove)))
					{
						boolean merged = false;
						boolean forked = false;
						
						for (Branch otherBranch : oneWayBranches)
						{
							if (otherBranch.groupsBelow.containsAll(branch.groupsBelow) &&
									branch.groupsBelow.containsAll(otherBranch.groupsBelow) &&
									!branch.groupsAbove.stream().anyMatch(g -> otherBranch.groupsAbove.contains(g)) &&
									!merged)
							{
								List<Branch> merge = merges.stream().filter(m -> m.contains(otherBranch)).findFirst().orElse(new ArrayList<>());
								merge.add(branch);
								if (!merge.contains(otherBranch)) merge.add(otherBranch);
								if (!merges.contains(merge)) merges.add(merge);
								merged = true;
							}
							
							if (branch.groupsAbove.containsAll(otherBranch.groupsAbove) &&
									otherBranch.groupsAbove.containsAll(branch.groupsAbove) &&
									!branch.groupsBelow.stream().anyMatch(g -> otherBranch.groupsBelow.contains(g)) &&
									!forked)
							{
								List<Branch> fork = forks.stream().filter(f -> f.contains(otherBranch)).findFirst().orElse(new ArrayList<>());
								fork.add(branch);
								if (!fork.contains(otherBranch)) fork.add(otherBranch);
								if (!forks.contains(fork)) forks.add(fork);
								forked = true;
							}
						}
						
						oneWayBranches.add(branch);
					}
					
					if (sourceCluster != destCluster)
					{
						sourceCluster.addAll(destCluster);
						warpClusters.remove(destCluster);
						destCluster = sourceCluster;
					}
				}
				
				break targetLoop;
			}
			
			if (sources.contains(source)) throw new IllegalStateException("Could not find a destination warp for source " + source);
			
			if (!allowSelfWarps && !testedAllSources)
			{
				while (sourceIndex < sources.size() && newDests.contains(sources.get(sourceIndex))) sourceIndex++;
				if (sourceIndex >= sources.size())
				{
					sourceIndex = 0;
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