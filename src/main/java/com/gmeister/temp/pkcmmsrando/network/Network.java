package com.gmeister.temp.pkcmmsrando.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Network<N, B extends Branch<N>>
{
	
	private Set<N> nodes;
	private Map<N, Set<B>> branchMap;
	private Set<Set<N>> components;
	
	public Network(Collection<? extends N> nodes, Collection<? extends B> branches)
	{
		this.nodes = new HashSet<>();
		this.branchMap = new HashMap<>();
		this.components = new HashSet<>();
		
		this.addNodes(nodes);
		this.addBranches(branches);
	}
	
	public Network(Network<? extends N, ? extends B> other)
	{
		this.nodes = new HashSet<>();
		this.branchMap = new HashMap<>();
		this.components = new HashSet<>();
		
		this.addNodes(other.getNodes());
		
		Set<B> branches = new HashSet<>();
		for (Set<? extends B> nodeBranches : other.branchMap.values()) branches.addAll(nodeBranches);
		this.addBranches(branches);
	}
	
	public Set<N> getNodes()
	{ return new HashSet<>(this.nodes); }
	
	public Set<Set<N>> getComponents()
	{
		Set<Set<N>> components = new HashSet<>();
		for (Set<N> component : this.components) components.add(new HashSet<>(component));
		return components;
	}
	
	private Set<B> getBranchEntry(N source)
	{
		if (source == null) throw new IllegalArgumentException("source node must not be null");
		if (!this.nodes.contains(source)) throw new IllegalArgumentException("source node must be part of the network");
		
		Set<B> branches = this.branchMap.get(source);
		//This should never be the case as addNode also adds entries to branchMap
		if (branches == null)
		{
			branches = new HashSet<>();
			this.branchMap.put(source, branches);
		}
		return branches;
	}
	
	public Set<B> getBranches(N source)
	{ return new HashSet<>(this.getBranchEntry(source)); }
	
	public void addNodes(Collection<? extends N> nodes)
	{
		if (nodes == null) throw new IllegalArgumentException("nodes must not be null");
		if (nodes.contains(null)) throw new IllegalArgumentException("nodes must not contain null elements");
		
		for (N node : nodes) this.addNode(node);
	}
	
	protected void validateNode(N node)
	{
		if (node == null) throw new IllegalArgumentException("node must not be null");
		if (!this.nodes.contains(node)) throw new IllegalArgumentException("node must be part of the network");
	}
	
	public void addNode(N node)
	{
		if (node == null) throw new IllegalArgumentException("node must not be null");
		
		this.nodes.add(node);
		this.branchMap.put(node, new HashSet<>());
		
		Set<N> component = new HashSet<>();
		component.add(node);
		this.components.add(component);
	}
	
	protected void validateBranch(B branch)
	{
		if (branch == null) throw new IllegalArgumentException("branch must not be null");
		
		if (branch.getSource() == null) throw new IllegalArgumentException("branch source must not be null");
		if (branch.getTarget() == null) throw new IllegalArgumentException("branch target must not be null");
		
		if (!this.nodes.contains(branch.getSource())) throw new IllegalArgumentException("branch source must be part of the network");
		if (!this.nodes.contains(branch.getTarget())) throw new IllegalArgumentException("branch target must be part of the network");
	}
	
	public void addBranches(Collection<? extends B> branches)
	{
		if (branches == null) throw new IllegalArgumentException("branches must not be null");
		
		//Validate all branches
		for (B branch : branches) this.validateBranch(branch);
		
		//Add all branches
		for (B branch : branches) this.addBranch(branch);
	}
	
	public void addBranch(B branch)
	{
		this.validateBranch(branch);
		
		this.getBranchEntry(branch.getSource())
			.add(branch);
		
		Set<N> sourceComponent = this.getComponentOf(branch.getSource());
		Set<N> targetComponent = this.getComponentOf(branch.getTarget());
		if (sourceComponent != targetComponent) this.mergeComponents(sourceComponent, targetComponent);
	}
	
	public void printEdgeTable()
	{
		System.out.println("Source\tTarget");
		for (N key : this.branchMap.keySet()) for (B branch : this.branchMap.get(key))
			System.out.println(
					branch.getSource() + "\t" +
					branch.getTarget());
	}
	
	/**
	 * Initialises the set of components. This can be used to recalculate the components of this network if any of the nodes or branches have been modified.
	 */
	public void initComponents()
	{
		this.components = new HashSet<>();
		Map<N, Set<N>> componentMap = new HashMap<>();
		
		for (N node : this.nodes)
		{
			Set<N> component = new HashSet<>();
			component.add(node);
			this.components.add(component);
			componentMap.put(node, component);
		}
		
		for (N source : this.branchMap.keySet())
		{
			Set<N> sourceComponent = componentMap.get(source);
			for (B edge : this.getBranchEntry(source)) if (!sourceComponent.contains(edge.getTarget()))
			{
				Set<N> targetComponent = componentMap.get(edge.getTarget());
				this.mergeComponents(sourceComponent, targetComponent);
				for (N node : targetComponent) componentMap.put(node, sourceComponent);
			}
		}
	}
	
	/**
	 * Merges two components into a single component. More specifically, puts all elements of <code>otherComponent</code> into <code>component</code>.
	 * @param component
	 * @param otherComponent
	 */
	private void mergeComponents(Set<N> component, Set<N> otherComponent)
	{
		if (!this.components.contains(component)) throw new IllegalArgumentException("component must be part of the network");
		if (!this.components.contains(otherComponent)) throw new IllegalArgumentException("otherComponent must be part of the network");
		
		if (component == otherComponent) throw new IllegalArgumentException("component and otherComponent must not be the same object");
		
		component.addAll(otherComponent);
		this.components.remove(otherComponent);
	}
	
	//This should probably return a copy, as a getting class could modify the component
	public Set<N> getComponentOf(N node)
	{
		if (node == null) throw new IllegalArgumentException("node must not be null");
		if (!this.nodes.contains(node)) throw new IllegalArgumentException("node must be part of the network");
		
		return this.components.stream().filter(c -> c.contains(node)).findAny().orElseThrow();
	}
	
	public boolean hasPath(N source, N target)
	{
		this.validateNode(source);
		this.validateNode(target);
		
		List<N> targets = new ArrayList<>();
		targets.add(source);
		
		for (int i = 0; i < targets.size(); i++)
		{
			N node = targets.get(i);
			for (B branch : this.getBranchEntry(node))
			{
				if (branch.getTarget().equals(target)) return true;
				else if (!targets.contains(branch.getTarget())) targets.add(branch.getTarget());
			}
		}
		
		return false;
	}
	
}
