package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WarpNetwork
{
	
	public static class Branch
	{
		public List<Warp> source;
		public List<Warp> target;
		public List<List<Warp>> sourceTier;
		public List<List<Warp>> targetTier;
		
		public Branch()
		{}
		
		public Branch(List<Warp> source, List<Warp> target, List<List<Warp>> sourceTier, List<List<Warp>> targetTier)
		{
			this.source = source;
			this.target = target;
			this.sourceTier = sourceTier;
			this.targetTier = targetTier;
		}
	}
	
	private Map<List<Warp>, List<List<Warp>>> network;
	private List<List<List<Warp>>> components;
	private List<List<List<Warp>>> tiers;
	private List<Branch> oneWayBranches;
	
	public WarpNetwork(Map<List<Warp>, List<List<Warp>>> network)
	{
		this.network = network;
		this.setUpComponents();
		this.setUpOneWayBranches();
		this.setUpTiers();
	}
	
	private void setUpComponents()
	{
		this.components = new ArrayList<>();
		for (List<Warp> warpGroup : this.network.keySet())
		{
			//Find an existing warp group group that contains this warp group, or any warp group that this warp group can access. Otherwise, make a new group
			List<List<Warp>> warpGroupGroup = this.components.stream().filter(g -> g.contains(
					warpGroup)).findFirst().orElse(this.components.stream().filter(
							g -> this.network.get(warpGroup).stream().anyMatch(h -> g.contains(h))).findFirst().orElse(
									new ArrayList<>()));
			
			//If the warp group group isn't part of the list, add it
			if (!this.components.contains(warpGroupGroup)) this.components.add(warpGroupGroup);
			
			//If this warp group isn't part of the warp group group, add it
			if (!warpGroupGroup.contains(warpGroup)) warpGroupGroup.add(warpGroup);
			
			//Add all of the warps groups that this warp group can access to the warp group group if they are not already present
			warpGroupGroup.addAll(this.network.get(warpGroup).stream().filter(g -> !warpGroupGroup.contains(g)).collect(
					Collectors.toList()));
		}
	}
	
	/**
	 * Sets up a list of Branches where the target cannot access the source.<br>
	 * <br>
	 * Branches are created without source tiers and target tiers.
	 */
	private void setUpOneWayBranches()
	{
		this.oneWayBranches = new ArrayList<>();
		
		for (List<Warp> source : this.network.keySet()) branch:
		for (List<Warp> target : this.network.get(source))
		{
			if (this.canAccess(target, source)) continue branch;
			Branch branch = new Branch(source, target, null, null);
			this.oneWayBranches.add(branch);
		}
	}
	
	private void setUpTiers()
	{
		this.tiers = new ArrayList<>();
		
		for (List<Warp> warp : this.network.keySet())
		{
			List<List<Warp>> tier = new ArrayList<>();
			tier.add(warp);
			this.tiers.add(tier);
		}
		
		for (List<Warp> source : this.network.keySet()) branch:
		for (List<Warp> target : this.network.get(source))
		{
			List<List<Warp>> sourceTier = this.tiers.stream().filter(t -> t.contains(source)).findFirst().orElseThrow();
			List<List<Warp>> targetTier = this.tiers.stream().filter(t -> t.contains(target)).findFirst().orElseThrow();
			
			//if tiers are the same, continue
			if (sourceTier == targetTier) continue branch;
			
			//if this branch is one way, continue
			boolean exit = false;
			for (Branch branch : this.oneWayBranches) if (branch.source == source && branch.target == target)
			{
				branch.sourceTier = sourceTier;
				branch.targetTier = targetTier;
				exit = true;
			}
			if (exit) continue branch;
			
			sourceTier.addAll(targetTier);
			this.tiers.remove(targetTier);
		}
	}
	
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
			accessors.addAll(this.network.entrySet().stream().filter(
					e -> e.getValue().contains(accessors.get(j)) && !accessors.contains(e.getKey())).map(
							e -> e.getKey()).distinct().collect(Collectors.toList()));
		}
		
		return accessors;
	}
	
	public List<List<Warp>> getAllAccessees(List<Warp> warpGroup)
	{
		List<List<Warp>> accessees = new ArrayList<>();
		accessees.add(warpGroup);
		
		//BEWARE, the size of the list changes during this loop
		for (int i = 0; i < accessees.size(); i++) accessees.addAll(
				this.network.get(accessees.get(i)).stream().filter(g -> !accessees.contains(g)).distinct().collect(
						Collectors.toList()));
		
		return accessees;
	}
	
	public boolean canAccess(List<Warp> warpGroup, List<Warp> otherGroup)
	{
		List<List<Warp>> downstreamGroups = new ArrayList<>();
		//This is intentionally different getAllAccessees to test a group accessing itself
		downstreamGroups.addAll(this.network.get(warpGroup));
		
		//BEWARE, the size of the list changes during this loop
		for (int j = 0; j < downstreamGroups.size(); j++)
		{
			downstreamGroups.addAll(this.network.get(downstreamGroups.get(j)).stream().filter(
					g -> !downstreamGroups.contains(g)).distinct().collect(Collectors.toList()));
			if (downstreamGroups.contains(otherGroup)) return true;
		}
		
		return false;
	}
	
	public void removeRedundantBranches()
	{
		for (List<Warp> warpGroup : this.network.keySet())
		{
			List<List<Warp>> accessees = this.network.get(warpGroup);
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
	
	public Map<List<Warp>, List<List<Warp>>> getNetwork()
	{ return this.network; }

	public List<List<List<Warp>>> getComponents()
	{ return this.components; }

	public List<List<List<Warp>>> getTiers()
	{ return this.tiers; }

	public List<Branch> getOneWayBranches()
	{ return this.oneWayBranches; }
	
}
