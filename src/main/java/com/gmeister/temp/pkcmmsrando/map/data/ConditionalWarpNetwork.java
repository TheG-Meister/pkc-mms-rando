package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ConditionalWarpNetwork
{
	
	public static class Node
	{
		private final List<Warp> warps;
		private final Set<Flag> obtainableFlags;
		
		public Node(List<Warp> warps, Set<Flag> obtainableFlags)
		{
			this.warps = warps;
			this.obtainableFlags = obtainableFlags;
		}

		public List<Warp> getWarps()
		{ return new ArrayList<>(this.warps); }

		public Set<Flag> getObtainableFlags()
		{ return new HashSet<>(this.obtainableFlags); }
	}
	
	public static class Branch
	{
		private final Node source;
		private final Node target;
		private final Set<Flag> requiredFlags;
		
		public Branch(Node source, Node target, Set<Flag> requiredFlags)
		{
			super();
			this.source = source;
			this.target = target;
			this.requiredFlags = requiredFlags;
		}

		public Node getSource()
		{ return this.source; }

		public Node getTarget()
		{ return this.target; }

		public Set<Flag> getRequiredFlags()
		{ return new HashSet<>(this.requiredFlags); }
	}
	
	private java.util.Map<List<Warp>, Node> nodeMap;
	private java.util.Map<Node, Set<Branch>> branchMap;
	
	public ConditionalWarpNetwork(List<Node> nodes, List<Branch> branches)
	{
		this.nodeMap = new HashMap<>();
		this.branchMap = new HashMap<>();
		
		for (Node node : nodes) this.addNode(node);
		for (Branch branch : branches) this.addBranch(branch);
	}
	
	public Node getNode(List<Warp> key)
	{ return this.nodeMap.get(key); }
	
	public List<Node> getNodes()
	{
		return this.nodeMap.entrySet().stream().map(e -> e.getValue()).collect(Collectors.toList());
	}
	
	private Set<Branch> getBranchEntry(Node source)
	{
		if (source == null) throw new IllegalArgumentException("source node cannot be null");
		if (!this.nodeMap.containsValue(source)) throw new IllegalArgumentException("This node is not part of the network");
		
		Set<Branch> branches = this.branchMap.get(source);
		//This should never be the case as addNode also adds entries to branchMap
		if (branches == null)
		{
			branches = new HashSet<>();
			this.branchMap.put(source, branches);
		}
		return branches;
	}
	
	public List<Branch> getBranches(Node source)
	{
		return new ArrayList<>(this.getBranchEntry(source));
	}
	
	public void addNode(Node node)
	{
		if (node == null) throw new IllegalArgumentException("node cannot be null");
		
		this.nodeMap.put(node.getWarps(), node);
		this.branchMap.put(node, new HashSet<>());
	}

	//This adds branches even if they are duplicates
	public void addBranch(Branch branch)
	{
		this.getBranchEntry(branch.getSource()).add(branch);
	}
	
	public WarpNetwork collapse()
	{
		List<Flag> flags = new ArrayList<>();
		for (Node node : this.branchMap.keySet()) for (Branch branch : this.branchMap.get(node))
			for (Flag flag : branch.requiredFlags) if (!flags.contains(flag)) flags.add(flag);
		return this.collapse(flags);
	}
	
	public WarpNetwork collapse(List<Flag> flags)
	{
		java.util.Map<List<Warp>, List<List<Warp>>> networkMap = new HashMap<>();
		for (List<Warp> key : this.nodeMap.keySet()) networkMap.put(key, new ArrayList<>());
		for (Node key : this.branchMap.keySet()) for (Branch branch : this.branchMap.get(key))
			if (flags.containsAll(branch.requiredFlags)) networkMap.get(branch.source.warps)
					.add(branch.target.warps);
		
		return new WarpNetwork(networkMap);
	}
	
	public void printEdgeTable()
	{
		System.out.println("Source\tTarget\tFlags");
		for (Node key : this.branchMap.keySet()) for (Branch branch : this.branchMap.get(key))
			System.out.println(
					branch.source.warps.get(0).getPosition() + "\t" +
					branch.target.warps.get(0).getPosition() + "\t" +
					branch.requiredFlags.stream().map(f -> f.getName()).collect(Collectors.toList()));
	}
	
}