package com.gmeister.temp.pkcmmsrando.network;

import java.util.ArrayList;
import java.util.List;

public class TreeExploration<N extends Node, E extends Edge<? extends N>> extends AbstractNetworkExploration<N, E>
{

	protected N startingNode;
	protected List<E> edgeTree;

	public TreeExploration(N startingNode)
	{
		super();
		this.edgeTree = new ArrayList<>();
		this.startingNode = startingNode;
		
		this.nodesAccessed.add(startingNode);
		this.nodesToExplore.add(startingNode);
	}

	public TreeExploration(N startingNode, List<N> nodesToExplore, List<N> nodesAccessed, List<E> edgesAccessed,
			List<E> edgeTree)
	{
		super(nodesToExplore, nodesAccessed, edgesAccessed);
		this.edgeTree = edgeTree;
		this.startingNode = startingNode;
		
		if (!this.nodesToExplore.contains(startingNode)) this.nodesToExplore.add(startingNode);
		if (!this.nodesAccessed.contains(startingNode)) this.nodesAccessed.add(startingNode); 
	}
	
	public TreeExploration(TreeExploration<? extends N, ? extends E> other)
	{
		super(other);
		this.edgeTree = new ArrayList<>(other.edgeTree);
		this.startingNode = other.startingNode;
	}

	public List<E> getPath(N target)
	{
		if (!this.getNodesAccessed()
				.contains(target))
			throw new IllegalArgumentException("target must be part of the exploration");

		N pathTarget = target;
		List<E> path = new ArrayList<>();

		while (true) for (E edge : this.edgeTree) if (pathTarget.equals(edge.getTarget()))
		{
			path.add(0, edge);
			pathTarget = edge.getSource();
			if (pathTarget.equals(this.startingNode)) return path;
		}
	}
	
	@Override
	public void exploreEdge(E edge)
	{
		if (!this.getNodesAccessed().contains(edge.getSource())) throw new IllegalArgumentException("edge source must be a node in the tree");
		
		if (!this.nodesAccessed.contains(edge.getTarget()))
		{
			this.nodesAccessed.add(edge.getTarget());
			this.nodesToExplore.add(edge.getTarget());
			this.edgeTree.add(edge);
		}
		this.edgesAccessed.add(edge);
	}
	
	public List<E> getEdgeTree()
	{ return new ArrayList<>(this.edgeTree); }

	public N getStartingNode()
	{ return this.startingNode; }

}
