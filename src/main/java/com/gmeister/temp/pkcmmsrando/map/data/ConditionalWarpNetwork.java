package com.gmeister.temp.pkcmmsrando.map.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ConditionalWarpNetwork
{
	
	public static class Node
	{
		public List<Warp> warps;
		public Set<Flag> obtainableFlags;
		
		public Node()
		{}
		
		public Node(List<Warp> warps, Set<Flag> obtainableFlags)
		{
			this.warps = warps;
			this.obtainableFlags = obtainableFlags;
		}
	}
	
	public static class Branch
	{
		public Node source;
		public Node target;
		public Set<Flag> requiredFlags;
		
		public Branch()
		{}
		
		public Branch(Node source, Node target, Set<Flag> requiredFlags)
		{
			super();
			this.source = source;
			this.target = target;
			this.requiredFlags = requiredFlags;
		}
	}
	
	private List<Node> nodes;
	private List<Branch> branches;
	
	public ConditionalWarpNetwork(List<Node> nodes, List<Branch> branches)
	{
		this.nodes = nodes;
		this.branches = branches;
	}
	
	public List<Node> getNodes()
	{ return this.nodes; }
	
	public List<Branch> getBranches()
	{ return this.branches; }
	
	public WarpNetwork collapse()
	{
		List<Flag> flags = new ArrayList<>();
		for (Branch branch : this.branches)
			for (Flag flag : branch.requiredFlags) if (!flags.contains(flag)) flags.add(flag);
		return this.collapse(flags);
	}
	
	public WarpNetwork collapse(List<Flag> flags)
	{
		java.util.Map<List<Warp>, List<List<Warp>>> networkMap = new HashMap<>();
		for (Node node : this.nodes) networkMap.put(node.warps, new ArrayList<>());
		for (Branch branch : this.branches)
			if (flags.containsAll(branch.requiredFlags)) networkMap.get(branch.source.warps)
					.add(branch.target.warps);
		
		return new WarpNetwork(networkMap);
	}
	
	public void printEdgeTable()
	{
		System.out.println("Source\tTarget\tFlags");
		for (Branch branch : branches) System.out.println(branch.source.warps.get(0).getPosition() + "\t" + branch.target.warps.get(0).getPosition() + "\t" + branch.requiredFlags.stream().map(f -> f.getName()).collect(Collectors.toList()));
	}
	
}