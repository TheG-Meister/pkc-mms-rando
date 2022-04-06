package com.gmeister.temp.pkcmmsrando.network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Network<N, B extends Branch<N>>
{
	
	private Set<N> nodes;
	private Map<N, Set<B>> branchMap;
	private Set<Set<N>> components;
	
	public Network(Set<N> nodes, Set<B> branches)
	{
		this.nodes = new HashSet<>();
		this.branchMap = new HashMap<>();
		
		for (N node : nodes) this.addNode(node);
		for (B branch : branches) this.addBranch(branch);
		
		this.setUpComponents();
	}
	
	public Network(Network<N, B> other)
	{
		this.nodes = new HashSet<>(other.nodes);
		
		this.branchMap = new HashMap<>();
		for (Set<? extends B> branches : other.branchMap.values()) for (B branch : branches) this.addBranch(branch);
		
		this.setUpComponents();
	}
	
	public Set<N> getNodes()
	{ return new HashSet<>(this.nodes); }
	
	private Set<B> getBranchEntry(N source)
	{
		if (source == null) throw new IllegalArgumentException("source node cannot be null");
		if (!this.nodes.contains(source)) throw new IllegalArgumentException("This node is not part of the network");
		
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
	
	public void addNode(N node)
	{
		if (node == null) throw new IllegalArgumentException("node cannot be null");
		
		this.nodes.add(node);
		this.branchMap.put(node, new HashSet<>());
	}
	
	public void addBranch(B branch)
	{ this.getBranchEntry(branch.getSource())
			.add(branch); }
	
	public void printEdgeTable()
	{
		System.out.println("Source\tTarget");
		for (N key : this.branchMap.keySet()) for (B branch : this.branchMap.get(key))
			System.out.println(
					branch.getSource() + "\t" +
					branch.getTarget());
	}
	
	private void setUpComponents()
	{
		this.components = new HashSet<>();
		Map<N, Set<N>> componentMap = new HashMap<>();
		
		for (N node : this.nodes)
		{
			Set<N> set = new HashSet<>();
			set.add(node);
			this.components.add(set);
			componentMap.put(node, set);
		}
		
		for (N source : this.branchMap.keySet())
		{
			Set<N> sourceComponent = componentMap.get(source);
			for (B edge : this.getBranchEntry(source)) if (!sourceComponent.contains(edge.getTarget()))
			{
				Set<N> targetComponent = componentMap.get(edge.getTarget());
				sourceComponent.addAll(targetComponent);
				this.components.remove(targetComponent);
				for (N node : targetComponent) componentMap.put(node, sourceComponent);
			}
		}
	}
	
	public Set<N> getComponentOf(N node)
	{
		if (node == null) throw new IllegalArgumentException("node cannot be null");
		if (!this.nodes.contains(node)) throw new IllegalArgumentException("node is not part of the network");
		
		return this.components.stream().filter(c -> c.contains(node)).findAny().orElseThrow();
	}
	
}
