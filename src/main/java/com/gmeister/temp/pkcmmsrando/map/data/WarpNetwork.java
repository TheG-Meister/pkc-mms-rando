package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.ArrayList;
import java.util.HashMap;
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
		
		public Branch(Branch other)
		{
			this.source = other.source;
			this.target = other.target;
			this.sourceTier = other.sourceTier;
			this.targetTier = other.targetTier;
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
	
	public WarpNetwork(WarpNetwork other)
	{
		this.network = new HashMap<>();
		for (List<Warp> key : other.network.keySet()) this.network.put(key, new ArrayList<>(other.network.get(key)));
		this.components = new ArrayList<>(other.components.stream().map(c -> new ArrayList<>(c)).collect(Collectors.toList()));
		this.oneWayBranches = new ArrayList<>(other.oneWayBranches.stream().map(b -> new Branch(b)).collect(Collectors.toList()));
		
		this.tiers = new ArrayList<>();
		for (List<List<Warp>> tier : other.tiers)
		{
			List<List<Warp>> newTier = new ArrayList<>(tier);
			this.tiers.add(newTier);
			for (Branch branch : this.oneWayBranches)
			{
				if (branch.sourceTier == tier) branch.sourceTier = newTier;
				if (branch.targetTier == tier) branch.targetTier = newTier;
			}
		}
	}
	
	private void setUpComponents()
	{
		this.components = new ArrayList<>();
		
		for (List<Warp> node : this.network.keySet())
		{
			List<List<Warp>> component = new ArrayList<>();
			component.add(node);
			this.components.add(component);
		}
		
		for (List<Warp> source : this.network.keySet())
		{
			List<List<Warp>> sourceComponent = this.components.stream().filter(c -> c.contains(source)).findFirst().orElseThrow();
			
			for (List<Warp> target : this.network.get(source)) if (!sourceComponent.contains(target))
			{
				List<List<Warp>> targetComponent = this.components.stream().filter(c -> c.contains(target)).findFirst().orElseThrow();
				sourceComponent.addAll(targetComponent);
				this.components.remove(targetComponent);
			}
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
			if (this.canAccessThroughNetwork(target, source)) continue branch;
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
		
		for (List<Warp> source : this.network.keySet())
		{
			List<List<Warp>> sourceTier = this.tiers.stream().filter(t -> t.contains(source)).findFirst().orElseThrow();
			
			branch:
			for (List<Warp> target : this.network.get(source))
			{
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
				
				for (Branch branch : this.oneWayBranches)
				{
					if (branch.sourceTier == targetTier) branch.sourceTier = sourceTier;
					if (branch.targetTier == targetTier) branch.targetTier = sourceTier;
				}
			}
		}
	}
	
	public void addBranch(List<Warp> source, List<Warp> target)
	{
		if (this.network.get(source)
				.contains(target))
			return;
		
		Branch newBranch = new Branch(source, target, null, null);
		this.network.get(source)
				.add(target);
		
		List<List<Warp>> sourceComponent = this.components.stream()
				.filter(c -> c.contains(source))
				.findFirst()
				.orElseThrow();
		List<List<Warp>> targetComponent = this.components.stream()
				.filter(c -> c.contains(target))
				.findFirst()
				.orElseThrow();
		
		if (sourceComponent != targetComponent)
		{
			sourceComponent.addAll(targetComponent);
			this.components.remove(targetComponent);
		}
		
		List<List<Warp>> sourceTier = this.tiers.stream()
				.filter(t -> t.contains(source))
				.findFirst()
				.orElseThrow();
		List<List<Warp>> targetTier = this.tiers.stream()
				.filter(t -> t.contains(target))
				.findFirst()
				.orElseThrow();
		
		if (sourceTier != targetTier)
		{
			newBranch.sourceTier = sourceTier;
			newBranch.targetTier = targetTier;
			this.oneWayBranches.add(newBranch);
			
			for (Branch branch : this.oneWayBranches)
				if (branch.sourceTier != branch.targetTier && this.canAccessTier(branch.targetTier, branch.sourceTier))
			{
				List<List<Warp>> newTier = branch.sourceTier;
				List<List<Warp>> removedTier = branch.targetTier;
				newTier.addAll(removedTier);
				this.tiers.remove(removedTier);
				
				for (Branch c : this.oneWayBranches)
				{
					if (c.sourceTier == removedTier) c.sourceTier = newTier;
					if (c.targetTier == removedTier) c.targetTier = newTier;
				}
			}
			
			this.oneWayBranches.removeAll(this.oneWayBranches.stream()
					.filter(b -> b.sourceTier == b.targetTier)
					.collect(Collectors.toList()));
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
	
	/**
	 * Returns a list of warps which can access the target warp
	 * @param target the target warp
	 * @return a List of warps which can access the target
	 */
	public List<List<Warp>> getAllAccessors(List<Warp> target)
	{
		List<List<Warp>> accessors = new ArrayList<>();
		accessors.add(target);
		
		//BEWARE, the size of the list changes during this loop
		for (int i = 0; i < accessors.size(); i++)
		{
			final int j = i;
			accessors.addAll(this.network.entrySet()
					.stream()
					.filter(e -> e.getValue()
							.contains(accessors.get(j)) && !accessors.contains(e.getKey()))
					.map(e -> e.getKey())
					.distinct()
					.collect(Collectors.toList()));
		}
		
		return accessors;
	}
	
	/**
	 * Returns a list of warps which can be accessed from the source warp.
	 * @param source the source warp
	 * @return a List of warps which can be accessed from the source warp
	 */
	public List<List<Warp>> getAllAccessees(List<Warp> source)
	{
		List<List<Warp>> accessees = new ArrayList<>();
		accessees.add(source);
		
		//BEWARE, the size of the list changes during this loop
		for (int i = 0; i < accessees.size(); i++) accessees.addAll(this.network.get(accessees.get(i))
				.stream()
				.filter(g -> !accessees.contains(g))
				.distinct()
				.collect(Collectors.toList()));
		
		return accessees;
	}
	
	/**
	 * Returns a list of tiers which can access the target tier.
	 * @param targetTier the target tier
	 * @return a List of tiers which can access the target tier
	 */
	public List<List<List<Warp>>> getAllAccessorTiers(List<List<Warp>> targetTier)
	{
		if (targetTier == null) throw new NullPointerException();
		if (!this.tiers.contains(targetTier)) throw new IllegalArgumentException();
		
		List<List<List<Warp>>> accessors = new ArrayList<>();
		accessors.add(targetTier);
		
		for (int i = 0; i < accessors.size(); i++)
		{
			final int j = i;
			this.oneWayBranches.stream().filter(b -> b.targetTier == accessors.get(j) && !accessors.contains(b.sourceTier)).forEach(b -> accessors.add(b.sourceTier));
		}
		
		return accessors;
	}
	
	/**
	 * Returns a list of tiers which can be accessed from the source tier.
	 * @param sourceTier the source tier
	 * @return a List of tiers which can be accessed from the source tier
	 */
	public List<List<List<Warp>>> getAllAccesseeTiers(List<List<Warp>> sourceTier)
	{
		if (sourceTier == null) throw new NullPointerException();
		if (!this.tiers.contains(sourceTier)) throw new IllegalArgumentException();
		
		List<List<List<Warp>>> accessees = new ArrayList<>();
		accessees.add(sourceTier);
		
		for (int i = 0; i < accessees.size(); i++)
		{
			final int j = i;
			this.oneWayBranches.stream().filter(b -> b.sourceTier == accessees.get(j) && !accessees.contains(b.targetTier)).forEach(b -> accessees.add(b.targetTier));
		}
		
		return accessees;
	}
	
	public long countTopTiers(List<List<Warp>> component)
	{
		List<List<List<Warp>>> componentTiers = this.getTiersOf(component);
		return componentTiers.stream().filter(t -> this.oneWayBranches.stream().noneMatch(b -> b.targetTier == t)).count();
	}
	
	public long countBottomTiers(List<List<Warp>> component)
	{
		List<List<List<Warp>>> componentTiers = this.getTiersOf(component);
		return componentTiers.stream().filter(t -> this.oneWayBranches.stream().noneMatch(b -> b.sourceTier == t)).count();
	}
	
	public boolean isTopTier(List<List<Warp>> tier)
	{
		return this.oneWayBranches.stream().noneMatch(b -> b.targetTier == tier);
	}
	
	public boolean isBottomTier(List<List<Warp>> tier)
	{
		return this.oneWayBranches.stream().noneMatch(b -> b.sourceTier == tier);
	}
	
	public List<List<Warp>> getTierOf(List<Warp> source)
	{
		return this.tiers.stream().filter(t -> t.contains(source)).findAny().orElseThrow();
	}
	
	public List<List<Warp>> getComponentOf(List<Warp> source)
	{
		return this.components.stream().filter(c -> c.contains(source)).findAny().orElseThrow();
	}
	
	public List<List<Warp>> getComponentOfTier(List<List<Warp>> tier)
	{
		return this.components.stream().filter(c -> c.contains(tier.get(0))).findAny().orElseThrow();
	}
	
	public List<List<List<Warp>>> getTiersOf(List<List<Warp>> component)
	{
		if (!this.components.contains(component)) throw new IllegalArgumentException("This network does not contain the provided component");
		return this.tiers.stream().filter(t -> component.contains(t.get(0))).collect(Collectors.toList());
	}
	
	public boolean canAccessTier(List<List<Warp>> sourceTier, List<List<Warp>> targetTier)
	{
		if (sourceTier == null) throw new NullPointerException();
		if (targetTier == null) throw new NullPointerException();
		if (sourceTier == targetTier) return true;
		
		List<List<List<Warp>>> accessedTiers = new ArrayList<>();
		accessedTiers.add(sourceTier);
		
		for (int i = 0; i < accessedTiers.size(); i++)
		{
			final int j = i;
			this.oneWayBranches.stream()
					.filter(b -> b.sourceTier == accessedTiers.get(j) && !accessedTiers.contains(b.targetTier))
					.map(b -> b.targetTier)
					.distinct()
					.forEach(t -> accessedTiers.add(t));
			if (accessedTiers.contains(targetTier)) return true;
		}
		
		return false;
	}
	
	public boolean canAccessThroughTiers(List<Warp> source, List<Warp> target)
	{
		return this.canAccessTier(this.getTierOf(source), this.getTierOf(target));
	}
	
	public boolean canAccessThroughNetwork(List<Warp> source, List<Warp> target)
	{
		List<List<Warp>> downstreamGroups = new ArrayList<>();
		//This is intentionally different getAllAccessees to test a group accessing itself
		downstreamGroups.addAll(this.network.get(source));
		
		//BEWARE, the size of the list changes during this loop
		for (int j = 0; j < downstreamGroups.size(); j++)
		{
			downstreamGroups.addAll(this.network.get(downstreamGroups.get(j)).stream().filter(
					g -> !downstreamGroups.contains(g)).distinct().collect(Collectors.toList()));
			if (downstreamGroups.contains(target)) return true;
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
	
	public void validateNetwork()
	{
		WarpNetwork other = new WarpNetwork(this.network);
		
		for (List<List<Warp>> component : this.components) if (other.components.stream()
				.filter(c -> c.containsAll(component) && component.containsAll(c))
				.count() != 1)
			throw new IllegalStateException();
		
		for (List<List<Warp>> component : other.components) if (this.components.stream()
				.filter(c -> c.containsAll(component) && component.containsAll(c))
				.count() != 1)
			throw new IllegalStateException();
		
		Map<List<List<Warp>>, List<List<Warp>>> tierMap = new HashMap<>();
		for (List<List<Warp>> tier : this.tiers)
		{
			List<List<List<Warp>>> equivalentTiers = other.tiers.stream()
					.filter(c -> c.containsAll(tier) && tier.containsAll(c))
					.collect(Collectors.toList());
			
			if (equivalentTiers.size() != 1) throw new IllegalStateException();
			tierMap.put(tier, equivalentTiers.get(0));
		}
		
		for (List<List<Warp>> tier : other.tiers) if (this.tiers.stream()
				.filter(c -> c.containsAll(tier) && tier.containsAll(c))
				.count() != 1)
			throw new IllegalStateException();
		
		branches:
		for (Branch branch : this.oneWayBranches)
		{
			for (Branch b : other.oneWayBranches) if (b.source == branch.source && b.target == branch.target
							&& b.sourceTier == tierMap.get(branch.sourceTier)
							&& b.targetTier == tierMap.get(branch.targetTier))
				continue branches;
			throw new IllegalStateException();
		}
		
		for (Branch branch : other.oneWayBranches) if (!this.oneWayBranches.stream()
				.anyMatch(b -> b.source == branch.source && b.target == branch.target
						&& tierMap.get(b.sourceTier) == branch.sourceTier
						&& tierMap.get(b.targetTier) == branch.targetTier))
			throw new IllegalStateException();
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
