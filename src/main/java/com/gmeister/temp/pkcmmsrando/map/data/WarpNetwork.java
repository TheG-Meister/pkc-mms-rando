package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WarpNetwork
{
	
	public static class Branch
	{
		public List<Warp> sourceGroup;
		public List<Warp> destGroup;
		public List<List<Warp>> groupsAbove = new ArrayList<>();
		public List<List<Warp>> groupsBelow = new ArrayList<>();
		
		public Branch(List<Warp> sourceGroup, List<Warp> destGroup, List<List<Warp>> groupsAbove,
				List<List<Warp>> groupsBelow)
		{
			this.sourceGroup = sourceGroup;
			this.destGroup = destGroup;
			this.groupsAbove = groupsAbove;
			this.groupsBelow = groupsBelow;
		}
	}
	
	private Map<List<Warp>, List<List<Warp>>> network;
	
	public WarpNetwork(Map<List<Warp>, List<List<Warp>>> network)
	{ this.network = network; }
	
	public Map<List<Warp>, List<List<Warp>>> getNetwork()
	{ return this.network; }
	
	public void setNetwork(Map<List<Warp>, List<List<Warp>>> network)
	{ this.network = network; }
	
	public void print()
	{
		for (List<Warp> warpGroup : this.network.keySet()) System.out.print(";" + warpGroup);
		System.out.println();
		for (List<Warp> warpGroup : this.network.keySet())
		{
			List<List<Warp>> accessibleGroups = this.network.get(warpGroup);
			System.out.print(warpGroup);
			for (List<Warp> otherGroup : this.network.keySet())
				System.out.print(";" + (accessibleGroups.contains(otherGroup) ? 1 : 0));
			System.out.println();
		}
	}
	
	public List<List<Warp>> getAllAccessors(List<Warp> warpGroup)
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
	
	public List<List<Warp>> getAllAccessees(List<Warp> warpGroup)
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
	
	public boolean canAccess(List<Warp> warpGroup, List<Warp> otherGroup)
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
	
	public List<List<List<Warp>>> groupWarpGroups(List<List<Warp>> warpGroups)
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
	
	public void removeRedundantBranches(Map<List<Warp>, List<List<Warp>>> network)
	{
		for (List<Warp> warpGroup : network.keySet())
		{
			List<List<Warp>> accessees = network.get(warpGroup);
			List<List<Warp>> allAccessees = this.getAllAccessees(warpGroup);
			
			for (int i = 0; i < accessees.size();)
			{
				List<Warp> accessee = accessees.remove(i);
				List<List<Warp>> newTotalAccessees = this.getAllAccessees(warpGroup);
				if (!newTotalAccessees.containsAll(allAccessees))
				{
					accessees.add(i, accessee);
					i++;
				}
			}
		}
	}
	
	public void checkRemovedBranches(WarpNetwork network)
	{
		for (List<Warp> warpGroup : this.network.keySet())
		{
			List<List<Warp>> groupsBelow = this.getAllAccessees(warpGroup);
			List<List<Warp>> newGroupsBelow = network.getAllAccessees(warpGroup);
			
			if (!groupsBelow.containsAll(newGroupsBelow) || !newGroupsBelow.containsAll(groupsBelow))
				throw new IllegalStateException("Branch removal algorithm removes too many branches");
		}
	}
	
}
