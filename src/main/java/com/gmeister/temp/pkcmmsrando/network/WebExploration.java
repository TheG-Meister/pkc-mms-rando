package com.gmeister.temp.pkcmmsrando.network;

import java.util.List;

public class WebExploration<N extends Node, E extends Edge<? extends N>> extends NetworkExploration<N, E>
{
	
	public WebExploration()
	{ super(); }
	
	public WebExploration(List<N> nodesToExplore, List<N> nodesAccessed, List<E> edgesAccessed)
	{ super(nodesToExplore, nodesAccessed, edgesAccessed); }
	
	public WebExploration(NetworkExploration<? extends N, ? extends E> other)
	{ super(other); }

	public void exploreFrom(N node)
	{ if (!this.nodesAccessed.contains(node)) this.nodesToExplore.add(node); }
	
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
