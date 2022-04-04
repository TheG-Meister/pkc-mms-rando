package com.gmeister.temp.pkcmmsrando.network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Network<N, B extends Branch<N>>
{
	
	private Set<N> nodes;
	private Map<N, Set<B>> branchMap;
	
	public Network(Set<N> nodes, Set<B> branches)
	{
		this.nodes = new HashSet<>();
		this.branchMap = new HashMap<>();
		
		for (N node : nodes) this.addNode(node);
		for (B branch : branches) this.addBranch(branch);
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
	
}
