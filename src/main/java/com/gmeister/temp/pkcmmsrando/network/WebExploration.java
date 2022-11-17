package com.gmeister.temp.pkcmmsrando.network;

import java.util.ArrayList;
import java.util.List;

public class WebExploration<N extends Node, E extends Edge<? extends N>> extends NetworkExploration<N, E>
{
	
	public WebExploration()
	{ super(); }
	
	public WebExploration(N startingNode)
	{
		super();
		this.exploreFrom(startingNode);
	}
	
	public WebExploration(List<N> nodesToExplore, List<N> nodesAccessed, List<E> edgesAccessed)
	{ super(nodesToExplore, nodesAccessed, edgesAccessed); }
	
	public WebExploration(NetworkExploration<? extends N, ? extends E> other)
	{ super(other); }
	
	public void exploreFrom(N node)
	{ if (!this.nodesAccessed.contains(node)) this.nodesToExplore.add(node); }
	
	/**
	 * Returns a list of all edges that can access a particular node in no particular order
	 * @param target
	 * @return
	 */
	public List<E> getAllSourceEdgesOf(N target)
	{
		if (!this.getNodesAccessed()
				.contains(target))
			throw new IllegalArgumentException("target must be part of the exploration");
		
		List<N> nodes = new ArrayList<>();
		nodes.add(target);
		List<E> paths = new ArrayList<>();
		
		for (int i = 0; i < nodes.size(); i++)
		{
			N node = nodes.get(i);
			for (E edge : this.edgesAccessed) if (edge.getTarget()
					.equals(node))
			{
				paths.add(edge);
				if (!nodes.contains(edge.getSource())) nodes.add(edge.getSource());
			}
		}
		
		return paths;
	}
	
	@Override
	public void exploreEdge(E edge)
	{
		if (!this.nodesAccessed.contains(edge.getSource())) this.nodesAccessed.add(edge.getSource());
		
		if (!this.nodesAccessed.contains(edge.getTarget()))
		{
			this.nodesAccessed.add(edge.getTarget());
			this.nodesToExplore.add(edge.getTarget());
		}
		this.edgesAccessed.add(edge);
	}
	
}
